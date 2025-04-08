package ru.anapa.autorent.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.anapa.autorent.dto.CarDto;
import ru.anapa.autorent.model.Car;
import ru.anapa.autorent.service.CarService;

import java.util.List;

@Controller
@RequestMapping("/cars")
public class CarController {

    private final CarService carService;

    @Autowired
    public CarController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping
    public String listCars(Model model) {
        List<Car> cars = carService.findAllCars();
        model.addAttribute("cars", cars);
        return "cars/list";
    }

    @GetMapping("/available")
    public String listAvailableCars(Model model) {
        List<Car> availableCars = carService.findAvailableCars();
        model.addAttribute("cars", availableCars);
        return "cars/available";
    }

    @GetMapping("/{id}")
    public String viewCar(@PathVariable Long id, Model model) {
        Car car = carService.findCarById(id);
        model.addAttribute("car", car);
        return "cars/view";
    }

    @GetMapping("/search")
    public String searchCars(@RequestParam String brand, Model model) {
        List<Car> cars = carService.searchCarsByBrand(brand);
        model.addAttribute("cars", cars);
        model.addAttribute("searchTerm", brand);
        return "cars/search-results";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/add")
    public String showAddCarForm(Model model) {
        model.addAttribute("car", new CarDto());
        return "cars/add";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public String addCar(@Valid @ModelAttribute("car") CarDto carDto,
                         BindingResult result) {
        if (result.hasErrors()) {
            return "cars/add";
        }

        Car car = new Car();
        car.setBrand(carDto.getBrand());
        car.setModel(carDto.getModel());
        car.setYear(carDto.getYear());
        car.setLicensePlate(carDto.getLicensePlate());
        car.setDailyRate(carDto.getDailyRate());
        car.setImageUrl(carDto.getImageUrl());
        car.setDescription(carDto.getDescription());
        car.setAvailable(true);

        carService.saveCar(car);
        return "redirect:/cars";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/edit")
    public String showEditCarForm(@PathVariable Long id, Model model) {
        Car car = carService.findCarById(id);

        CarDto carDto = new CarDto();
        carDto.setBrand(car.getBrand());
        carDto.setModel(car.getModel());
        carDto.setYear(car.getYear());
        carDto.setLicensePlate(car.getLicensePlate());
        carDto.setDailyRate(car.getDailyRate());
        carDto.setImageUrl(car.getImageUrl());
        carDto.setDescription(car.getDescription());

        model.addAttribute("car", carDto);
        model.addAttribute("carId", id);
        return "cars/edit";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/edit")
    public String updateCar(@PathVariable Long id,
                            @Valid @ModelAttribute("car") CarDto carDto,
                            BindingResult result) {
        if (result.hasErrors()) {
            return "cars/edit";
        }

        Car car = carService.findCarById(id);
        car.setBrand(carDto.getBrand());
        car.setModel(carDto.getModel());
        car.setYear(carDto.getYear());
        car.setLicensePlate(carDto.getLicensePlate());
        car.setDailyRate(carDto.getDailyRate());
        car.setImageUrl(carDto.getImageUrl());
        car.setDescription(carDto.getDescription());

        carService.saveCar(car);
        return "redirect:/cars/" + id;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String deleteCar(@PathVariable Long id) {
        carService.deleteCar(id);
        return "redirect:/cars";
    }
}