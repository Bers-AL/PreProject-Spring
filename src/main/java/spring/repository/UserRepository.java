package spring.repository;

import spring.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    void deleteById(Long id);
    List<User> findAll();
    Optional<User> findById(Long id);
    Optional<User> findByIdWithCars(Long id);
    List<User> searchWithCars(String email, String firstName, String lastName, String brand, String model, Integer year, String color);
}
