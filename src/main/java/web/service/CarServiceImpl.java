package web.service;

import org.springframework.stereotype.Service;
import web.model.Car;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CarServiceImpl implements CarService {

    private final List<Car> cars;
    private static final int MAX_COUNT = 5;

    public CarServiceImpl() {
        List<Car> list = new ArrayList<>();
        list.add(new Car("BMW", "3 Series", 2020));
        list.add(new Car("Audi", "A6", 2019));
        list.add(new Car("Mercedes", "C-Class", 2021));
        list.add(new Car("Volkswagen", "Golf", 2018));
        list.add(new Car("Toyota", "Camry", 2022));
        list.add(new Car("Chevrolet", "Cruze", 2014));
        this.cars = Collections.unmodifiableList(list);
    }

    @Override
    public List<Car> getCars(Integer count) {
        if (count == null || count >= MAX_COUNT) return cars;
        if (count <= 0) return List.of();

        return cars.subList(0, Math.min(count, cars.size()));
    }
}
