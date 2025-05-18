package ru.anapa.autorent.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
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
import java.util.Map;
import java.util.HashMap;

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
    public ModelAndView listAvailableCars(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Car> carPage = carService.findAvailableCarsWithPagination(page, size);
            ModelAndView mav = new ModelAndView("cars/available");
            mav.addObject("cars", carPage.getContent());
            mav.addObject("currentPage", page);
            mav.addObject("totalPages", carPage.getTotalPages());
            mav.addObject("totalItems", carPage.getTotalElements());
            return mav;
        } catch (Exception e) {
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("error", "Ошибка при получении списка доступных автомобилей");
            return mav;
        }
    }

    @GetMapping("/{id}")
    public ModelAndView viewCar(@PathVariable Long id) {
        try {
            Car car = carService.findCarById(id);
            LocalDateTime nextAvailableDate = carService.getNextAvailableDate(id);
            List<RentalPeriodDto> bookedPeriods = carService.getBookedPeriods(id);

            ModelAndView mav = new ModelAndView("cars/view");
            mav.addObject("car", car);
            mav.addObject("nextDate", nextAvailableDate);
            mav.addObject("bookedPeriods", bookedPeriods);
            return mav;
        } catch (Exception e) {
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("error", "Автомобиль не найден");
            return mav;
        }
    }

    @GetMapping
    public ModelAndView listCars(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<CarSummaryDto> carsPage = carService.findAllCarsWithAvailabilityPaginated(page, size);
            ModelAndView mav = new ModelAndView("cars/list");
            mav.addObject("cars", carsPage.getContent());
            mav.addObject("currentPage", page);
            mav.addObject("totalPages", carsPage.getTotalPages());
            mav.addObject("totalItems", carsPage.getTotalElements());
            return mav;
        } catch (Exception e) {
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("error", "Ошибка при получении списка автомобилей");
            return mav;
        }
    }

    @GetMapping("/search")
    public ModelAndView searchCars(
            @RequestParam String brand,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Car> carPage = carService.searchCarsByBrandWithPagination(brand, page, size);
            ModelAndView mav = new ModelAndView("cars/search-results");
            mav.addObject("cars", carPage.getContent());
            mav.addObject("searchTerm", brand);
            mav.addObject("currentPage", page);
            mav.addObject("totalPages", carPage.getTotalPages());
            mav.addObject("totalItems", carPage.getTotalElements());
            return mav;
        } catch (Exception e) {
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("error", "Ошибка при поиске автомобилей");
            return mav;
        }
    }

    @GetMapping("/category/{category}")
    public ModelAndView getCarsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Car> carPage = carService.findCarsByCategory(category, page, size);
            ModelAndView mav = new ModelAndView("cars/category");
            mav.addObject("cars", carPage.getContent());
            mav.addObject("categoryName", category);
            mav.addObject("currentPage", page);
            mav.addObject("totalPages", carPage.getTotalPages());
            mav.addObject("totalItems", carPage.getTotalElements());
            return mav;
        } catch (Exception e) {
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("error", "Ошибка при получении автомобилей категории");
            return mav;
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/add")
    public ModelAndView showAddCarForm() {
        ModelAndView mav = new ModelAndView("cars/add");
        mav.addObject("car", new CarDto());
        return mav;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ModelAndView addCar(@Valid @ModelAttribute("car") CarDto carDto,
                             BindingResult result) {
        if (result.hasErrors()) {
            ModelAndView mav = new ModelAndView("cars/add");
            mav.addObject("car", carDto);
            return mav;
        }

        try {
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

            Car savedCar = carService.saveCar(car);
            ModelAndView mav = new ModelAndView("redirect:/cars/" + savedCar.getId());
            mav.addObject("success", "Автомобиль успешно добавлен");
            return mav;
        } catch (Exception e) {
            ModelAndView mav = new ModelAndView("cars/add");
            mav.addObject("car", carDto);
            mav.addObject("error", "Ошибка при добавлении автомобиля");
            return mav;
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/edit")
    public ModelAndView showEditCarForm(@PathVariable Long id) {
        try {
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

            ModelAndView mav = new ModelAndView("cars/edit");
            mav.addObject("car", carDto);
            mav.addObject("carId", id);
            return mav;
        } catch (Exception e) {
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("error", "Автомобиль не найден");
            return mav;
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/edit")
    public ModelAndView updateCar(@PathVariable Long id,
                                @Valid @ModelAttribute("car") CarDto carDto,
                                BindingResult result) {
        if (result.hasErrors()) {
            ModelAndView mav = new ModelAndView("cars/edit");
            mav.addObject("car", carDto);
            mav.addObject("carId", id);
            return mav;
        }

        try {
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

            Car updatedCar = carService.updateCar(car);
            ModelAndView mav = new ModelAndView("redirect:/cars/" + updatedCar.getId());
            mav.addObject("success", "Автомобиль успешно обновлен");
            return mav;
        } catch (Exception e) {
            ModelAndView mav = new ModelAndView("cars/edit");
            mav.addObject("car", carDto);
            mav.addObject("carId", id);
            mav.addObject("error", "Ошибка при обновлении автомобиля");
            return mav;
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public ModelAndView carsDashboard() {
        try {
            List<Car> allCars = carService.findAllCars();
            List<Car> availableCars = carService.findAvailableCars();

            ModelAndView mav = new ModelAndView("admin/cars-dashboard");
            mav.addObject("allCars", allCars);
            mav.addObject("availableCars", availableCars);
            mav.addObject("totalCars", allCars.size());
            mav.addObject("availableCount", availableCars.size());
            mav.addObject("unavailableCount", allCars.size() - availableCars.size());
            return mav;
        } catch (Exception e) {
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("error", "Ошибка при получении данных дашборда");
            return mav;
        }
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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public ResponseEntity<?> deleteCar(@PathVariable Long id) {
        try {
            carService.deleteCar(id);
            return ResponseEntity.ok(Map.of("message", "Автомобиль успешно удален"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при удалении автомобиля"));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/availability")
    public ResponseEntity<?> toggleCarAvailability(@PathVariable Long id) {
        try {
            Car car = carService.findCarById(id);
            carService.updateCarAvailability(id, !car.isAvailable());

            String statusMessage = car.isAvailable() ?
                    "Автомобиль отмечен как недоступный" :
                    "Автомобиль отмечен как доступный";

            return ResponseEntity.ok(Map.of("message", statusMessage));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при изменении статуса доступности автомобиля"));
        }
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