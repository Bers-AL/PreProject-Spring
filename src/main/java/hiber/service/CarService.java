package hiber.service;

import hiber.model.Car;

import java.util.List;

public interface CarService {
    Car createCar(Car car);
    Car getCarById(Long id);
    Car updateCar(Car car);
    void deleteCarById(Long carId);
    List<Car> listCars();
    List<Car> findByModel(String model);
    List<Car> findBySeries(int series);
    List<Car> findByModelAndSeries(String model, int series);
    List<Car> findCarsByUserId(Long userId);
}
