package ru.anapa.autorent.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.anapa.autorent.model.Car;
import ru.anapa.autorent.repository.CarRepository;

import java.util.List;

@Service
public class CarService {
    private final CarRepository carRepository;

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

    public void deleteCar(Long id) {
        carRepository.deleteById(id);
    }

    public List<Car> searchCarsByBrand(String brand) {
        return carRepository.findByBrandContainingIgnoreCase(brand);
    }
}