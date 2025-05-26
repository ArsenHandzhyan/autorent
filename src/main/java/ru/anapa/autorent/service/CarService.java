package ru.anapa.autorent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.anapa.autorent.config.DataInitializer;
import ru.anapa.autorent.dto.CarSummaryDto;
import ru.anapa.autorent.dto.RentalPeriodDto;
import ru.anapa.autorent.model.Car;
import ru.anapa.autorent.model.CarImage;
import ru.anapa.autorent.model.CarStats;
import ru.anapa.autorent.model.CarStatus;
import ru.anapa.autorent.model.CarStatusHistory;
import ru.anapa.autorent.model.Rental;
import ru.anapa.autorent.model.RentalStatus;
import ru.anapa.autorent.repository.CarImageRepository;
import ru.anapa.autorent.repository.CarRepository;
import ru.anapa.autorent.repository.CarStatusHistoryRepository;
import ru.anapa.autorent.repository.RentalRepository;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CarService {
    private final CarRepository carRepository;
    private final RentalRepository rentalRepository;
    private final RentalService rentalService;
    private final CarImageRepository carImageRepository;
    private final CarStatusHistoryRepository carStatusHistoryRepository;
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    public CarService(CarRepository carRepository, RentalRepository rentalRepository, @Lazy RentalService rentalService, CarImageRepository carImageRepository, CarStatusHistoryRepository carStatusHistoryRepository) {
        this.carRepository = carRepository;
        this.rentalRepository = rentalRepository;
        this.rentalService = rentalService;
        this.carImageRepository = carImageRepository;
        this.carStatusHistoryRepository = carStatusHistoryRepository;
    }

    // Метод для получения всех автомобилей
    public List<Car> findAllCars() {
        return carRepository.findAll();
    }

    // Оптимизированный метод с пагинацией
    @Transactional(readOnly = true)
    public Page<Car> findAllCarsWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Car> carPage = carRepository.findAll(pageable);
        
        // Загружаем изображения для всех автомобилей одним запросом
        List<Long> carIds = carPage.getContent().stream()
                .map(Car::getId)
                .collect(Collectors.toList());
        
        if (!carIds.isEmpty()) {
            List<CarImage> images = carImageRepository.findByCarIdInOrderByDisplayOrderAsc(carIds);
            Map<Long, List<CarImage>> imagesByCarId = images.stream()
                    .collect(Collectors.groupingBy(img -> img.getCar().getId()));
            
            carPage.getContent().forEach(car -> 
                car.setImages(imagesByCarId.getOrDefault(car.getId(), new ArrayList<>())));
        }
        
        return carPage;
    }

    // Получение DTO с предварительно вычисленными датами доступности для списка автомобилей
    public List<CarSummaryDto> findAllCarsWithAvailability() {
        // Получаем страницу автомобилей для лучшей производительности
        List<Car> cars = carRepository.findAll();

        // Предварительно загружаем даты доступности
        Map<Long, LocalDateTime> availabilityMap = preloadAvailabilityDates(cars);

        // Преобразуем в DTO для шаблона
        return cars.stream().map(car -> mapToSummaryDto(car, availabilityMap.get(car.getId())))
                .collect(Collectors.toList());
    }

    // Получение DTO с пагинацией и предварительно вычисленными датами
    public Page<CarSummaryDto> findAllCarsWithAvailabilityPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Car> carPage = carRepository.findAll(pageable);

        List<Car> cars = carPage.getContent();
        Map<Long, LocalDateTime> availabilityMap = preloadAvailabilityDates(cars);

        List<CarSummaryDto> dtoList = cars.stream()
                .map(car -> mapToSummaryDto(car, availabilityMap.get(car.getId())))
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(
                dtoList, pageable, carPage.getTotalElements());
    }

    // Вспомогательный метод для преобразования Car в CarSummaryDto
    private CarSummaryDto mapToSummaryDto(Car car, LocalDateTime nextAvailableDate) {
        CarSummaryDto dto = new CarSummaryDto();
        dto.setId(car.getId());
        dto.setBrand(car.getBrand());
        dto.setModel(car.getModel());
        dto.setYear(car.getYear());
        dto.setImageUrl(car.getImages().stream()
                .filter(CarImage::isMain)
                .findFirst()
                .map(CarImage::getImageUrl)
                .orElseGet(() -> car.getImages().isEmpty() ? null : car.getImages().get(0).getImageUrl()));
        dto.setDailyRate(car.getDailyRate());
        dto.setAvailable(car.isAvailable());
        dto.setStatus(car.getStatus());
        dto.setNextAvailableDate(nextAvailableDate);
        dto.setTransmission(car.getTransmission());
        dto.setFuelType(car.getFuelType());
        dto.setSeats(car.getSeats());
        dto.setCategory(car.getCategory());
        return dto;
    }

    // Оптимизированный метод предварительной загрузки дат доступности
    private Map<Long, LocalDateTime> preloadAvailabilityDates(List<Car> cars) {
        if (cars.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, LocalDateTime> result = new HashMap<>();

        // Извлекаем ID автомобилей
        List<Long> carIds = cars.stream().map(Car::getId).collect(Collectors.toList());

        // Получаем все активные и ожидающие аренды одним запросом
        List<Rental> allRentals = rentalRepository.findRentalsByCarIdInAndStatusIn(
                carIds, Arrays.asList(RentalStatus.ACTIVE, RentalStatus.PENDING));

        // Группируем аренды по ID автомобиля для быстрого доступа
        Map<Long, List<Rental>> rentalsByCarId = allRentals.stream()
                .collect(Collectors.groupingBy(rental -> rental.getCar().getId()));

        // Обрабатываем каждый автомобиль
        for (Car car : cars) {
            List<Rental> carRentals = rentalsByCarId.getOrDefault(car.getId(), Collections.emptyList());

            if (carRentals.isEmpty()) {
                result.put(car.getId(), null); // Автомобиль доступен сейчас
            } else {
                // Находим самую позднюю дату окончания аренды
                LocalDateTime latestEndDate = carRentals.stream()
                        .map(Rental::getEndDate)
                        .max(LocalDateTime::compareTo)
                        .orElse(null);

                result.put(car.getId(), latestEndDate);
            }
        }

        return result;
    }

    // Базовые методы для работы с автомобилями
    public List<Car> findAvailableCars() {
        return carRepository.findByAvailableTrue();
    }

    public Page<Car> findAvailableCarsWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return carRepository.findByAvailableTrue(pageable);
    }

    public Car findCarById(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Автомобиль не найден"));
        // Загружаем изображения в одном запросе
        List<CarImage> images = carImageRepository.findByCarIdOrderByDisplayOrderAsc(id);
        car.setImages(images);
        return car;
    }

    @Transactional
    public Car saveCar(Car car) {
        if (car.getImages() != null) {
            for (int i = 0; i < car.getImages().size(); i++) {
                CarImage image = car.getImages().get(i);
                image.setCar(car);
                image.setDisplayOrder(i);
            }
        }
        return carRepository.save(car);
    }

    @Transactional
    public Car updateCar(Car car) {
        Car existingCar = carRepository.findById(car.getId())
                .orElseThrow(() -> new RuntimeException("Автомобиль не найден"));

        // Обновляем основные поля
        existingCar.setBrand(car.getBrand());
        existingCar.setModel(car.getModel());
        existingCar.setYear(car.getYear());
        existingCar.setLicensePlate(car.getLicensePlate());
        existingCar.setDailyRate(car.getDailyRate());
        existingCar.setDescription(car.getDescription());
        existingCar.setRegistrationNumber(car.getLicensePlate());
        existingCar.setTransmission(car.getTransmission());
        existingCar.setFuelType(car.getFuelType());
        existingCar.setSeats(car.getSeats());
        existingCar.setColor(car.getColor());
        existingCar.setCategory(car.getCategory());
        existingCar.setSchedule(car.getSchedule());
        existingCar.setStatus(car.getStatus());

        // Обновляем изображения
        if (car.getImages() != null) {
            // Получаем существующие изображения
            List<CarImage> existingImages = carImageRepository.findByCarIdOrderByDisplayOrderAsc(car.getId());
            
            // Удаляем изображения, которых нет в новом списке
            Set<Long> newImageIds = car.getImages().stream()
                    .map(CarImage::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            
            existingImages.stream()
                    .filter(img -> !newImageIds.contains(img.getId()))
                    .forEach(img -> carImageRepository.deleteById(img.getId()));

            // Очищаем список изображений и добавляем новые
            existingCar.getImages().clear();

            // Обновляем или добавляем новые изображения
            for (int i = 0; i < car.getImages().size(); i++) {
                CarImage image = car.getImages().get(i);
                if (image.getId() == null) {
                    // Новое изображение
                    image.setCar(existingCar);
                    image.setDisplayOrder(i);
                    existingCar.getImages().add(image);
                } else {
                    // Обновляем существующее изображение
                    CarImage existingImage = existingImages.stream()
                            .filter(img -> img.getId().equals(image.getId()))
                            .findFirst()
                            .orElse(null);
                    
                    if (existingImage != null) {
                        existingImage.setMain(image.isMain());
                        existingImage.setDisplayOrder(i);
                        existingImage.setDescription(image.getDescription());
                        existingImage.setRotation(image.getRotation());
                        existingCar.getImages().add(existingImage);
                    }
                }
            }
        }

        return carRepository.save(existingCar);
    }

    @Transactional
    @CacheEvict(value = "carAvailability", key = "#carId")
    public void updateCarAvailability(Long carId, boolean available) {
        Car car = findCarById(carId);
        car.setAvailable(available);
        carRepository.save(car);
        logger.info("Статус доступности автомобиля {} обновлен на: {}", carId, available);
    }

    @Transactional
    @CacheEvict(value = "carAvailability", key = "#id")
    public void deleteCar(Long id) {
        carRepository.deleteById(id);
    }

    // Оптимизированные методы поиска
    public Page<Car> searchCarsByBrandWithPagination(String brand, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return carRepository.searchByBrand(brand, pageable);
    }

    public Page<Car> findCarsByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return carRepository.findByCategoryIgnoreCase(category, pageable);
    }

    // Метод для автоматического обновления статусов автомобилей (планировщик)
    @Scheduled(fixedRate = 3600000) // Запускаем каждый час
    @Transactional
    @CacheEvict(value = "carAvailability", allEntries = true)
    public void checkAndUpdateCarStatuses() {
        logger.info("Запуск проверки статусов автомобилей");

        // Получаем все активные аренды
        List<Rental> activeRentals = rentalRepository.findByStatus(RentalStatus.ACTIVE);
        Set<Long> activeCarIds = activeRentals.stream()
                .map(rental -> rental.getCar().getId())
                .collect(Collectors.toSet());

        // Получаем все ожидающие аренды
        List<Rental> pendingRentals = rentalRepository.findByStatus(RentalStatus.PENDING);
        Set<Long> pendingCarIds = pendingRentals.stream()
                .map(rental -> rental.getCar().getId())
                .collect(Collectors.toSet());

        // Обновляем статусы автомобилей в одной транзакции
        List<Car> allCars = carRepository.findAll();
        int updatedCount = 0;

        for (Car car : allCars) {
            // Пропускаем автомобили на обслуживании
            if (car.getStatus() == CarStatus.MAINTENANCE) {
                continue;
            }

            CarStatus newStatus;
            if (activeCarIds.contains(car.getId())) {
                newStatus = CarStatus.RENTED;
            } else if (pendingCarIds.contains(car.getId())) {
                newStatus = CarStatus.RESERVED;
            } else {
                newStatus = CarStatus.AVAILABLE;
            }

            if (car.getStatus() != newStatus) {
                car.setStatus(newStatus);
                car.setAvailable(newStatus == CarStatus.AVAILABLE);
                carRepository.save(car);
                updatedCount++;
            }
        }

        logger.info("Обновлены статусы {} автомобилей", updatedCount);
    }

    /**
     * Получает ближайшую дату освобождения автомобиля
     * @param carId ID автомобиля
     * @return Дата освобождения или null, если автомобиль доступен
     */
    @Cacheable(value = "carAvailability", key = "#carId")
    public LocalDateTime getNextAvailableDate(Long carId) {
        Car car = findCarById(carId);

        // Получаем все активные и ожидающие аренды
        List<Rental> allRelevantRentals = new ArrayList<>();
        allRelevantRentals.addAll(rentalRepository.findByCarAndStatusOrderByEndDateAsc(car, RentalStatus.ACTIVE));
        allRelevantRentals.addAll(rentalRepository.findByCarAndStatusOrderByEndDateAsc(car, RentalStatus.PENDING));

        // Сортируем по дате окончания
        allRelevantRentals.sort(Comparator.comparing(Rental::getEndDate));

        if (allRelevantRentals.isEmpty()) {
            return null; // Автомобиль доступен сейчас
        }

        // Возвращаем дату окончания последней аренды
        return allRelevantRentals.get(allRelevantRentals.size() - 1).getEndDate();
    }

    /**
     * Получает все периоды бронирования автомобиля (активные и ожидающие)
     * @param carId ID автомобиля
     * @return Список периодов бронирования
     */
    public List<RentalPeriodDto> getBookedPeriods(Long carId) {
        Car car = findCarById(carId);

        // Находим все активные и ожидающие аренды
        List<Rental> rentals = rentalRepository.findByCarAndStatusInOrderByStartDateAsc(
                car, List.of(RentalStatus.ACTIVE, RentalStatus.PENDING));

        // Преобразуем в DTO
        return rentals.stream()
                .map(rental -> new RentalPeriodDto(rental.getStartDate(), rental.getEndDate()))
                .collect(Collectors.toList());
    }

    public List<CarStats> getPopularCars() {
        List<Car> allCars = findAllCars();
        List<CarStats> carStats = new ArrayList<>();
        
        allCars.forEach(car -> {
            CarStats stats = new CarStats();
            stats.setBrand(car.getBrand());
            stats.setModel(car.getModel());
            
            // Получаем все аренды для этого автомобиля
            List<Rental> carRentals = rentalService.findRentalsByCar(car);
            
            // Считаем количество аренд
            stats.setRentalCount(carRentals.size());
            
            // Считаем общий доход
            BigDecimal revenue = carRentals.stream()
                    .filter(rental -> rental.getStatus().equals(RentalStatus.COMPLETED.name()))
                    .map(Rental::getTotalCost)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.setRevenue(revenue.doubleValue());
            
            // Считаем среднюю оценку
            double averageRating = carRentals.stream()
                    .filter(rental -> rental.getRating() != null)
                    .mapToDouble(rental -> rental.getRating().doubleValue())
                    .average()
                    .orElse(0.0);
            stats.setAverageRating(averageRating);
            
            carStats.add(stats);
        });
        
        // Сортируем по количеству аренд и доходу
        carStats.sort((a, b) -> {
            int compareByRentals = Integer.compare(b.getRentalCount(), a.getRentalCount());
            if (compareByRentals != 0) return compareByRentals;
            return Double.compare(b.getRevenue(), a.getRevenue());
        });
        
        // Возвращаем топ-10 автомобилей
        return carStats.stream()
                .limit(10)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CarImage> getCarImages(Long carId) {
        Car car = findCarById(carId);
        return car.getImages().stream()
                .sorted(Comparator.comparing(CarImage::getDisplayOrder))
                .collect(Collectors.toList());
    }

    @Transactional
    public String saveCarImage(MultipartFile file) {
        try {
            // Генерируем уникальное имя файла
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            
            // Создаем директорию для сохранения, если она не существует
            String uploadDir = "uploads/cars";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // Сохраняем файл
            File destFile = new File(dir.getAbsolutePath() + File.separator + fileName);
            file.transferTo(destFile);
            
            // Возвращаем относительный путь к файлу
            return "/uploads/cars/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить изображение: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Car updateCarStatus(Long carId, CarStatus newStatus, String changedBy, String reason) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Новый статус не может быть null");
        }
        
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("Автомобиль не найден"));
        
        CarStatus oldStatus = car.getStatus();
        if (oldStatus == newStatus) {
            logger.info("Статус автомобиля {} уже установлен как {}", carId, newStatus);
            return car;
        }
        
        try {
            // Сохраняем историю изменения статуса
            CarStatusHistory history = new CarStatusHistory(car, oldStatus, newStatus, changedBy);
            if (reason != null) {
                history.setReason(reason);
            }
            carStatusHistoryRepository.save(history);
            
            // Обновляем только статус и доступность
            car.setStatus(newStatus);
            car.setAvailable(newStatus == CarStatus.AVAILABLE);
            
            // Сохраняем автомобиль
            Car savedCar = carRepository.save(car);
            
            logger.info("Статус автомобиля {} изменен с {} на {} пользователем {}", 
                       carId, oldStatus, newStatus, changedBy);
            
            return savedCar;
        } catch (Exception e) {
            logger.error("Ошибка при обновлении статуса автомобиля {}: {}", carId, e.getMessage());
            throw new RuntimeException("Не удалось обновить статус автомобиля: " + e.getMessage());
        }
    }

    public List<CarStatusHistory> getCarStatusHistory(Long carId) {
        return carStatusHistoryRepository.findByCarIdOrderByChangeDateDesc(carId);
    }

    @Transactional
    public Car setMaintenanceStatus(Long carId, boolean inMaintenance, String changedBy, String reason) {
        Car car = findCarById(carId);
        CarStatus oldStatus = car.getStatus();
        CarStatus newStatus = inMaintenance ? CarStatus.MAINTENANCE : CarStatus.AVAILABLE;
        
        // Сохраняем историю изменения статуса
        CarStatusHistory history = new CarStatusHistory(car, oldStatus, newStatus, changedBy);
        if (reason != null) {
            history.setReason(reason);
        }
        carStatusHistoryRepository.save(history);
        
        // Обновляем статус автомобиля
        car.setStatus(newStatus);
        car.setAvailable(!inMaintenance);
        
        logger.info("Статус обслуживания автомобиля {} изменен на {} пользователем {}", 
                   carId, newStatus, changedBy);
        
        return carRepository.save(car);
    }
}