package ru.anapa.autorent.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.anapa.autorent.config.DataInitializer;
import ru.anapa.autorent.dto.RentalPeriodDto;
import ru.anapa.autorent.model.Car;
import ru.anapa.autorent.model.Rental;
import ru.anapa.autorent.repository.CarRepository;
import ru.anapa.autorent.repository.RentalRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarService {
    private final CarRepository carRepository;
    private final RentalRepository rentalRepository;
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);


    @Autowired
    public CarService(CarRepository carRepository, RentalRepository rentalRepository) {
        this.carRepository = carRepository;
        this.rentalRepository = rentalRepository;
    }

    public List<Car> findAllCars() {
        return carRepository.findAll();
    }

    public List<Car> findAvailableCars() {
        return carRepository.findByAvailableTrue();
    }

    public Car findCarById(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Автомобиль не найден"));
    }

    public Car saveCar(Car car) {
        return carRepository.save(car);
    }

    @Transactional
    public Car updateCar(Car car) {
        return carRepository.save(car);
    }

    @Transactional
    public void updateCarAvailability(Long carId, boolean available) {
        Car car = findCarById(carId);
        car.setAvailable(available);
        carRepository.save(car);
        logger.info("Статус доступности автомобиля " + carId + " обновлен на: " + available);
    }

    public void deleteCar(Long id) {
        carRepository.deleteById(id);
    }

    public List<Car> searchCarsByBrand(String brand) {
        return carRepository.findByBrandContainingIgnoreCase(brand);
    }

    @Transactional
    public void checkAndUpdateCarStatuses() {
        // Этот метод можно использовать для периодической проверки и обновления статусов автомобилей
        // Например, через планировщик задач
        List<Car> allCars = carRepository.findAll();
        for (Car car : allCars) {
            // Здесь можно добавить логику проверки активных аренд
            // и обновления статуса автомобиля
        }
    }

    /**
     * Получает ближайшую дату освобождения автомобиля
     * @param carId ID автомобиля
     * @return Дата освобождения или null, если автомобиль доступен
     */
    public LocalDateTime getNextAvailableDate(Long carId) {
        Car car = findCarById(carId);

        // Проверяем активные аренды независимо от статуса car.isAvailable()
        List<Rental> activeRentals = rentalRepository.findByCarAndStatusOrderByEndDateAsc(
                car, "ACTIVE");

        List<Rental> pendingRentals = rentalRepository.findByCarAndStatusOrderByEndDateAsc(
                car, "PENDING");

        // Объединяем активные и ожидающие аренды
        List<Rental> allRelevantRentals = new ArrayList<>();
        allRelevantRentals.addAll(activeRentals);
        allRelevantRentals.addAll(pendingRentals);

        // Сортируем по дате окончания
        allRelevantRentals.sort(Comparator.comparing(Rental::getEndDate));

        if (allRelevantRentals.isEmpty()) {
            // Если нет аренд, но автомобиль помечен как недоступный, исправляем статус
            if (!car.isAvailable()) {
                car.setAvailable(true);
                carRepository.save(car);
            }
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
                car, List.of("ACTIVE", "PENDING"));

        // Преобразуем в DTO
        return rentals.stream()
                .map(rental -> new RentalPeriodDto(rental.getStartDate(), rental.getEndDate()))
                .collect(Collectors.toList());
    }
}