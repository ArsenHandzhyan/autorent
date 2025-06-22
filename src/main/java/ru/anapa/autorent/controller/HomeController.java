package ru.anapa.autorent.controller;

import ru.anapa.autorent.model.Category;
import ru.anapa.autorent.model.RentalItem;
import ru.anapa.autorent.model.Car;
import ru.anapa.autorent.service.CategoryService;
import ru.anapa.autorent.service.RentalItemService;
import ru.anapa.autorent.service.CarService;
import ru.anapa.autorent.service.UserService;
import ru.anapa.autorent.service.RentalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final CategoryService categoryService;
    private final RentalItemService rentalItemService;
    private final CarService carService;
    private final UserService userService;
    private final RentalService rentalService;

    @Autowired
    public HomeController(CategoryService categoryService, 
                         RentalItemService rentalItemService,
                         CarService carService,
                         UserService userService,
                         RentalService rentalService) {
        this.categoryService = categoryService;
        this.rentalItemService = rentalItemService;
        this.carService = carService;
        this.userService = userService;
        this.rentalService = rentalService;
    }

    @GetMapping("/")
    public String home(Model model) {
        try {
            // Получаем популярные категории
            List<Category> popularCategories = categoryService.findPopularCategories(6);

            // Получаем популярные предметы для проката
            List<RentalItem> popularItems = rentalItemService.findPopularItems(8);

            // Получаем новые поступления
            List<RentalItem> newItems = rentalItemService.findNewItems(4);

            // Получаем акционные предложения
            List<RentalItem> promotionalItems = rentalItemService.findPromotionalItems(4);

            // Получаем популярные автомобили (первые 6)
            List<Car> allCars = carService.findAllCars();
            List<Car> popularCars = allCars.stream()
                    .limit(6)
                    .collect(Collectors.toList());

            // Формируем список лет для фильтра (от 2020 до текущего года)
            List<Integer> years = new ArrayList<>();
            int currentYear = java.time.Year.now().getValue();
            for (int year = currentYear; year >= 2020; year--) {
                years.add(year);
            }

            // Формируем статистику
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCars", allCars.size());
            stats.put("totalUsers", userService.findAllUsers().size());
            stats.put("totalRentals", rentalService.findAllRentals().size());

            model.addAttribute("categories", popularCategories);
            model.addAttribute("popularItems", popularItems);
            model.addAttribute("newItems", newItems);
            model.addAttribute("promotionalItems", promotionalItems);
            model.addAttribute("popularCars", popularCars);
            model.addAttribute("years", years);
            model.addAttribute("stats", stats);

            return "home";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при получении данных главной страницы");
            return "error";
        }
    }
}