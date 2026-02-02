package spring.service;

import spring.model.Car;
import spring.model.User;
import spring.repository.UserRepository;
import spring.service.exception.NotFoundException;
import spring.service.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public User createUser(User user) {
        requireUserForCreate(user);

        log.debug("Создание пользователя: email = '{}', имя = '{}', фамилия = '{}'",
                safe(user.getEmail()), safe(user.getFirstName()), safe(user.getLastName()));

        User newUser = userRepository.save(user);

        log.info("Пользователь создан: id={}", newUser.getId());
        return newUser;
    }

    @Override
    public void updateUser(User user) {
        requireUserForUpdate(user);
        log.debug("Обновление пользователя: id = '{}', email = '{}', имя = '{}', фамилия = '{}'",
                safe(user.getId().toString()), safe(user.getEmail()), safe(user.getFirstName()), safe(user.getLastName()));

        userRepository.save(user);

        log.info("Пользователь обновлён: id = '{}'", user.getId());
    }

    @Transactional
    @Override
    public void addCarToUser(Long userId, Car car) {
        requireId(userId, "userId");
        requireCar(car);

        log.debug("addCarToUser: userId={}, brand='{}', model='{}', year={}, color='{}'",
                userId, safe(car.getBrand()), safe(car.getModel()), car.getYear(), safe(car.getColor()));

        User user = userRepository.findByIdWithCars(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь id=" + userId + " не найден"));

        car.setUser(user);
        user.getCars().add(car);

        log.info("Машина добавлена пользователю: userId={}, carsCount={}", userId, user.getCars().size());
    }

    @Transactional
    @Override
    public void deleteUserById(Long userId) {
        requireId(userId, "userId");
        log.debug("Удаление пользователя: userId = {}", userId);

        userRepository.deleteById(userId);

        log.info("Пользователь удалён: userId = {}", userId);
    }

    @Transactional(readOnly = true)
    @Override
    public User getUserById(Long userId) {
        requireId(userId, "userId");
        log.debug("Получение пользователя по id: userId = {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        log.info("Пользователь получен: userId = {}", userId);
        return user;
    }

    @Transactional(readOnly = true)
    @Override
    public User getUserByIdWithCars(Long userId) {
        requireId(userId, "userId");
        log.debug("Получение пользователя с машинами: userId = {}", userId);

        User user = userRepository.findByIdWithCars(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        if (user.getCars() == null) user.setCars(new java.util.ArrayList<>());

        log.info("Пользователь с машинами получен: userId = {}, машин = {}", userId, user.getCars().size());
        return user;
    }

    @Transactional
    @Override
    public void removeCarFromUser(Long userId, Long carId) {
        requireId(userId, "userId");
        requireId(carId, "carId");

        log.debug("removeCarFromUser: userId={}, carId={}", userId, carId);

        User user = userRepository.findByIdWithCars(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь id=" + userId + " не найден"));

        if (user.getCars() == null || user.getCars().isEmpty()) throw new NotFoundException("У пользователя id=" + userId + " нет машин");

        Car target = null;
        for (Car c : user.getCars()) {
            if (carId.equals(c.getId())) {
                target = c;
                break;
            }
        }

        if (target == null) throw new NotFoundException("У пользователя id=" + userId + " нет машины id=" + carId);

        log.debug("removeCarFromUser: найдено для удаления: carId={}, brand='{}', model='{}', year={}, color='{}'",
                target.getId(), safe(target.getBrand()), safe(target.getModel()), target.getYear(), safe(target.getColor()));

        user.getCars().remove(target);

        log.info("Машина удалена у пользователя: userId={}, carId={}", userId, carId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<User> findAllUsers() {
        log.debug("Получение списка всех пользователей");

        List<User> users = userRepository.findAll();

        log.info("Список всех пользователей получен");
        return users;
    }

    @Transactional(readOnly = true)
    @Override
    public List<User> searchUsersWithCars(String email,
                                          String firstName,
                                          String lastName,
                                          String brand,
                                          String model,
                                          Integer year,
                                          String color) {

        log.debug("Поиск пользователей. Фильтры: email='{}', имя='{}', фамилия='{}', brand='{}', model='{}', year={}, color='{}'",
                safe(email), safe(firstName), safe(lastName), safe(brand), safe(model), year, safe(color));

        if (year != null && year <= 0) throw new ValidationException("year должен быть > 0");

        boolean hasFilters =
                hasText(email) ||
                        hasText(firstName) ||
                        hasText(lastName) ||
                        hasText(brand) ||
                        hasText(model) ||
                        year != null ||
                        hasText(color);

        if (!hasFilters) {
            log.debug("Фильтры не заданы — возвращаю полный список пользователей.");
            return findAllUsers();
        }

        List<User> users = userRepository.searchWithCars(email, firstName, lastName, brand, model, year, color);

        log.info("Поиск пользователей завершён. Найдено: {}", (users == null ? 0 : users.size()));
        return users;
    }

    // --- Методы валидации ---

    private void requireUserForCreate(User user) {
        if (user == null) throw new ValidationException("пользователь не может быть null");
        requireNotBlank(user.getFirstName(), "user.firstName");
        requireNotBlank(user.getLastName(), "user.lastName");
        requireNotBlank(user.getEmail(), "user.email");
    }

    private void requireUserForUpdate(User user) {
        requireUserForCreate(user);
        requireId(user.getId(), "user.id");
    }

    private void requireCar(Car car) {
        if (car == null) throw new ValidationException("машина не может быть null");
        requireNotBlank(car.getModel(), "car.model");
        requireNotBlank(car.getBrand(), "car.brand");
        requireNotBlank(car.getColor(), "car.color");
        Integer year = car.getYear();
        if (year != null && year <= 0) throw new ValidationException("car.year должна быть > 0");
    }

    private void requireId(Long id, String field) {
        if (id == null) throw new ValidationException(field + " не должен быть null");
    }

    private void requireNotBlank(String value, String field) {
        if (value == null || value.trim().isEmpty()) throw new ValidationException(field + " не должен быть пустым");
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
