package hiber.service;

import hiber.model.Car;
import hiber.model.User;

import java.util.List;

public interface UserService {
    void add(User user);
    List<User> listUsers();
    User createUser(User user);
    User updateUser(User user);
    void deleteUserById(Long userId);
    User getUserById(Long userId);
    User getUserWithCars(Long userId);
    User getUserByEmail(String email);
    Car addCarToUser(Long userId, Car car);
    void removeCarFromUser(Long userId, Long carId);
    List<Car> listUserCars(Long userId);
    List<User> findUsersByCarModelAndSeries(String model, int series);
}
