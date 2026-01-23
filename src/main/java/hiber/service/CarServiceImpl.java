package hiber.service;

import hiber.dao.CarDao;
import hiber.dao.UserDao;
import hiber.model.Car;
import hiber.service.exception.NotFoundException;
import hiber.service.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class CarServiceImpl implements CarService {

    private final CarDao carDao;
    private final UserDao userDao;

    // --- читал что правильнее внедрять через конструктор и аннотация @Autowired не обязательна ---
    public CarServiceImpl(CarDao carDao, UserDao userDao) {
        this.carDao = carDao;
        this.userDao = userDao;
    }

    @Transactional
    @Override
    public Car createCar(Car car) {
        requireCar(car);
        log.info("Создание машины: модель = {}, серия = {}", car.getModel(), car.getSeries());
        carDao.add(car);
        log.info("Машина создана: id = {}", car.getId());
        return car;
    }

    @Transactional
    @Override
    public Car updateCar(Car car) {
        requireCar(car);
        requireId(car.getId(), "car.id");
        log.info("Обновление машины с id = {}", car.getId());
        carDao.update(car);
        log.info("Машина с id = {} обновлена", car.getId());
        return car;
    }

    @Transactional
    @Override
    public void deleteCarById(Long carId) {
        log.info("Удаление машины по id = {}", carId);
        requireId(carId, "carId");
        Car car = carDao.getById(carId)
                .orElseThrow(() -> new NotFoundException("Машина id = " + carId + " не найдена"));
        log.info("Машина для удаления по id = {} найдена, модель = {}, серия = {}", car.getId(), car.getModel(), car.getSeries());
        carDao.delete(car);
        log.info("Машина с id = {} удалена", car.getId());
    }

    @Transactional(readOnly = true)
    @Override
    public Car getCarById(Long carId) {
        log.info("Получение машины по id = {}", carId);
        requireId(carId, "carId");
        Car car = carDao.getById(carId)
                .orElseThrow(() -> new NotFoundException("Машина с id = " + carId + " не найдена"));
        log.info("Машина с id = {} получена", carId);
        return car;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Car> listCars() {
        log.info("Получение всех машин");
        List<Car> cars = carDao.findAll();
        log.info("Получено {} машин", cars.size());
        return cars;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Car> findByModel(String model) {
        requireNotBlank(model, "model");
        log.info("Получение машин по модели = {}", model);
        List<Car> cars = carDao.findByModel(model);
        log.info("Машин по модели = {} получено {} шт.", model, cars.size());
        return cars;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Car> findBySeries(int series) {
        log.info("Получение машин по серии = {}", series);
        if (series <= 0) throw new ValidationException("series должна быть > 0");
        List<Car> cars = carDao.findBySeries(series);
        log.info("Машин по серии = {} получено {} шт.", series, cars.size());
        return cars;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Car> findByModelAndSeries(String model, int series) {
        requireNotBlank(model, "model");
        log.info("Получение машин по модели {} и серии {}", model, series);
        if (series <= 0) throw new ValidationException("series должна быть > 0");
        List<Car> cars = carDao.findByModelAndSeries(model, series);
        log.info("Машин по модели {} и серии {}, получено {} шт.", model, series, cars.size());
        return cars;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Car> findCarsByUserId(Long userId) {
        requireId(userId, "userId");
        log.info("Получение всех машин пользователя с id = {}", userId);
        userDao.getById(userId)  // убеждаемся, что пользователь существует
                .orElseThrow(() -> new NotFoundException("пользователь с id = " + userId + " не найден"));

        List<Car> cars = carDao.findByUserId(userId);
        log.info("Машин пользователя с id = {}, получено {} шт.", userId, cars.size());
        return cars;
    }

    // --- Методы валидации ---
    private void requireCar(Car car) {
        if (car == null) throw new ValidationException("Машина не может быть null");
        requireNotBlank(car.getModel(), "car.model");
        if (car.getSeries() <= 0) throw new ValidationException("series должна быть > 0");
    }

    private void requireId(Long id, String field) {
        if (id == null) throw new ValidationException(field + " не может быть null");
    }

    private void requireNotBlank(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(field + " не может быть пустым");
        }
    }
}
