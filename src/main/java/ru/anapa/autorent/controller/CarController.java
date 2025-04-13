package ru.anapa.autorent.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.anapa.autorent.dto.CarDto;
import ru.anapa.autorent.model.Car;
import ru.anapa.autorent.service.CarService;
import ru.anapa.autorent.service.RentalService;

import java.util.List;

@Controller
@RequestMapping("/cars")
public class CarController {

    private final CarService carService;
    private final RentalService rentalService;

    @Autowired
    public CarController(CarService carService, RentalService rentalService) {
        this.carService = carService;
        this.rentalService = rentalService;
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

        if (carDto.getTransmission() != null) {
            car.setTransmission(carDto.getTransmission());
        }
        if (carDto.getFuelType() != null) {
            car.setFuelType(carDto.getFuelType());
        }
        if (carDto.getSeats() != null) {
            car.setSeats(carDto.getSeats());
        }
        if (carDto.getColor() != null) {
            car.setColor(carDto.getColor());
        }
        if (carDto.getCategory() != null) {
            car.setCategory(carDto.getCategory());
        }

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
        carDto.setTransmission(car.getTransmission());
        carDto.setFuelType(car.getFuelType());
        carDto.setSeats(car.getSeats());
        carDto.setColor(car.getColor());
        carDto.setCategory(car.getCategory());

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
        car.setTransmission(carDto.getTransmission());
        car.setFuelType(carDto.getFuelType());
        car.setSeats(carDto.getSeats());
        car.setColor(carDto.getColor());
        car.setCategory(carDto.getCategory());

        carService.saveCar(car);
        return "redirect:/cars/" + id;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String deleteCar(@PathVariable Long id) {
        carService.deleteCar(id);
        return "redirect:/cars";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/availability")
    public String toggleCarAvailability(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Car car = carService.findCarById(id);
            carService.updateCarAvailability(id, !car.isAvailable());

            String statusMessage = car.isAvailable() ?
                    "Автомобиль отмечен как недоступный" :
                    "Автомобиль отмечен как доступный";

            redirectAttributes.addFlashAttribute("success", statusMessage);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cars/" + id;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public String carsDashboard(Model model) {
        List<Car> allCars = carService.findAllCars();
        List<Car> availableCars = carService.findAvailableCars();

        model.addAttribute("allCars", allCars);
        model.addAttribute("availableCars", availableCars);
        model.addAttribute("totalCars", allCars.size());
        model.addAttribute("availableCount", availableCars.size());
        model.addAttribute("unavailableCount", allCars.size() - availableCars.size());

        return "admin/cars-dashboard";
    }

    @GetMapping("/category/{category}")
    public String getCarsByCategory(@PathVariable String category, Model model) {
        // Предполагается, что у вас есть метод в CarService для поиска по категории
        // Если нет, нужно его добавить
        List<Car> cars = carService.findAllCars().stream()
                .filter(car -> category.equalsIgnoreCase(car.getCategory()))
                .toList();

        model.addAttribute("cars", cars);
        model.addAttribute("categoryName", category);
        return "cars/category";
    }

    @GetMapping("/filter")
    public String filterCars(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Integer minYear,
            @RequestParam(required = false) Integer maxYear,
            @RequestParam(required = false) String transmission,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) Integer minSeats,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "false") boolean onlyAvailable,
            Model modelAttribute) {

        // Здесь должна быть логика фильтрации автомобилей
        // Для простоты просто возвращаем все автомобили или только доступные
        List<Car> filteredCars = onlyAvailable ?
                carService.findAvailableCars() :
                carService.findAllCars();

        // Дополнительная фильтрация может быть реализована здесь

        modelAttribute.addAttribute("cars", filteredCars);
        modelAttribute.addAttribute("filterApplied", true);

        // Возвращаем параметры фильтра для отображения в форме
        modelAttribute.addAttribute("brand", brand);
        modelAttribute.addAttribute("model", model);
        modelAttribute.addAttribute("minYear", minYear);
        modelAttribute.addAttribute("maxYear", maxYear);
        modelAttribute.addAttribute("transmission", transmission);
        modelAttribute.addAttribute("fuelType", fuelType);
        modelAttribute.addAttribute("minSeats", minSeats);
        modelAttribute.addAttribute("category", category);
        modelAttribute.addAttribute("onlyAvailable", onlyAvailable);

        return "cars/filtered";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/check-statuses")
    public String checkAndUpdateCarStatuses(RedirectAttributes redirectAttributes) {
        try {
            rentalService.synchronizeAllCarStatuses();
            redirectAttributes.addFlashAttribute("success", "Статусы автомобилей обновлены");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении статусов: " + e.getMessage());
        }
        return "redirect:/cars";
    }
}