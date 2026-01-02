package hiber.dao;

import hiber.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    void add(User user);
    List<User> listUsers();
    void update(User user);
    void delete(User user);
    void deleteById(Long id);
    Optional<User> getById(Long id);
    Optional<User> findByEmail(String email);
    List<User> findUsersByCarModelAndSeries(String model, int series);
    Optional<User> getByIdWithCars(Long id);

}
