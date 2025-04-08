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

    public List<Rental> findAllRentals() {
        return rentalRepository.findAll();
    }

    public List<Rental> findRentalsByUser(User user) {
        return rentalRepository.findByUser(user);
    }

    public List<Rental> findRentalsByStatus(String status) {
        return rentalRepository.findByStatus(status);
    }

    public Rental findById(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Аренда не найдена"));
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

        Rental savedRental = rentalRepository.save(rental);

        // Если аренда начинается сегодня, сразу обновляем статус автомобиля
        if (startDate.toLocalDate().equals(LocalDateTime.now().toLocalDate())) {
            carService.updateCarAvailability(car.getId(), false);
        }

        return savedRental;
    }

    @Transactional
    public Rental updateRental(Rental rental) {
        rental.setUpdatedAt(LocalDateTime.now());
        return rentalRepository.save(rental);
    }

    @Transactional
    public void approveRental(Long rentalId) {
        Rental rental = findById(rentalId);

        if (!"PENDING".equals(rental.getStatus())) {
            throw new RuntimeException("Невозможно подтвердить аренду: неверный статус");
        }

        rental.setStatus("ACTIVE");
        rental.setUpdatedAt(LocalDateTime.now());
        rentalRepository.save(rental);
    }

    @Transactional
    public void completeRental(Long rentalId) {
        Rental rental = findById(rentalId);

        if (!"ACTIVE".equals(rental.getStatus())) {
            throw new RuntimeException("Невозможно завершить аренду: неверный статус");
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

        // Проверяем, что аренду можно отменить
        if ("COMPLETED".equals(rental.getStatus()) || "CANCELLED".equals(rental.getStatus())) {
            throw new RuntimeException("Невозможно отменить аренду: неверный статус");
        }

        rental.setStatus("CANCELLED");
        rental.setUpdatedAt(LocalDateTime.now());

        // Освобождаем автомобиль
        Car car = rental.getCar();
        car.setAvailable(true);
        carService.updateCar(car);

        rentalRepository.save(rental);
    }

    /**
     * Запрос на отмену аренды от пользователя
     * Изменяет статус аренды на PENDING_CANCELLATION
     */
    @Transactional
    public void requestCancellation(Long rentalId, String reason) {
        Rental rental = findById(rentalId);

        // Проверяем, что аренду можно отменить
        if ("COMPLETED".equals(rental.getStatus()) || "CANCELLED".equals(rental.getStatus()) ||
                "PENDING_CANCELLATION".equals(rental.getStatus())) {
            throw new RuntimeException("Невозможно запросить отмену аренды: неверный статус");
        }

        rental.setStatus("PENDING_CANCELLATION");

        // Добавляем причину отмены в примечания
        if (reason != null && !reason.trim().isEmpty()) {
            String notes = rental.getNotes() != null ? rental.getNotes() + "\n\n" : "";
            notes += "Причина запроса на отмену: " + reason;
            rental.setNotes(notes);
        }

        rental.setUpdatedAt(LocalDateTime.now());
        rentalRepository.save(rental);
    }

    /**
     * Отклонение запроса на отмену аренды администратором
     * Возвращает статус аренды в предыдущее состояние (ACTIVE или PENDING)
     */
    @Transactional
    public void rejectCancellationRequest(Long rentalId, String adminNotes) {
        Rental rental = findById(rentalId);

        // Проверяем, что аренда находится в статусе ожидания отмены
        if (!"PENDING_CANCELLATION".equals(rental.getStatus())) {
            throw new RuntimeException("Невозможно отклонить запрос на отмену: неверный статус аренды");
        }

        // Определяем, какой статус был до запроса на отмену
        // Для простоты предположим, что это был ACTIVE или PENDING
        // В реальном приложении можно хранить предыдущий статус в отдельном поле
        LocalDateTime now = LocalDateTime.now();
        String newStatus = now.isBefore(rental.getStartDate()) ? "PENDING" : "ACTIVE";
        rental.setStatus(newStatus);

        // Добавляем примечания администратора
        if (adminNotes != null && !adminNotes.trim().isEmpty()) {
            String notes = rental.getNotes() != null ? rental.getNotes() + "\n\n" : "";
            notes += "Запрос на отмену отклонен администратором: " + adminNotes;
            rental.setNotes(notes);
        }

        rental.setUpdatedAt(LocalDateTime.now());
        rentalRepository.save(rental);
    }
}