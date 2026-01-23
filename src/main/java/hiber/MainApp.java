package hiber;

import hiber.config.AppConfig;
import hiber.model.Car;
import hiber.model.User;
import hiber.service.CarService;
import hiber.service.UserService;
import hiber.service.exception.NotFoundException;
import hiber.service.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.sql.SQLException;
import java.util.List;

@Slf4j
public class MainApp {
    public static void main(String[] args) throws SQLException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        UserService userService = context.getBean(UserService.class);
        CarService carService = context.getBean(CarService.class);

        userService.add(new User("User1", "Lastname1", "user1@mail.ru"));
        userService.add(new User("User2", "Lastname2", "user2@mail.ru"));
        userService.add(new User("User3", "Lastname3", "user3@mail.ru"));
        userService.add(new User("User4", "Lastname4", "user4@mail.ru"));

        List<User> users = userService.listUsers();
        for (User user : users) {
            log.info("""
                    User:
                    Id = {}
                    First Name = {}
                    Last Name = {}
                    Email = {}
                    
                    """, user.getId(), user.getFirstName(), user.getLastName(), user.getEmail());
        }

        // ------------- свой код -----------------
        // Берём одного пользователя для дальнейших тестов
        Long userId = users.get(0).getId();

        // 3) getUserById()
        log.info("=== Получения пользователя по ID ===");
        User u1 = userService.getUserById(userId);
        log.info("Загружен пользователь: id = {}, email = {}", u1.getId(), u1.getEmail());

        // 4) addCarToUser()
        log.info("=== Добавление машин пользователю ===");
        userService.addCarToUser(userId, new Car("BMW", 3));
        userService.addCarToUser(userId, new Car("BMW", 3));
        userService.addCarToUser(userId, new Car("Audi", 6));
        log.info("Машины добавлены пользователю userId = {}", userId);

        // 5) listUserCars()
        log.info("=== Список машин пользователя ===");
        List<Car> userCars = userService.listUserCars(userId);
        log.info("Всего найдено машин {}", userCars.size());
        for (Car c : userCars) {
            log.info("carId = {}, model = {}, series = {}", c.getId(), c.getModel(), c.getSeries());
        }

        // 6) getUserWithCars() (JOIN FETCH)
        log.info("=== Получение пользователя с машинами (JOIN FETCH) ===");
        User userWithCars = userService.getUserWithCars(userId);
        log.info("Получен пользователь {} c {} машинами", userWithCars.getId(), userWithCars.getCars().size());

        // 7) findUsersByCarModelAndSeries() (JOIN)
        log.info("=== Поиск пользователей по MODEL + SERIES (JOIN) ===");
        List<User> owners = userService.findUsersByCarModelAndSeries("BMW", 3);
        log.info("Владельцев BMW 3 серии: {}", owners.size());
        for (User u : owners) {
            log.info("Id владельца = {} email = {}", u.getId(), u.getEmail());
        }

        log.info("=== Получение всех машин (CarService) ===");
        List<Car> allCars = carService.listCars();
        log.info("Всего машин = {}", allCars.size());

        log.info("=== Поиск машин по модели ===");
        List<Car> bmwCars = carService.findByModel("BMW");
        log.info("Количество BMW = {}", bmwCars.size());

        log.info("=== Поиск машин по серии ===");
        List<Car> series3Cars = carService.findBySeries(3);
        log.info("Количество машин 3 серии = {}", series3Cars.size());

        log.info("=== Поиск машин по модели и серии ===");
        List<Car> bmw3Cars = carService.findByModelAndSeries("BMW", 3);
        log.info("BMW 3 найдено = {}", bmw3Cars.size());

        // 9) getCarById()
        log.info("=== Получение машины по id ===");
        Long carIdToCheck = userCars.get(0).getId();
        Car loadedCar = carService.getCarById(carIdToCheck);
        log.info("Получена машина: id = {}, model = {}", loadedCar.getId(), loadedCar.getModel());

        // 10) removeCarFromUser()
        log.info("=== Удаление машины у пользователя ===");
        Long carIdToRemove = userCars.get(1).getId();
        userService.removeCarFromUser(userId, carIdToRemove);
        log.info("Удалена машина carId = {} у пользователя userId = {}", carIdToRemove, userId);
        log.info("машин после удаления: {}", userService.listUserCars(userId).size());

        // 11) Проверка ошибок (NotFound / Validation) — чтобы показать, что работает
        log.info("=== Проверка исключений ===");

        try {
            userService.getUserById(999999L);
        } catch (NotFoundException e) {
            log.info("[OK] NotFoundException поймано: {}", e.getMessage());
        }

        try {
            userService.addCarToUser(userId, new Car("", 0));
        } catch (ValidationException e) {
            log.info("[OK] ValidationException поймано: {}", e.getMessage());
        }

        log.info("=== Удаление всех пользователей ===");
        List<User> list = userService.listUsers();
        list.forEach(user -> userService.deleteUserById(user.getId()));
        log.info("Осталось {} пользователей", userService.listUsers().size());
        log.info("Машин осталось {}", carService.listCars().size());

        context.close();
    }
}
