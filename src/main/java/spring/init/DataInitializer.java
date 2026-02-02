package spring.init;

import spring.model.Car;
import spring.model.User;
import spring.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserService userService;

    @PostConstruct
    public void init() {
        log.info("DataInitializer: init start");
        initData();
        log.info("DataInitializer: init done");
    }

    @Transactional
    public void initData() {
        List<User> users = userService.findAllUsers();
        log.info("сейчас в бд есть " + users.size() + " пользователей");
        if (users.size() == 9) {
            log.info("Инициализация пропущена: users уже заполнены");
            return;
        }

        log.info("Инициализация запущена");

        User alice = new User("Alice", "Smith", "alice@example.com");
        User bob = new User("Bob", "Brown", "bob@example.com");
        User charlie = new User("Charlie", "Johnson", "charlie@example.com");
        User dave = new User("Dave", "Jones", "dave@example.com");
        User eve = new User("Eve", "Williams", "eve@example.com");
        User frank = new User("Frank", "Taylor", "frank@example.com");

        userService.createUser(alice);
        userService.createUser(bob);
        userService.createUser(charlie);
        userService.createUser(dave);
        userService.createUser(eve);
        userService.createUser(frank);

        // 7 уникальных машин и 2 пары одинаковые (BMW M3 2018 и Audi A4 2016) распределены между пользователями
        userService.addCarToUser(alice.getId(), new Car("BMW", "M3", 2018, "#ff3b30"));
        userService.addCarToUser(alice.getId(), new Car("Audi", "A4", 2016, "#1e90ff"));
        userService.addCarToUser(alice.getId(), new Car("Toyota", "Camry", 2020, "#34c759"));

        userService.addCarToUser(bob.getId(), new Car("BMW", "M3", 2018, "#ff3b30"));
        userService.addCarToUser(bob.getId(), new Car("Mercedes-Benz", "C200", 2017, "#ffd60a"));
        userService.addCarToUser(bob.getId(), new Car("Honda", "Civic", 2015, "#ffffff"));

        userService.addCarToUser(charlie.getId(), new Car("Hyundai", "Palisade", 2025, "#dd0000"));

        userService.addCarToUser(eve.getId(), new Car("Audi", "A4", 2016, "#1e90ff"));

        userService.addCarToUser(frank.getId(), new Car("Nissan", "Teana", 2020, "#154032"));

        log.info("Демо пользователи и машины успешно добавлены");
    }
}