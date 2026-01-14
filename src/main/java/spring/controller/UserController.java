package spring.controller;

import spring.dto.CarFormDto;
import spring.dto.CarViewDto;
import spring.dto.UserFormDto;
import spring.dto.UserSearchFilterDto;
import spring.dto.UserViewDto;
import spring.model.Car;
import spring.model.User;
import spring.service.UserService;
import spring.service.exception.NotFoundException;
import spring.service.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public String finder(UserSearchFilterDto filter, Model modelView) {

        log.debug("Поиск пользователей. Фильтры: email='{}', имя='{}', фамилия='{}', бренд='{}', модель='{}', год={}, цвет='{}'",
                safe(filter.getEmail()), safe(filter.getFirstName()), safe(filter.getLastName()),
                safe(filter.getBrand()), safe(filter.getModel()), filter.getYear(), safe(filter.getColor()));

        List<User> users;
        try {
            users = userService.searchUsersWithCars(
                    filter.getEmail(),
                    filter.getFirstName(),
                    filter.getLastName(),
                    filter.getBrand(),
                    filter.getModel(),
                    filter.getYear(),
                    filter.getColor()
            );
            log.debug("Поиск пользователей завершён. Найдено пользователей: {}", users.size());
        } catch (ValidationException e) {
            users = List.of();
            modelView.addAttribute("error", e.getMessage());
            log.warn("Ошибка валидации при поиске пользователей. Причина: {}", e.getMessage());
        } catch (Exception e) {
            users = List.of();
            modelView.addAttribute("error", "Внутренняя ошибка при поиске пользователей");
            log.error("Непредвиденная ошибка при поиске пользователей", e);
        }

        List<UserViewDto> userDtos = users.stream().map(UserController::toUserViewDto).toList();

        modelView.addAttribute("users", userDtos);
        modelView.addAttribute("newUser", new UserFormDto());
        modelView.addAttribute("newCar", new CarFormDto());
        modelView.addAttribute("filter", filter);

        return "users";
    }

    @PostMapping
    public String createUser(UserFormDto form, RedirectAttributes ra) {
        String email = safe(form.getEmail());
        String fn = safe(form.getFirstName());
        String ln = safe(form.getLastName());

        log.debug("Запрос на создание пользователя: email='{}', имя='{}', фамилия='{}'", email, fn, ln);

        return runAction(
                "создание пользователя",
                null,
                ra,
                () -> userService.createUser(toUserEntity(form)),
                "Пользователь создан",
                "redirect:/users",
                "email='" + email + "'"
        );
    }

    @PostMapping("/{id}/update")
    public String updateUser(@PathVariable Long id, UserFormDto form, RedirectAttributes ra) {
        form.setId(id);

        log.debug("Запрос на обновление пользователя: id={}, email='{}', имя='{}', фамилия='{}'",
                id, safe(form.getEmail()), safe(form.getFirstName()), safe(form.getLastName()));

        return runAction(
                "обновление пользователя",
                id,
                ra,
                () -> {
                    User user = userService.getUserById(id);
                    user.setFirstName(form.getFirstName());
                    user.setLastName(form.getLastName());
                    user.setEmail(form.getEmail());
                    userService.updateUser(user);
                },
                "Пользователь обновлен",
                "redirect:/users",
                "email='" + safe(form.getEmail()) + "'"
        );
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        log.debug("Запрос на удаление пользователя: id={}", id);

        return runAction(
                "удаление пользователя",
                id,
                ra,
                () -> userService.deleteUserById(id),
                "Пользователь удален",
                "redirect:/users",
                null
        );
    }

    @PostMapping("/{id}/cars")
    public String addCar(@PathVariable Long id, CarFormDto form, RedirectAttributes ra) {

        log.debug("Запрос на добавление машины пользователю: userId={}, бренд='{}', модель='{}', год={}, цвет='{}'",
                id, safe(form.getBrand()), safe(form.getModel()), form.getYear(), safe(form.getColor()));

        return runAction(
                "добавление машины пользователю",
                id,
                ra,
                () -> userService.addCarToUser(id, toCarEntity(form)),
                "Машина добавлена",
                "redirect:/users",
                "brand='" + safe(form.getBrand()) + "', model='" + safe(form.getModel()) + "', year=" + form.getYear() + ", color='" + safe(form.getColor()) + "'"
        );
    }

    @PostMapping("/{userId}/cars/{carId}/delete")
    public String deleteCar(@PathVariable Long userId,
                            @PathVariable Long carId,
                            RedirectAttributes ra) {

        log.debug("Запрос на удаление машины: userId={}, carId={}", userId, carId);

        return runAction(
                "удаление машины",
                userId,
                ra,
                () -> userService.removeCarFromUser(userId, carId),
                "Машина удалена",
                "redirect:/users",
                "carId=" + carId
        );
    }

    @GetMapping("/{userId}/cars-fragment")
    public String getUserCarsFragment(@PathVariable Long userId, Model model) {
        log.debug("Запрос фрагмента списка машин для пользователя: userId={}", userId);

        model.addAttribute("__carsFragmentRequest__", true);
        model.addAttribute("userId", userId);

        try {
            List<Car> cars = userService.getUserByIdWithCars(userId).getCars();
            List<CarViewDto> carDtos = cars.stream().map(UserController::toCarViewDto).toList();

            log.info("Список машин получен: userId={}, количество={}", userId, carDtos.size());
            model.addAttribute("cars", carDtos);
            return "cars-fragment :: carsTable";

        } catch (NotFoundException e) {
            log.warn("Пользователь не найден при запросе фрагмента машин: userId={}. Причина: {}", userId, e.getMessage());
            model.addAttribute("error", e.getMessage());

        } catch (ValidationException e) {
            log.warn("Ошибка валидации при запросе фрагмента машин: userId={}. Причина: {}", userId, e.getMessage());
            model.addAttribute("error", e.getMessage());

        } catch (Exception e) {
            log.error("Непредвиденная ошибка при запросе фрагмента машин: userId={}", userId, e);
            model.addAttribute("error", "Внутренняя ошибка при получении списка машин");
        }

        model.addAttribute("cars", List.of());
        return "cars-fragment :: carsTable";
    }

    private String runAction(String actionName,
                             Long id,
                             RedirectAttributes ra,
                             Runnable action,
                             String successMsg,
                             String redirectUrl,
                             String details) {

        if (details != null && !details.isBlank()) {
            log.debug("Детали действия '{}': id={}, {}", actionName, id, details);
        }

        try {
            action.run();
            ra.addFlashAttribute("success", successMsg);
            log.info("Успех: {}. id={}", actionName, id);

        } catch (NotFoundException e) {
            ra.addFlashAttribute("error", e.getMessage());
            log.warn("NotFound при {}: id={}. Причина: {}", actionName, id, e.getMessage());

        } catch (ValidationException e) {
            ra.addFlashAttribute("error", e.getMessage());
            log.warn("Ошибка валидации при {}: id={}. Причина: {}", actionName, id, e.getMessage());

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Внутренняя ошибка при выполнении операции");
            log.error("Непредвиденная ошибка при {}: id={}", actionName, id, e);
        }

        return redirectUrl;
    }

    private static UserViewDto toUserViewDto(User u) {
        UserViewDto dto = new UserViewDto();
        dto.setId(u.getId());
        dto.setEmail(u.getEmail());
        dto.setFirstName(u.getFirstName());
        dto.setLastName(u.getLastName());
        return dto;
    }

    private static CarViewDto toCarViewDto(Car c) {
        CarViewDto dto = new CarViewDto();
        dto.setId(c.getId());
        dto.setBrand(c.getBrand());
        dto.setModel(c.getModel());
        dto.setYear(c.getYear());
        dto.setColor(c.getColor());
        return dto;
    }

    private static User toUserEntity(UserFormDto form) {
        User u = new User();
        u.setId(form.getId());
        u.setEmail(form.getEmail());
        u.setFirstName(form.getFirstName());
        u.setLastName(form.getLastName());
        return u;
    }

    private static Car toCarEntity(CarFormDto form) {
        Car c = new Car();
        c.setBrand(form.getBrand());
        c.setModel(form.getModel());
        if (form.getYear() != null) {
            c.setYear(form.getYear());
        }
        c.setColor(form.getColor());
        return c;
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}