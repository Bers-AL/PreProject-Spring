package hiber.dao;

import hiber.model.User;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class UserDaoImpl implements UserDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void add(User user) {
        sessionFactory.getCurrentSession().save(user);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<User> listUsers() {
        TypedQuery<User> query = sessionFactory.getCurrentSession().createQuery("from User");
        return query.getResultList();
    }

    @Override
    public void update(User user) {
        sessionFactory.getCurrentSession().update(user);
        log.debug("User updated: id = {}, email = {}", user.getId(), user.getEmail());
    }

    @Override
    public void delete(User user) {
        sessionFactory.getCurrentSession().delete(user);
        log.debug("User deleted: id = {}", user.getId());
    }

    @Override
    public void deleteById(Long id) {
        getById(id).ifPresent(u -> {
            sessionFactory.getCurrentSession().delete(u);
            log.debug("User deletedById: id = {}", id);
        });
    }

    @Override
    public Optional<User> getById(Long id) {
        User user = sessionFactory.getCurrentSession().get(User.class, id);
        log.debug("getById: id = {}, found = {}", id, user != null);
        return Optional.ofNullable(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        TypedQuery<User> q = sessionFactory.getCurrentSession()
                .createQuery("from User u " +
                        "where u.email = :email",
                        User.class)
                .setParameter("email", email);

        List<User> res = q.getResultList();
        log.debug("findByEmail: email = {}, found = {}", email, !res.isEmpty());
        return res.isEmpty() ? Optional.empty() : Optional.of(res.get(0));
    }

    @Override
    public List<User> findUsersByCarModelAndSeries(String model, int series) {
        List<User> users = sessionFactory.getCurrentSession()
                .createQuery(
                        "select distinct u from User u " +  // distinct чтобы не было дублей пользователей
                                "join u.cars c " +
                                "where c.model = :model " +
                                "and c.series = :series",
                        User.class
                )
                .setParameter("model", model)
                .setParameter("series", series)
                .getResultList();

        log.debug("findUsersByCarModelAndSeries: model = {}, series = {}, count = {}", model, series, users.size());
        return users;
    }

    // получение сразу с машинами
    @Override
    public Optional<User> getByIdWithCars(Long id) {
        TypedQuery<User> q = sessionFactory.getCurrentSession()
                .createQuery(
                        "select distinct u from User u " +
                                "left join fetch u.cars " +
                                "where u.id = :id",
                        User.class
                )
                .setParameter("id", id);

        List<User> res = q.getResultList();
        User user = res.isEmpty() ? null : res.get(0);

        log.debug("getByIdWithCars: id = {}, found = {}", id, user != null);
        return res.isEmpty() ? Optional.empty() : Optional.of(res.get(0));
    }
}
