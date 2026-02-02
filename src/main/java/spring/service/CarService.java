package spring.service;

import spring.model.Car;

import java.util.List;

public interface CarService {
    Car saveCar(Car car);
    Car getCarById(Long id);
    void deleteCarById(Long carId);
    List<Car> getAllCars();
    List<Car> getCarsLimited(Integer count);
    List<Car> findCarsByUserId(Long userId);
}
