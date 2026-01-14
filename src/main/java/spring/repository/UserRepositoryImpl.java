package spring.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import spring.model.User;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class UserRepositoryImpl implements UserRepository {


    @PersistenceContext
    private EntityManager em;

    @Override
    public User save(User user) {
        boolean isNew = (user.getId() == null);

        User managed;
        if (isNew) {
            em.persist(user);
            managed = user; // persist делает объект managed
        } else {
            managed = em.merge(user);
        }

        log.debug("User {}: id = {}, firstName = {}, lastName = {}, email = {}",
                isNew ? "created" : "updated", managed.getId(), managed.getFirstName(), managed.getLastName(), managed.getEmail());

        return managed;
    }

    @Override
    public void deleteById(Long id) {
        User user = em.find(User.class, id);
        if (user == null) {
            log.debug("deleteById: id = {}, not found", id);
            return;
        }
        em.remove(user);
        log.debug("User deleted: id = {}", id);
        return;
    }

    @Override
    public Optional<User> findById(Long id) {
        User user = em.find(User.class, id);
        log.debug("findById: id = {}, found = {}", id, user != null);
        return Optional.ofNullable(user);
    }

    @Override
    public List<User> findAll() {
        List<User> users = em.createQuery("select u from User u", User.class).getResultList();
        log.debug("findAll: count = {}", users.size());
        return users;
    }

    @Override
    public List<User> searchWithCars(String email, String firstName, String lastName,
                                     String brand, String model, Integer year, String color) {

        StringBuilder jpql = new StringBuilder(
                "select distinct u from User u left join fetch u.cars c where 1=1"
        );

        // LIKE-фильтры: paramName -> "поле в JPQL"
        var likeFilters = new java.util.LinkedHashMap<String, String>();
        likeFilters.put("email", "u.email");
        likeFilters.put("firstName", "u.firstName");
        likeFilters.put("lastName", "u.lastName");
        likeFilters.put("brand", "c.brand");
        likeFilters.put("model", "c.model");
        likeFilters.put("color", "c.color");

        // значения: paramName -> value
        var values = new java.util.HashMap<String, String>();
        values.put("email", email);
        values.put("firstName", firstName);
        values.put("lastName", lastName);
        values.put("brand", brand);
        values.put("model", model);
        values.put("color", color);

        for (var e : likeFilters.entrySet()) {
            String param = e.getKey();
            String field = e.getValue();
            String value = values.get(param);

            if (hasText(value)) {
                jpql.append(" and lower(").append(field).append(") like :").append(param);
            }
        }

        if (year != null && year > 0) {
            jpql.append(" and c.year = :year");
        }

        TypedQuery<User> query = em.createQuery(jpql.toString(), User.class);

        for (var param : likeFilters.keySet()) {
            String value = values.get(param);
            if (hasText(value)) {
                query.setParameter(param, "%" + value.toLowerCase().trim() + "%");
            }
        }

        if (year != null && year > 0) {
            query.setParameter("year", year);
        }
        List<User> result = query.getResultList();

        log.debug("searchWithCars: count = {}", result.size());
        return result;
    }

    // получение сразу с машинами
    @Override
    public Optional<User> findByIdWithCars(Long id) {
        List<User> res = em.createQuery(
                        "select distinct u from User u " +
                                "left join fetch u.cars " +
                                "where u.id = :id",
                        User.class
                )
                .setParameter("id", id)
                .getResultList();

        User user = res.isEmpty() ? null : res.get(0);
        log.debug("findByIdWithCars: id = {}, found = {}", id, user != null);
        return Optional.ofNullable(user);
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
