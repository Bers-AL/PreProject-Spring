package spring.repository;

import spring.model.Car;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class CarRepositoryImpl implements CarRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Car save(Car car) {
        boolean isNew = (car.getId() == null);
        Car managed;

        if (isNew) {
            em.persist(car);
            managed = car;
        } else {
            managed = em.merge(car);
        }

        log.debug("Car {}: id={}, brand='{}', model='{}', year={}, color='{}'",
                isNew ? "created" : "updated", managed.getId(), managed.getBrand(), managed.getModel(), managed.getYear(), managed.getColor());
        return managed;
    }

    @Override
    public void deleteById(Long carId) {
        Car car = em.find(Car.class, carId);
        if (car == null) {
            log.debug("deleteById: carId={} not found", carId);
            return;
        }
        em.remove(car);
        log.debug("deleteById: deleted carId={}", carId);
    }

    @Override
    public Optional<Car> findById(Long carId) {
        Car car = em.find(Car.class, carId);
        log.debug("findById: id = {}, found = {}", carId, car != null);
        return Optional.ofNullable(car);
    }

    @Override
    public List<Car> findAll() {
        List<Car> cars = em.createQuery("select c from Car c", Car.class)
                .getResultList();
        log.debug("findAll cars: count = {}", cars.size());
        return cars;
    }

    @Override
    public List<Car> findTop(int limit) {
        List<Car> list = em.createQuery("select c from Car c order by c.id", Car.class)
                .setMaxResults(limit)
                .getResultList();
        log.debug("findTop: limit = {}, count = {}", limit, list.size());
        return list;
    }

    @Override
    public List<Car> findAllByUserId(Long userId) {
        List<Car> cars = em.createQuery(
                        "select c from Car c where c.user.id = :userId", Car.class)
                .setParameter("userId", userId)
                .getResultList();
        log.debug("findAllByUserId: userId = {}, count = {}", userId, cars.size());
        return cars;
    }
}
