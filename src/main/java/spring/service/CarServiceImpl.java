package spring.service;

import spring.model.Car;
import spring.repository.CarRepository;
import spring.repository.UserRepository;
import spring.service.exception.NotFoundException;
import spring.service.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final UserRepository userRepository;

    public CarServiceImpl(CarRepository carRepository, UserRepository userRepository) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public Car saveCar(Car car) {
        requireCar(car);
        log.debug("Создание машины: бренд = {}, модель = {}, год = {}, цвет = {}", car.getBrand(), car.getModel(), car.getYear(), car.getColor());

        carRepository.save(car);

        log.info("Машина создана: id = {}", car.getId());
        return car;
    }

    @Transactional
    @Override
    public void deleteCarById(Long carId) {
        requireId(carId, "carId");
        log.debug("Удаление машины: carId={}", carId);

        carRepository.deleteById(carId);

        log.info("Машина удалена: carId={}", carId);
    }


    @Transactional(readOnly = true)
    @Override
    public Car getCarById(Long carId) {
        log.debug("Получение машины по id = {}", carId);
        requireId(carId, "carId");

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new NotFoundException("Машина с id = " + carId + " не найдена"));

        log.info("Машина с id = {} получена", carId);
        return car;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Car> getAllCars() {
        log.debug("Получение всех машин");

        List<Car> cars = carRepository.findAll();

        log.info("Получено {} машин", cars.size());
        return cars;
    }


    @Transactional(readOnly = true)
    @Override
    public List<Car> getCarsLimited(Integer count) {
        if (count == null || count <= 0 || count >= 5) {
            List<Car> cars = carRepository.findAll();
            if (count == null) log.info("getCarsLimited: count=null -> отдаю ВСЕ: {}", cars.size());
            else if (count <= 0) log.info("getCarsLimited: некорректный count={} (<=0) -> отдаю ВСЕ: {}", count, cars.size());
            else log.info("getCarsLimited: count={} (>=5) -> отдаю ВСЕ: {}", count, cars.size());
            return cars;
        }
        List<Car> cars = carRepository.findTop(count);

        log.info("getCarsLimited: count={} (1..4) -> отдаю {} (из запрошенных {})", count, cars.size(), count);
        return cars;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Car> findCarsByUserId(Long userId) {
        requireId(userId, "userId");
        log.debug("Получение всех машин пользователя с id = {}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("пользователь с id = " + userId + " не найден"));
        List<Car> cars = carRepository.findAllByUserId(userId);

        log.info("Список машин пользователя получен: userId = {}, машин = {}", userId, (cars == null ? 0 : cars.size()));
        return cars;
    }

    // --- Методы валидации ---
    private void requireCar(Car car) {
        if (car == null) throw new ValidationException("Машина не может быть null");
        requireNotBlank(car.getBrand(), "car.brand");
        requireNotBlank(car.getModel(), "car.model");
        if (car.getYear() <= 0) throw new ValidationException("year должна быть > 0");
    }

    private void requireId(Long id, String field) {
        if (id == null) throw new ValidationException(field + " не может быть null");
    }

    private void requireNotBlank(String value, String field) {
        if (value == null || value.trim().isEmpty()) throw new ValidationException(field + " не может быть пустым");
    }
}
