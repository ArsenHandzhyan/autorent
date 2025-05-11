package ru.anapa.autorent.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.anapa.autorent.dto.CarDto;
import ru.anapa.autorent.dto.CarSummaryDto;
import ru.anapa.autorent.dto.RentalPeriodDto;
import ru.anapa.autorent.model.Car;
import ru.anapa.autorent.service.CarService;
import ru.anapa.autorent.service.RentalService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @GetMapping("/available")
    public String listAvailableCars(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        // Используем пагинацию для оптимизации выборки
        Page<Car> carPage = carService.findAvailableCarsWithPagination(page, size);
        model.addAttribute("cars", carPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", carPage.getTotalPages());
        model.addAttribute("totalItems", carPage.getTotalElements());
        return "cars/available";
    }

    @GetMapping("/{id}")
    public String viewCar(@PathVariable Long id, Model model) {
        Car car = carService.findCarById(id);
        model.addAttribute("car", car);

        // Информация о доступности и периодах бронирования
        LocalDateTime nextAvailableDate = carService.getNextAvailableDate(id);
        List<RentalPeriodDto> bookedPeriods = carService.getBookedPeriods(id);

        model.addAttribute("nextAvailableDate", nextAvailableDate);
        model.addAttribute("bookedPeriods", bookedPeriods);

        return "cars/view";
    }

    @GetMapping
    public String listCars(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        // Используем оптимизированный метод с пагинацией и предварительно загруженными датами доступности
        Page<CarSummaryDto> carsPage = carService.findAllCarsWithAvailabilityPaginated(page, size);

        model.addAttribute("cars", carsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", carsPage.getTotalPages());
        model.addAttribute("totalItems", carsPage.getTotalElements());

        return "cars/list";
    }

    @GetMapping("/search")
    public String searchCars(
            @RequestParam String brand,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        // Используем оптимизированный метод поиска с пагинацией
        Page<Car> carPage = carService.searchCarsByBrandWithPagination(brand, page, size);

        model.addAttribute("cars", carPage.getContent());
        model.addAttribute("searchTerm", brand);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", carPage.getTotalPages());
        model.addAttribute("totalItems", carPage.getTotalElements());

        return "cars/search-results";
    }

    @GetMapping("/category/{category}")
    public String getCarsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        // Используем оптимизированный метод с пагинацией
        Page<Car> carPage = carService.findCarsByCategory(category, page, size);

        model.addAttribute("cars", carPage.getContent());
        model.addAttribute("categoryName", category);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", carPage.getTotalPages());
        model.addAttribute("totalItems", carPage.getTotalElements());

        return "cars/category";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/check-statuses")
    public String checkAndUpdateCarStatuses(RedirectAttributes redirectAttributes) {
        try {
            // Запускаем обновление статусов напрямую
            carService.checkAndUpdateCarStatuses();
            redirectAttributes.addFlashAttribute("success", "Статусы автомобилей обновлены");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении статусов: " + e.getMessage());
        }
        return "redirect:/cars";
    }

    // Остальные методы контроллера остаются без изменений

    // CRUD операции для админа
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
        if (carDto.getDailyRate() == null) {
            carDto.setDailyRate(BigDecimal.ZERO);
        }
        Car car = new Car();
        car.setBrand(carDto.getBrand());
        car.setModel(carDto.getModel());
        car.setYear(carDto.getYear());
        car.setLicensePlate(carDto.getLicensePlate());
        car.setDailyRate(carDto.getDailyRate());
        car.setImageUrl(carDto.getImageUrl());
        car.setDescription(carDto.getDescription());
        car.setRegistrationNumber(carDto.getLicensePlate());
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

        if (carDto.getDailyRate() == null) {
            carDto.setDailyRate(BigDecimal.ZERO);
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
        car.setRegistrationNumber(carDto.getLicensePlate());

        carService.updateCar(car);
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model modelAttribute) {

        // Логика фильтрации осталась прежней для сохранения функциональности
        List<Car> filteredCars = onlyAvailable ?
                carService.findAvailableCars() :
                carService.findAllCars();

        // Дополнительная фильтрация может быть реализована здесь

        modelAttribute.addAttribute("cars", filteredCars);
        modelAttribute.addAttribute("filterApplied", true);

        // Возвращаем параметры фильтра
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
}