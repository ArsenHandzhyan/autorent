package ru.anapa.autorent.controller;

import ru.anapa.autorent.model.Category;
import ru.anapa.autorent.model.RentalItem;
import ru.anapa.autorent.service.CategoryService;
import ru.anapa.autorent.service.RentalItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final CategoryService categoryService;
    private final RentalItemService rentalItemService;

    @Autowired
    public HomeController(CategoryService categoryService, RentalItemService rentalItemService) {
        this.categoryService = categoryService;
        this.rentalItemService = rentalItemService;
    }

    @GetMapping("/")
    public String home(Model model) {
        // Получаем популярные категории
        List<Category> popularCategories = categoryService.findPopularCategories(6);

        // Получаем популярные предметы для проката
        List<RentalItem> popularItems = rentalItemService.findPopularItems(8);

        // Получаем новые поступления
        List<RentalItem> newItems = rentalItemService.findNewItems(4);

        // Получаем акционные предложения
        List<RentalItem> promotionalItems = rentalItemService.findPromotionalItems(4);

        model.addAttribute("categories", popularCategories);
        model.addAttribute("popularItems", popularItems);
        model.addAttribute("newItems", newItems);
        model.addAttribute("promotionalItems", promotionalItems);

        return "home";
    }
}