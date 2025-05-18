package ru.anapa.autorent.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.anapa.autorent.model.Car;
import ru.anapa.autorent.model.Rental;
import ru.anapa.autorent.model.RentalStatus;
import ru.anapa.autorent.model.User;
import ru.anapa.autorent.repository.CarRepository;
import ru.anapa.autorent.repository.RentalRepository;
import ru.anapa.autorent.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class RentalService {

    private final RentalRepository rentalRepository;
    private final CarService carService;
    private final CarRepository carRepository;

    @Autowired
    public RentalService(RentalRepository rentalRepository, @Lazy CarService carService, CarRepository carRepository) {
        this.rentalRepository = rentalRepository;
        this.carService = carService;
        this.carRepository = carRepository;
    }

    public Page<Rental> findAllRentals(Pageable pageable) {
        return rentalRepository.findAll(pageable);
    }

    public List<Rental> findAllRentals() {
        return rentalRepository.findAll();
    }

    public List<Rental> findRentalsByUser(User user) {
        return rentalRepository.findByUser(user);
    }

    public List<Rental> findRentalsByStatus(RentalStatus status) {
        return rentalRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public Rental findById(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Аренда не найдена"));
    }

    public Rental getRentalById(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Аренда с ID " + id + " не найдена"));
    }

    @Transactional
    public Rental createRental(User user, Long carId, LocalDateTime startDate, LocalDateTime endDate) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Автомобиль не найден"));

        // Проверяем, что дата начала аренды не раньше текущей даты
        if (startDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Дата начала аренды не может быть в прошлом");
        }

        // Проверяем, что дата окончания аренды позже даты начала
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Дата окончания аренды должна быть позже даты начала");
        }

        // Проверяем, что период аренды не пересекается с существующими арендами
        List<Rental> existingRentals = rentalRepository.findByCarAndStatusInOrderByStartDateAsc(
                car, List.of(RentalStatus.ACTIVE, RentalStatus.PENDING));

        for (Rental existingRental : existingRentals) {
            // Проверяем пересечение периодов
            if (!(endDate.isBefore(existingRental.getStartDate()) ||
                    startDate.isAfter(existingRental.getEndDate()))) {
                throw new IllegalArgumentException(
                        "Выбранный период пересекается с существующей арендой. " +
                                "Пожалуйста, выберите другие даты."
                );
            }
        }

        // Рассчитываем стоимость аренды
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days < 1) days = 1; // Минимум 1 день

        // Создаем новую аренду
        Rental rental = new Rental();
        rental.setUser(user);
        rental.setCar(car);
        rental.setStartDate(startDate);
        rental.setEndDate(endDate);
        rental.setTotalCost(car.getDailyRate());
        rental.setStatus(RentalStatus.PENDING);
        rental.setCreatedAt(LocalDateTime.now());

        // Если аренда начинается сегодня, то автомобиль становится недоступным
        if (startDate.toLocalDate().equals(LocalDateTime.now().toLocalDate())) {
            car.setAvailable(false);
            carRepository.save(car);
        }

        return rentalRepository.save(rental);
    }

    @Transactional
    public Rental updateRental(Rental rental) {
        rental.setUpdatedAt(LocalDateTime.now());
        return rentalRepository.save(rental);
    }

    @Transactional
    public Rental approveRental(Long rentalId) {
        Rental rental = findById(rentalId);

        if (rental.getStatus() != RentalStatus.PENDING) {
            throw new RuntimeException("Можно подтвердить только ожидающую аренду");
        }

        Car car = rental.getCar();

        // Проверка, что автомобиль не арендован на указанные даты
        List<Rental> overlappingRentals = rentalRepository.findOverlappingActiveRentals(
                car, rental.getStartDate(), rental.getEndDate(), rentalId);

        if (!overlappingRentals.isEmpty()) {
            throw new RuntimeException("Автомобиль уже арендован на указанные даты");
        }

        rental.setStatus(RentalStatus.ACTIVE);
        rental.setUpdatedAt(LocalDateTime.now());

        // Если аренда начинается сегодня или раньше, меняем статус автомобиля
        if (rental.getStartDate().toLocalDate().isEqual(LocalDate.now()) ||
                rental.getStartDate().toLocalDate().isBefore(LocalDate.now())) {
            car.setAvailable(false);
            carService.updateCar(car);
        }

        return rentalRepository.save(rental);
    }

    @Transactional
    public Rental completeRental(Long rentalId) {
        Rental rental = findById(rentalId);

        if (rental.getStatus() != RentalStatus.ACTIVE) {
            throw new RuntimeException("Можно завершить только активную аренду");
        }

        rental.setStatus(RentalStatus.COMPLETED);
        rental.setActualReturnDate(LocalDateTime.now());
        rental.setUpdatedAt(LocalDateTime.now());

        // Освобождаем автомобиль
        Car car = rental.getCar();
        car.setAvailable(true);
        carService.updateCar(car);

        return rentalRepository.save(rental);
    }

    @Transactional
    public Rental cancelRental(Long rentalId, String notes) {
        Rental rental = findById(rentalId);

        if (rental.getStatus() == RentalStatus.COMPLETED || 
            rental.getStatus() == RentalStatus.CANCELLED) {
            throw new RuntimeException("Нельзя отменить завершенную или уже отмененную аренду");
        }

        rental.setStatus(RentalStatus.CANCELLED);
        rental.setUpdatedAt(LocalDateTime.now());

        // Добавляем причину отмены, если она указана
        if (notes != null && !notes.isEmpty()) {
            rental.setCancelReason(notes);
        }

        // Если аренда была активной, освобождаем автомобиль
        if (rental.getStatus() == RentalStatus.ACTIVE) {
            Car car = rental.getCar();
            car.setAvailable(true);
            carService.updateCar(car);
        }

        return rentalRepository.save(rental);
    }

    @Transactional
    public Rental requestCancellation(Long rentalId, String cancelReason) {
        Rental rental = findById(rentalId);

        if (rental.getStatus() == RentalStatus.COMPLETED || 
            rental.getStatus() == RentalStatus.CANCELLED) {
            throw new RuntimeException("Нельзя отменить завершенную или уже отмененную аренду");
        }

        if (rental.getStatus() == RentalStatus.PENDING_CANCELLATION) {
            throw new RuntimeException("Запрос на отмену уже отправлен");
        }

        rental.setStatus(RentalStatus.PENDING_CANCELLATION);
        rental.setCancelReason(cancelReason);
        rental.setUpdatedAt(LocalDateTime.now());

        return rentalRepository.save(rental);
    }

    @Transactional
    public Rental confirmCancellation(Long rentalId) {
        Rental rental = findById(rentalId);

        if (rental.getStatus() != RentalStatus.PENDING_CANCELLATION) {
            throw new RuntimeException("Можно подтвердить только запрос на отмену");
        }

        rental.setStatus(RentalStatus.CANCELLED);
        rental.setUpdatedAt(LocalDateTime.now());

        // Освобождаем автомобиль, если он был занят
        Car car = rental.getCar();
        car.setAvailable(true);
        carService.updateCar(car);

        return rentalRepository.save(rental);
    }

    @Transactional
    public Rental rejectCancellation(Long rentalId, String adminNotes) {
        Rental rental = findById(rentalId);

        if (rental.getStatus() != RentalStatus.PENDING_CANCELLATION) {
            throw new RuntimeException("Можно отклонить только запрос на отмену");
        }

        // Возвращаем предыдущий статус (ACTIVE или PENDING)
        if (rental.getStartDate().toLocalDate().isEqual(LocalDate.now()) ||
                rental.getStartDate().toLocalDate().isBefore(LocalDate.now())) {
            rental.setStatus(RentalStatus.ACTIVE);
        } else {
            rental.setStatus(RentalStatus.PENDING);
        }

        // Добавляем примечание администратора
        if (adminNotes != null && !adminNotes.isEmpty()) {
            String notes = rental.getNotes() != null ? rental.getNotes() + "\n\n" : "";
            notes += "Отказ в отмене: " + adminNotes;
            rental.setNotes(notes);
        }

        rental.setUpdatedAt(LocalDateTime.now());
        return rentalRepository.save(rental);
    }

    @Transactional
    public Rental updateRentalNotes(Long rentalId, String notes) {
        Rental rental = findById(rentalId);
        rental.setNotes(notes);
        rental.setUpdatedAt(LocalDateTime.now());
        return rentalRepository.save(rental);
    }

    @Transactional
    public void synchronizeAllCarStatuses() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        // 1. Находим все активные аренды, которые должны были закончиться
        List<Rental> completedRentals = rentalRepository.findByStatusAndEndDateBefore(
                RentalStatus.ACTIVE, now);
        for (Rental rental : completedRentals) {
            rental.setStatus(RentalStatus.COMPLETED);
            rental.setActualReturnDate(now);
            rental.setUpdatedAt(now);

            // Освобождаем автомобиль
            Car car = rental.getCar();

            // Проверяем, нет ли других активных аренд для этого автомобиля
            List<Rental> otherActiveRentals = rentalRepository.findByCarAndStatusAndIdNot(
                    car, RentalStatus.ACTIVE, rental.getId());

            if (otherActiveRentals.isEmpty()) {
                car.setAvailable(true);
                carService.updateCar(car);
            }

            rentalRepository.save(rental);
        }

        // 2. Находим все ожидающие аренды, которые должны начаться сегодня
        LocalDateTime startOfDay = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(today, LocalTime.MAX);

        List<Rental> startingRentals = rentalRepository.findByStatusAndStartDateBetween(
                RentalStatus.PENDING, startOfDay, endOfDay);
        for (Rental rental : startingRentals) {
            rental.setStatus(RentalStatus.ACTIVE);
            rental.setUpdatedAt(now);

            // Занимаем автомобиль
            Car car = rental.getCar();
            car.setAvailable(false);
            carService.updateCar(car);

            rentalRepository.save(rental);
        }

        // 3. Проверяем все автомобили на наличие активных аренд
        List<Car> allCars = carRepository.findAll();
        for (Car car : allCars) {
            List<Rental> activeRentals = rentalRepository.findByCarAndStatusIn(
                    car, List.of(RentalStatus.ACTIVE, RentalStatus.PENDING));

            boolean shouldBeAvailable = activeRentals.isEmpty();

            // Если статус не соответствует наличию активных аренд, исправляем
            if (car.isAvailable() != shouldBeAvailable) {
                car.setAvailable(shouldBeAvailable);
                carRepository.save(car);
            }
        }
    }

    public double calculateTotalRevenue() {
        return findAllRentals().stream()
                .filter(rental -> rental.getStatus() == RentalStatus.COMPLETED)
                .mapToDouble(rental -> rental.getTotalCost().doubleValue())
                .sum();
    }

    public double calculateConversionRate() {
        List<Rental> allRentals = findAllRentals();
        if (allRentals.isEmpty()) return 0.0;
        
        long completedCount = allRentals.stream()
                .filter(rental -> rental.getStatus() == RentalStatus.COMPLETED)
                .count();
        
        return (double) completedCount / allRentals.size() * 100;
    }

    public Map<String, Integer> getMonthlyRentalStats() {
        Map<String, Integer> stats = new HashMap<>();
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        
        findAllRentals().stream()
                .filter(rental -> rental.getStartDate().isAfter(sixMonthsAgo))
                .forEach(rental -> {
                    String month = rental.getStartDate().getMonth().toString();
                    stats.merge(month, 1, Integer::sum);
                });
        
        return stats;
    }

    public Map<String, Integer> getRentalStatusDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        
        findAllRentals().forEach(rental -> {
            String status = rental.getStatus().name();
            distribution.merge(status, 1, Integer::sum);
        });
        
        return distribution;
    }

    public List<Rental> findRentalsByCar(Car car) {
        return rentalRepository.findByCar(car);
    }

    @Transactional
    public Rental setRating(Long rentalId, BigDecimal rating, String comment) {
        Rental rental = findById(rentalId);
        
        if (rental.getStatus() != RentalStatus.COMPLETED) {
            throw new RuntimeException("Можно оставить оценку только для завершенной аренды");
        }
        
        rental.setRating(rating);
        rental.setComment(comment);
        rental.setUpdatedAt(LocalDateTime.now());
        
        return rentalRepository.save(rental);
    }
}