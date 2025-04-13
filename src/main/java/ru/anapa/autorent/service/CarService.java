package ru.anapa.autorent.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.anapa.autorent.config.DataInitializer;
import ru.anapa.autorent.model.Car;
import ru.anapa.autorent.repository.CarRepository;

import java.util.List;

@Service
public class CarService {
    private final CarRepository carRepository;
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);


    @Autowired
    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
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
}