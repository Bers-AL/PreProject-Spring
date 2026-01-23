package spring.controller;

import spring.service.CarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CarsController {

    private final CarService carService;

    @GetMapping("/cars")
    public String getCars(@RequestParam(value = "count", required = false) Integer count, Model model) {

        if (count == null) log.debug("Открытие страницы машин. Параметр count не задан.");
        else log.debug("Открытие страницы машин. Параметр count = {}.", count);

        var cars = carService.getCarsLimited(count);

        log.debug("Страница машин подготовлена. Возвращено записей: {}.", (cars == null ? 0 : cars.size()));

        model.addAttribute("cars", cars);

        return "cars";
    }
}
