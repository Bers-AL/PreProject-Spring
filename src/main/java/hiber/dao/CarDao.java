package hiber.dao;

import hiber.model.Car;

import java.util.List;
import java.util.Optional;

public interface CarDao {
    void add(Car car);
    void update(Car car);
    void delete(Car car);
    void deleteById(Long id);
    Optional<Car> getById(Long id);
    List<Car> findAll();
    List<Car> findByUserId(Long userId);
    List<Car> findByModel(String model);
    List<Car> findBySeries(int series);
    List<Car> findByModelAndSeries(String model, int series);
}