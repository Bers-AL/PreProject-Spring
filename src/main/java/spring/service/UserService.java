package spring.service;

import spring.model.Car;
import spring.model.User;

import java.util.List;

public interface UserService {
    User createUser(User user);

    void updateUser(User user);
    void addCarToUser(Long userId, Car car);
    void deleteUserById(Long userId);
    User getUserById(Long userId);
    User getUserByIdWithCars(Long userId);
    List<User> findAllUsers();
    void removeCarFromUser(Long userId, Long carId);
    List<User> searchUsersWithCars(String email, String firstName, String lastName, String brand, String model, Integer year, String color);
}
