package spring.repository;

import spring.model.Car;

import java.util.List;
import java.util.Optional;

public interface CarRepository {
    Car save(Car car);
    void deleteById(Long carId);
    Optional<Car> findById(Long carId);
    List<Car> findAll();
    List<Car> findTop(int limit);
    List<Car> findAllByUserId(Long userId);
}