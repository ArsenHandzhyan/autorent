package ru.anapa.autorent.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.anapa.autorent.model.Car;
import ru.anapa.autorent.model.Rental;
import ru.anapa.autorent.model.User;
import ru.anapa.autorent.repository.RentalRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    public List<Rental> findAllRentals() {
        return rentalRepository.findAll();
    }

    public List<Rental> findRentalsByUser(User user) {
        return rentalRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Rental> findRentalsByStatus(String status) {
        return rentalRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public Rental findById(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Аренда не найдена"));
    }

    @Transactional
    public Rental createRental(User user, Long carId, LocalDateTime startDate, LocalDateTime endDate) {
        // Проверка доступности автомобиля
        Car car = carService.findCarById(carId);

        if (!car.isAvailable()) {
            throw new RuntimeException("Автомобиль недоступен для аренды");
        }

        // Проверка дат
        if (startDate.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Дата начала аренды не может быть в прошлом");
        }

        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("Дата окончания аренды не может быть раньше даты начала");
        }

        // Проверка, что автомобиль не арендован на указанные даты
        List<Rental> overlappingRentals = rentalRepository.findOverlappingRentals(car, startDate, endDate);
        if (!overlappingRentals.isEmpty()) {
            throw new RuntimeException("Автомобиль уже арендован на указанные даты");
        }

        // Расчет стоимости
        long hours = ChronoUnit.HOURS.between(startDate, endDate);
        long days = hours / 24 + (hours % 24 > 0 ? 1 : 0); // округляем вверх до полных дней
        BigDecimal totalCost = car.getPricePerDay().multiply(BigDecimal.valueOf(days));

        // Создание аренды
        Rental rental = new Rental();
        rental.setUser(user);
        rental.setCar(car);
        rental.setStartDate(startDate);
        rental.setEndDate(endDate);
        rental.setTotalCost(totalCost);
        rental.setStatus("PENDING");
        rental.setCreatedAt(LocalDateTime.now());
        rental.setUpdatedAt(LocalDateTime.now());

        // Если аренда начинается сегодня, автоматически подтверждаем её
        if (startDate.toLocalDate().isEqual(LocalDate.now())) {
            rental.setStatus("ACTIVE");
            car.setAvailable(false);
            carService.updateCar(car);
        }

        return rentalRepository.save(rental);
    }

    @Transactional
    public Rental updateRental(Rental rental) {
        rental.setUpdatedAt(LocalDateTime.now());
        return rentalRepository.save(rental);
    }

    @Transactional
    public void approveRental(Long rentalId) {
        Rental rental = findById(rentalId);

        if (!rental.getStatus().equals("PENDING")) {
            throw new RuntimeException("Можно подтвердить только ожидающую аренду");
        }

        Car car = rental.getCar();

        // Проверка, что автомобиль не арендован на указанные даты
        List<Rental> overlappingRentals = rentalRepository.findOverlappingActiveRentals(
                car, rental.getStartDate(), rental.getEndDate(), rentalId);

        if (!overlappingRentals.isEmpty()) {
            throw new RuntimeException("Автомобиль уже арендован на указанные даты");
        }

        rental.setStatus("ACTIVE");
        rental.setUpdatedAt(LocalDateTime.now());

        // Если аренда начинается сегодня или раньше, меняем статус автомобиля
        if (rental.getStartDate().toLocalDate().isEqual(LocalDate.now()) ||
                rental.getStartDate().toLocalDate().isBefore(LocalDate.now())) {
            car.setAvailable(false);
            carService.updateCar(car);
        }

        rentalRepository.save(rental);
    }

    @Transactional
    public void completeRental(Long rentalId) {
        Rental rental = findById(rentalId);

        if (!rental.getStatus().equals("ACTIVE")) {
            throw new RuntimeException("Можно завершить только активную аренду");
        }

        rental.setStatus("COMPLETED");
        rental.setActualReturnDate(LocalDateTime.now());
        rental.setUpdatedAt(LocalDateTime.now());

        // Освобождаем автомобиль
        Car car = rental.getCar();
        car.setAvailable(true);
        carService.updateCar(car);

        rentalRepository.save(rental);
    }

    @Transactional
    public void cancelRental(Long rentalId) {
        Rental rental = findById(rentalId);

        if (rental.getStatus().equals("COMPLETED") || rental.getStatus().equals("CANCELLED")) {
            throw new RuntimeException("Нельзя отменить завершенную или уже отмененную аренду");
        }

        rental.setStatus("CANCELLED");
        rental.setUpdatedAt(LocalDateTime.now());

        // Если аренда была активной, освобождаем автомобиль
        if (rental.getStatus().equals("ACTIVE")) {
            Car car = rental.getCar();
            car.setAvailable(true);
            carService.updateCar(car);
        }

        rentalRepository.save(rental);
    }

    @Transactional
    public void requestCancellation(Long rentalId, String cancelReason) {
        Rental rental = findById(rentalId);

        if (rental.getStatus().equals("COMPLETED") || rental.getStatus().equals("CANCELLED")) {
            throw new RuntimeException("Нельзя отменить завершенную или уже отмененную аренду");
        }

        if (rental.getStatus().equals("PENDING_CANCELLATION")) {
            throw new RuntimeException("Запрос на отмену уже отправлен");
        }

        rental.setStatus("PENDING_CANCELLATION");
        rental.setCancelReason(cancelReason);
        rental.setUpdatedAt(LocalDateTime.now());

        rentalRepository.save(rental);
    }

    @Transactional
    public void rejectCancellationRequest(Long rentalId, String adminNotes) {
        Rental rental = findById(rentalId);

        if (!rental.getStatus().equals("PENDING_CANCELLATION")) {
            throw new RuntimeException("Можно отклонить только запрос на отмену");
        }

        // Возвращаем предыдущий статус (ACTIVE или PENDING)
        if (rental.getStartDate().toLocalDate().isEqual(LocalDate.now()) ||
                rental.getStartDate().toLocalDate().isBefore(LocalDate.now())) {
            rental.setStatus("ACTIVE");
        } else {
            rental.setStatus("PENDING");
        }

        // Добавляем примечание администратора
        if (adminNotes != null && !adminNotes.isEmpty()) {
            String notes = rental.getNotes() != null ? rental.getNotes() + "\n\n" : "";
            notes += "Отказ в отмене: " + adminNotes;
            rental.setNotes(notes);
        }

        rental.setUpdatedAt(LocalDateTime.now());
        rentalRepository.save(rental);
    }

    @Transactional
    public void synchronizeAllCarStatuses() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        // Находим все активные аренды, которые должны были закончиться
        List<Rental> completedRentals = rentalRepository.findByStatusAndEndDateBefore("ACTIVE", now);
        for (Rental rental : completedRentals) {
            rental.setStatus("COMPLETED");
            rental.setActualReturnDate(now);
            rental.setUpdatedAt(now);

            // Освобождаем автомобиль
            Car car = rental.getCar();
            car.setAvailable(true);
            carService.updateCar(car);

            rentalRepository.save(rental);
        }

        // Находим все ожидающие аренды, которые должны начаться сегодня
        LocalDateTime startOfDay = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(today, LocalTime.MAX);

        List<Rental> startingRentals = rentalRepository.findByStatusAndStartDateBetween("PENDING", startOfDay, endOfDay);
        for (Rental rental : startingRentals) {
            rental.setStatus("ACTIVE");
            rental.setUpdatedAt(now);

            // Занимаем автомобиль
            Car car = rental.getCar();
            car.setAvailable(false);
            carService.updateCar(car);

            rentalRepository.save(rental);
        }
    }
}