package ru.anapa.autorent.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.anapa.autorent.model.Car;
import ru.anapa.autorent.model.Rental;
import ru.anapa.autorent.model.User;
import ru.anapa.autorent.repository.RentalRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class RentalService {

    private final RentalRepository rentalRepository;
    private final CarService carService;

    @Autowired
    public RentalService(RentalRepository rentalRepository, CarService carService) {
        this.rentalRepository = rentalRepository;
        this.carService = carService;
    }

    @Transactional(readOnly = true)
    public List<Rental> findRentalsByUser(User user) {
        return rentalRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public Rental findById(Long id) {
        return rentalRepository.findByIdWithCarAndUser(id)
                .orElseThrow(() -> new RuntimeException("Аренда с ID " + id + " не найдена"));
    }

    @Transactional(readOnly = true)
    public List<Rental> findAllRentals() {
        return rentalRepository.findAllWithCarAndUser();
    }

    @Transactional(readOnly = true)
    public List<Rental> findRentalsByStatus(String status) {
        return rentalRepository.findByStatusWithCarAndUser(status);
    }

    @Transactional
    public Rental createRental(User user, Long carId, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new RuntimeException("Дата начала аренды не может быть позже даты окончания");
        }

        if (startDate.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Дата начала аренды не может быть в прошлом");
        }

        Car car = carService.findCarById(carId);

        if (!car.isAvailable()) {
            throw new RuntimeException("Автомобиль недоступен для аренды");
        }

        // Проверка, не арендован ли автомобиль на указанные даты
        boolean isCarBooked = rentalRepository.findAllWithCarAndUser().stream()
                .filter(r -> r.getCar().getId().equals(carId))
                .filter(r -> r.getStatus().equals("PENDING") || r.getStatus().equals("ACTIVE"))
                .anyMatch(r ->
                        (startDate.isAfter(r.getStartDate()) && startDate.isBefore(r.getEndDate())) ||
                                (endDate.isAfter(r.getStartDate()) && endDate.isBefore(r.getEndDate())) ||
                                (startDate.isBefore(r.getStartDate()) && endDate.isAfter(r.getEndDate())) ||
                                (startDate.isEqual(r.getStartDate()) || endDate.isEqual(r.getEndDate()))
                );

        if (isCarBooked) {
            throw new RuntimeException("Автомобиль уже забронирован на указанные даты");
        }

        // Расчет стоимости аренды
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days < 1) days = 1; // Минимум 1 день аренды

        BigDecimal totalCost = car.getDailyRate().multiply(BigDecimal.valueOf(days));

        Rental rental = new Rental();
        rental.setUser(user);
        rental.setCar(car);
        rental.setStartDate(startDate);
        rental.setEndDate(endDate);
        rental.setStatus("PENDING");
        rental.setTotalCost(totalCost);
        rental.setCreatedAt(LocalDateTime.now());

        return rentalRepository.save(rental);
    }

    @Transactional
    public void cancelRental(Long rentalId) {
        Rental rental = findById(rentalId);

        if (rental.getStatus().equals("COMPLETED")) {
            throw new RuntimeException("Нельзя отменить завершенную аренду");
        }

        rental.setStatus("CANCELLED");
        rental.setUpdatedAt(LocalDateTime.now());
        rentalRepository.save(rental);
    }

    @Transactional
    public void completeRental(Long rentalId) {
        Rental rental = findById(rentalId);

        if (rental.getStatus().equals("CANCELLED")) {
            throw new RuntimeException("Нельзя завершить отмененную аренду");
        }

        if (rental.getStatus().equals("COMPLETED")) {
            throw new RuntimeException("Аренда уже завершена");
        }

        rental.setStatus("COMPLETED");
        rental.setActualReturnDate(LocalDateTime.now());
        rental.setUpdatedAt(LocalDateTime.now());

        // Если автомобиль возвращен позже срока, можно добавить дополнительную плату
        if (rental.getActualReturnDate().isAfter(rental.getEndDate())) {
            long extraDays = ChronoUnit.DAYS.between(rental.getEndDate(), rental.getActualReturnDate());
            if (extraDays > 0) {
                BigDecimal extraCharge = rental.getCar().getDailyRate()
                        .multiply(BigDecimal.valueOf(extraDays))
                        .multiply(BigDecimal.valueOf(1.5)); // Штраф 150% от дневной ставки
                rental.setTotalCost(rental.getTotalCost().add(extraCharge));
            }
        }

        rentalRepository.save(rental);
    }

    @Transactional
    public void approveRental(Long rentalId) {
        Rental rental = findById(rentalId);

        if (!rental.getStatus().equals("PENDING")) {
            throw new RuntimeException("Аренду можно одобрить только в статусе 'Ожидает'");
        }

        rental.setStatus("ACTIVE");
        rental.setUpdatedAt(LocalDateTime.now());

        // Обновляем статус автомобиля, если аренда начинается сегодня
        if (rental.getStartDate().toLocalDate().equals(LocalDateTime.now().toLocalDate())) {
            Car car = rental.getCar();
            car.setAvailable(false);
            // Предполагается, что у вас есть метод updateCar в CarService
            carService.updateCar(car);
        }

        rentalRepository.save(rental);
    }

    @Transactional
    public Rental updateRental(Rental rental) {
        return rentalRepository.save(rental);
    }
}