package ru.anapa.autorent.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.anapa.autorent.dto.CarDto;
import ru.anapa.autorent.dto.CarImageDto;
import ru.anapa.autorent.dto.CarSummaryDto;
import ru.anapa.autorent.dto.RentalPeriodDto;
import ru.anapa.autorent.model.Car;
import ru.anapa.autorent.model.CarImage;
import ru.anapa.autorent.service.CarService;
import ru.anapa.autorent.service.RentalService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Controller
@RequestMapping("/cars")
public class CarController {

    private static final Logger logger = LoggerFactory.getLogger(CarController.class);

    private final CarService carService;
    private final RentalService rentalService;

    @Autowired
    public CarController(CarService carService, RentalService rentalService) {
        this.carService = carService;
        this.rentalService = rentalService;
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
            if (carDto.getSchedule() != null) {
                car.setSchedule(carDto.getSchedule());
            }

            // Обработка загруженных изображений
            if (carDto.getNewImages() != null && !carDto.getNewImages().isEmpty()) {
                List<CarImage> images = new ArrayList<>();
                for (int i = 0; i < carDto.getNewImages().size(); i++) {
                    MultipartFile file = carDto.getNewImages().get(i);
                    if (!file.isEmpty()) {
                        CarImage image = new CarImage();
                        String imageUrl = carService.saveCarImage(file);
                        image.setImageUrl(imageUrl);
                        image.setMain(i == 0); // Первое изображение - основное
                        image.setCar(car);
                        images.add(image);
                    }
                }
                car.setImages(images);
            }

            Car savedCar = carService.saveCar(car);
            ModelAndView mav = new ModelAndView("redirect:/cars/" + savedCar.getId());
            mav.addObject("success", "Автомобиль успешно добавлен");
            return mav;
        } catch (Exception e) {
            ModelAndView mav = new ModelAndView("cars/add");
            mav.addObject("car", carDto);
            mav.addObject("error", "Ошибка при добавлении автомобиля: " + e.getMessage());
            return mav;
        }
    }

    @GetMapping("/{id}")
    public ModelAndView viewCar(@PathVariable Long id) {
        try {
            Car car = carService.findCarById(id);
            LocalDateTime nextAvailableDate = carService.getNextAvailableDate(id);
            List<RentalPeriodDto> bookedPeriods = carService.getBookedPeriods(id);

            logger.debug("Loading car details for ID: {}", id);
            logger.debug("Car images count: {}", car.getImages() != null ? car.getImages().size() : 0);
            if (car.getImages() != null) {
                car.getImages().forEach(img -> 
                    logger.debug("Image: id={}, url={}, main={}", img.getId(), img.getImageUrl(), img.isMain())
                );
            }

            ModelAndView mav = new ModelAndView("cars/view");
            mav.addObject("car", car);
            mav.addObject("nextDate", nextAvailableDate);
            mav.addObject("bookedPeriods", bookedPeriods);
            return mav;
        } catch (Exception e) {
            logger.error("Error viewing car ID {}: {}", id, e.getMessage(), e);
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("error", "Автомобиль не найден");
            return mav;
        }
    }

    @GetMapping("/available")
    public ModelAndView listAvailableCars(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Car> carPage = carService.findAvailableCarsWithPagination(page, size);
            // Загружаем изображения для каждого автомобиля
            carPage.getContent().forEach(car -> car.getImages().size());
            
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

    @GetMapping
    public ModelAndView listCars(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Car> carPage = carService.findAllCarsWithPagination(page, size);
            
            ModelAndView mav = new ModelAndView("cars/list");
            mav.addObject("cars", carPage.getContent());
            mav.addObject("currentPage", page);
            mav.addObject("totalPages", carPage.getTotalPages());
            mav.addObject("totalItems", carPage.getTotalElements());
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
            // Загружаем изображения для каждого автомобиля
            carPage.getContent().forEach(car -> car.getImages().size());
            
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
            // Загружаем изображения для каждого автомобиля
            carPage.getContent().forEach(car -> car.getImages().size());
            
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
    @GetMapping("/{id}/edit")
    public ModelAndView showEditCarForm(@PathVariable Long id) {
        try {
            Car car = carService.findCarById(id);
            CarDto carDto = new CarDto();
            carDto.setId(car.getId());
            carDto.setBrand(car.getBrand());
            carDto.setModel(car.getModel());
            carDto.setYear(car.getYear());
            carDto.setLicensePlate(car.getLicensePlate());
            carDto.setDailyRate(car.getDailyRate());
            carDto.setDescription(car.getDescription());
            carDto.setTransmission(car.getTransmission());
            carDto.setFuelType(car.getFuelType());
            carDto.setSeats(car.getSeats());
            carDto.setColor(car.getColor());
            carDto.setCategory(car.getCategory());
            carDto.setSchedule(car.getSchedule());
            carDto.setAvailable(car.isAvailable());

            // Преобразование изображений
            if (car.getImages() != null) {
                List<CarImageDto> imageDtos = car.getImages().stream()
                    .map(img -> {
                        CarImageDto imgDto = new CarImageDto();
                        imgDto.setId(img.getId());
                        imgDto.setImageUrl(img.getImageUrl());
                        imgDto.setDescription(img.getDescription());
                        imgDto.setMain(img.isMain());
                        imgDto.setDisplayOrder(img.getDisplayOrder());
                        imgDto.setRotation(img.getRotation());
                        return imgDto;
                    })
                    .collect(Collectors.toList());
                carDto.setExistingImages(imageDtos);
            }

            ModelAndView mav = new ModelAndView("cars/edit");
            mav.addObject("car", carDto);
            mav.addObject("carId", id);
            
            logger.debug("Car ID: {}", id);
            logger.debug("Car DTO: {}", carDto);
            logger.debug("Existing images: {}", carDto.getExistingImages());
            
            return mav;
        } catch (Exception e) {
            logger.error("Error showing edit form for car ID {}: {}", id, e.getMessage(), e);
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("error", "Автомобиль не найден");
            return mav;
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/edit")
    public ModelAndView updateCar(@PathVariable Long id,
                                @Valid @ModelAttribute("car") CarDto carDto,
                                @RequestParam(value = "mainImage", required = false) Long mainImageId,
                                @RequestParam Map<String, String> allParams,
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

            // Нормализация номера автомобиля
            String normalizedLicensePlate = normalizeLicensePlate(carDto.getLicensePlate());
            carDto.setLicensePlate(normalizedLicensePlate);

            // Получаем автомобиль с его изображениями
            Car car = carService.findCarById(id);
            
            // Обновляем основные данные автомобиля
            car.setBrand(carDto.getBrand());
            car.setModel(carDto.getModel());
            car.setYear(carDto.getYear());
            car.setLicensePlate(normalizedLicensePlate);
            car.setDailyRate(carDto.getDailyRate());
            car.setDescription(carDto.getDescription());
            car.setTransmission(carDto.getTransmission());
            car.setFuelType(carDto.getFuelType());
            car.setSeats(carDto.getSeats());
            car.setColor(carDto.getColor());
            car.setCategory(carDto.getCategory());
            car.setSchedule(carDto.getSchedule());
            car.setRegistrationNumber(normalizedLicensePlate);

            // Обновляем статус основного изображения
            if (mainImageId != null && car.getImages() != null) {
                car.getImages().forEach(img -> img.setMain(img.getId().equals(mainImageId)));
            }

            // Обработка поворота изображений
            if (car.getImages() != null) {
                car.getImages().forEach(img -> {
                    String rotationKey = "imageRotation_" + img.getId();
                    if (allParams.containsKey(rotationKey)) {
                        int rotation = Integer.parseInt(allParams.get(rotationKey));
                        img.setRotation(rotation);
                    }
                });
            }

            // Обработка новых загруженных изображений
            if (carDto.getNewImages() != null && !carDto.getNewImages().isEmpty()) {
                List<CarImage> newImages = new ArrayList<>();
                boolean isFirstImage = car.getImages() == null || car.getImages().isEmpty();
                
                for (MultipartFile file : carDto.getNewImages()) {
                    if (!file.isEmpty()) {
                        CarImage image = new CarImage();
                        String imageUrl = carService.saveCarImage(file);
                        image.setImageUrl(imageUrl);
                        image.setMain(isFirstImage);
                        image.setCar(car);
                        newImages.add(image);
                        isFirstImage = false;
                    }
                }
                
                if (car.getImages() == null) {
                    car.setImages(new ArrayList<>());
                }
                car.getImages().addAll(newImages);
            }

            // Сохраняем обновленный автомобиль
            Car updatedCar = carService.updateCar(car);
            
            ModelAndView mav = new ModelAndView("redirect:/cars/" + updatedCar.getId());
            mav.addObject("success", "Автомобиль успешно обновлен");
            return mav;
        } catch (Exception e) {
            logger.error("Error updating car ID {}: {}", id, e.getMessage(), e);
            ModelAndView mav = new ModelAndView("cars/edit");
            mav.addObject("car", carDto);
            mav.addObject("carId", id);
            mav.addObject("error", "Ошибка при обновлении автомобиля: " + e.getMessage());
            return mav;
        }
    }

    private String normalizeLicensePlate(String licensePlate) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            return null;
        }
        // Удаляем все пробелы и приводим к верхнему регистру
        return licensePlate.replaceAll("\\s+", "").toUpperCase();
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

        List<Car> filteredCars = onlyAvailable ?
                carService.findAvailableCars() :
                carService.findAllCars();

        // Загружаем изображения для каждого автомобиля
        filteredCars.forEach(car -> car.getImages().size());

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