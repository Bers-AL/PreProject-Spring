package hiber.dao;

import hiber.model.Car;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class CarDaoImpl implements CarDao {

    private final SessionFactory sessionFactory;

    public CarDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void add(Car car) {
        sessionFactory.getCurrentSession().save(car);
        log.debug("add: id = {}, model = {}, series = {}", car.getId(), car.getModel(), car.getSeries());
    }

    @Override
    public void update(Car car) {
        sessionFactory.getCurrentSession().update(car);
        log.debug("update: id = {}, model = {}, series = {}", car.getId(), car.getModel(), car.getSeries());
    }

    @Override
    public void delete(Car car) {
        sessionFactory.getCurrentSession().delete(car);
        log.debug("delete: id = {}", car.getId());
    }

    @Override
    public void deleteById(Long id) {
        boolean deleted = getById(id).map(
                car -> { sessionFactory.getCurrentSession().delete(car); return true; })
                .orElse(false);

        log.debug("deleteById: id = {}, deleted = {}", id, deleted);
    }

    @Override
    public Optional<Car> getById(Long id) {
        Car car = sessionFactory.getCurrentSession().get(Car.class, id);
        log.debug("getById: id = {}, found = {}", id, car != null);
        return Optional.ofNullable(car);
    }

    @Override
    public List<Car> findAll() {
        List<Car> cars = sessionFactory.getCurrentSession()
                .createQuery("from Car",
                        Car.class)
                .getResultList();
        log.debug("findAll cars: count = {}", cars.size());
        return cars;
    }

    @Override
    public List<Car> findByUserId(Long userId) {
        List<Car> cars = sessionFactory.getCurrentSession()
                .createQuery("from Car c " +
                        "where c.user.id = :userId",
                        Car.class)
                .setParameter("userId", userId)
                .getResultList();
        log.debug("findByUserId: userId = {}, count = {}", userId, cars.size());
        return cars;
    }

    @Override
    public List<Car> findByModel(String model) {
        List<Car> cars = sessionFactory.getCurrentSession()
                .createQuery("from Car c " +
                        "where c.model = :model",
                        Car.class)
                .setParameter("model", model)
                .getResultList();
        log.debug("findByModel: model = {}, count = {}", model, cars.size());
        return cars;
    }

    @Override
    public List<Car> findBySeries(int series) {
        List<Car> cars = sessionFactory.getCurrentSession()
                .createQuery("from Car c " +
                        "where c.series = :series",
                        Car.class)
                .setParameter("series", series)
                .getResultList();
        log.debug("findBySeries: series = {}, count = {}", series, cars.size());
        return cars;
    }

    @Override
    public List<Car> findByModelAndSeries(String model, int series) {
        List<Car> cars = sessionFactory.getCurrentSession()
                .createQuery("from Car c " +
                        "where c.model = :model " +
                        "and c.series = :series",
                        Car.class)
                .setParameter("model", model)
                .setParameter("series", series)
                .getResultList();
        log.debug("findByModelAndSeries: model = {}, series = {}, count = {}", model, series, cars.size());
        return cars;
    }
}
