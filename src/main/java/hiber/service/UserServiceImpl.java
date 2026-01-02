package hiber.service;

import hiber.dao.CarDao;
import hiber.dao.UserDao;
import hiber.model.Car;
import hiber.model.User;
import hiber.service.exception.NotFoundException;
import hiber.service.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

   @Autowired
   private UserDao userDao;

   // --- здесь внедрил по аналогии репозитория)
   @Autowired
   private CarDao carDao;

   @Transactional
   @Override
   public void add(User user) {
      userDao.add(user);
   }

   @Transactional
   @Override
   public User createUser(User user) {
      requireUserForCreate(user);
      log.info("Создание пользователя: email = {}, имя = {}, фамилия = {}",
              user.getEmail(), user.getFirstName(), user.getLastName());
      userDao.add(user);
      log.info("Пользователь создан: id = {}", user.getId());
      return user;
   }

   @Transactional
   @Override
   public User updateUser(User user) {
      requireUserForUpdate(user);
      log.info("Обновление пользователя : id = {}, email = {}", user.getId(), user.getEmail());
      userDao.update(user);
      log.info("Пользователь с id = {} обновлен", user.getId());
      return user;
   }

   @Transactional
   @Override
   public void deleteUserById(Long userId) {
      log.info("Удаление пользователя по id = {}", userId);
      requireId(userId, "userId");

      // чтобы удалить “честно” сначала проверяем
      User existing = userDao.getById(userId)
              .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

      userDao.delete(existing);
      log.info("Пользователь с id = {} удален", userId);
   }

   @Transactional(readOnly = true)
   @Override
   public User getUserById(Long userId) {
      log.info("Получение пользователя по id = {}", userId);
      requireId(userId, "userId");
      User user = userDao.getById(userId)
              .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
      log.info("Пользователь с id = {} получен", userId);
      return user;
   }

   @Transactional(readOnly = true)
   @Override
   public User getUserWithCars(Long userId) {
      log.info("Получение пользователя с машинами по id = {}", userId);
      requireId(userId, "userId");

      User user = userDao.getByIdWithCars(userId)
              .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

      int count = user.getCars() == null ? 0 : user.getCars().size();
      log.info("Пользователь с машинами получен: userId = {}, количество машин = {} шт.", userId, count);

      return user;
   }

   @Transactional(readOnly = true)
   @Override
   public User getUserByEmail(String email) {
      log.info("Получение пользователя по email = {}", email);
      requireNotBlank(email, "email");

      return userDao.findByEmail(email)
              .orElseThrow(() -> new NotFoundException("Пользователь по email = " + email + " не найден"));
   }

   // --- Cars inside user ---

   @Transactional
   @Override
   public Car addCarToUser(Long userId, Car car) {
      requireId(userId, "userId");
      requireCar(car);
      log.info("Добавление машины к пользователю: userId = {}, модель = {}, серия = {}",
              userId, car.getModel(), car.getSeries());

      User user = userDao.getById(userId)
              .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

      // важно: правильно держать обе стороны связи
      user.addCar(car);

      // cascade=ALL на User.cars => сохранятся и машины
      userDao.update(user);

      log.info("Машина модель = {}, серия = {} добавлена к пользователю: userId = {}",
              car.getModel(), car.getSeries(), userId);

      return car;
   }

   @Transactional
   @Override
   public void removeCarFromUser(Long userId, Long carId) {
      log.info("Удаление машины с id = {} у пользователя с id = {}", carId, userId);
      requireId(userId, "userId");
      requireId(carId, "carId");

      // 1) получаем машину
      Car car = carDao.getById(carId)
              .orElseThrow(() -> new NotFoundException("Машина с id = " + carId + " не найдена"));

      // 2) проверяем принадлежность пользователю (без getByIdAndUserId — как ты хотел)
      if (car.getUser() == null || car.getUser().getId() == null || !car.getUser().getId().equals(userId)) {
         throw new NotFoundException("Машина с id = " + carId + " не найдена у пользователя с id = " + userId);
      }

      // 3) удаляем
      carDao.delete(car);

      log.info("Машина с id = {} удалена у пользователя с id = {}", carId, userId);
   }

   @Transactional(readOnly = true)
   @Override
   public List<Car> listUserCars(Long userId) {
      log.info("Получение машин пользователя с id = {}", userId);
      requireId(userId, "userId");

      // если хочешь строго: проверить, что пользователь существует
      userDao.getById(userId)
              .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

      List<Car> cars = carDao.findByUserId(userId);
      log.info("Машины пользователя с id = {} получены, машин {}шт.", userId, cars.size());
      return cars;
   }

   // --- JOIN query ---

   @Transactional(readOnly = true)
   @Override
   public List<User> findUsersByCarModelAndSeries(String model, int series) {
      requireNotBlank(model, "model");
      if (series <= 0) throw new ValidationException("series должна быть > 0");

      List<User> users = userDao.findUsersByCarModelAndSeries(model, series);
      log.info("Найдено пользователей {} по модели = {} и серии = {}", users.size(), model, series );
      return users;
   }

   @Transactional(readOnly = true)
   @Override
   public List<User> listUsers() {
      return userDao.listUsers();
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
      if (car.getSeries() <= 0) throw new ValidationException("car.series должна быть > 0");
   }

   private void requireId(Long id, String field) {
      if (id == null) throw new ValidationException(field + " не должен быть null");
   }

   private void requireNotBlank(String value, String field) {
      if (value == null || value.trim().isEmpty()) {
         throw new ValidationException(field + " не должен  быть пустым");
      }
   }

}
