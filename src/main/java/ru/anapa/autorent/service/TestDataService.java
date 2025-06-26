package ru.anapa.autorent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.anapa.autorent.model.*;
import ru.anapa.autorent.repository.DailyPaymentRepository;
import ru.anapa.autorent.repository.RentalRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TestDataService {

    private static final Logger logger = LoggerFactory.getLogger(TestDataService.class);

    private final DailyPaymentRepository dailyPaymentRepository;
    private final RentalRepository rentalRepository;
    private final AccountService accountService;

    @Autowired
    public TestDataService(DailyPaymentRepository dailyPaymentRepository,
                          RentalRepository rentalRepository,
                          AccountService accountService) {
        this.dailyPaymentRepository = dailyPaymentRepository;
        this.rentalRepository = rentalRepository;
        this.accountService = accountService;
    }

    /**
     * Очищает все платежи и создает новые тестовые данные
     */
    @Transactional
    public void cleanAndCreateTestPayments() {
        logger.info("=== ОЧИСТКА И СОЗДАНИЕ ТЕСТОВЫХ ДАННЫХ ПЛАТЕЖЕЙ ===");
        
        // 1. Очистка всех платежей
        long deletedCount = dailyPaymentRepository.count();
        dailyPaymentRepository.deleteAll();
        logger.info("Удалено {} существующих платежей", deletedCount);
        
        // 2. Получение активных аренд
        List<Rental> activeRentals = rentalRepository.findByStatus(RentalStatus.ACTIVE);
        if (activeRentals.isEmpty()) {
            logger.warn("Нет активных аренд для создания тестовых платежей");
            return;
        }
        
        logger.info("Найдено {} активных аренд", activeRentals.size());
        
        // 3. Создание тестовых платежей
        createTestPayments(activeRentals.get(0));
        
        logger.info("=== ТЕСТОВЫЕ ДАННЫЕ СОЗДАНЫ УСПЕШНО ===");
    }

    /**
     * Создает тестовые платежи для указанной аренды
     */
    private void createTestPayments(Rental rental) {
        logger.info("Создание тестовых платежей для аренды ID: {}", rental.getId());
        
        Account account = accountService.getAccountByUserId(rental.getUser().getId());
        if (account == null) {
            logger.error("Счет не найден для пользователя ID: {}", rental.getUser().getId());
            return;
        }
        
        LocalDate today = LocalDate.now();
        
        // Платеж 1: Вчера (обработанный)
        createTestPayment(rental, account, today.minusDays(1), 
                         BigDecimal.valueOf(1500.00), 
                         DailyPayment.PaymentStatus.PROCESSED,
                         today.minusDays(1).atTime(8, 0),
                         today.minusDays(1).atTime(9, 0),
                         "Платеж успешно обработан");
        
        // Платеж 2: Сегодня (обработанный)
        createTestPayment(rental, account, today, 
                         BigDecimal.valueOf(1500.00), 
                         DailyPayment.PaymentStatus.PROCESSED,
                         today.atTime(6, 0),
                         today.atTime(7, 0),
                         "Платеж успешно обработан");
        
        // Платеж 3: Завтра (ожидающий обработки)
        createTestPayment(rental, account, today.plusDays(1), 
                         BigDecimal.valueOf(1500.00), 
                         DailyPayment.PaymentStatus.PENDING,
                         LocalDateTime.now(),
                         null,
                         null);
        
        // Платеж 4: Послезавтра (ожидающий обработки)
        createTestPayment(rental, account, today.plusDays(2), 
                         BigDecimal.valueOf(1500.00), 
                         DailyPayment.PaymentStatus.PENDING,
                         LocalDateTime.now(),
                         null,
                         null);
        
        // Платеж 5: 3 дня назад (обработанный с предупреждением о превышении лимита)
        createTestPayment(rental, account, today.minusDays(3), 
                         BigDecimal.valueOf(1500.00), 
                         DailyPayment.PaymentStatus.PROCESSED,
                         today.minusDays(3).atTime(7, 30),
                         today.minusDays(3).atTime(8, 0),
                         "Платеж обработан с предупреждением: превышен кредитный лимит");
        
        // Платеж 6: 4 дня назад (обработанный с предупреждением о недостатке средств)
        createTestPayment(rental, account, today.minusDays(4), 
                         BigDecimal.valueOf(1500.00), 
                         DailyPayment.PaymentStatus.PROCESSED,
                         today.minusDays(4).atTime(7, 15),
                         today.minusDays(4).atTime(8, 0),
                         "Платеж обработан с предупреждением: недостаточно средств на счете");
        
        // Платеж 7: 5 дней назад (техническая ошибка - FAILED)
        createTestPayment(rental, account, today.minusDays(5), 
                         BigDecimal.valueOf(1500.00), 
                         DailyPayment.PaymentStatus.FAILED,
                         today.minusDays(5).atTime(7, 0),
                         today.minusDays(5).atTime(8, 0),
                         "Техническая ошибка при списании: временная недоступность системы");
        
        // Платеж 8: 6 дней назад (обработанный)
        createTestPayment(rental, account, today.minusDays(6), 
                         BigDecimal.valueOf(1500.00), 
                         DailyPayment.PaymentStatus.PROCESSED,
                         today.minusDays(6).atTime(7, 45),
                         today.minusDays(6).atTime(8, 0),
                         "Платеж успешно обработан");
        
        // Платеж 9: 7 дней назад (обработанный)
        createTestPayment(rental, account, today.minusDays(7), 
                         BigDecimal.valueOf(1500.00), 
                         DailyPayment.PaymentStatus.PROCESSED,
                         today.minusDays(7).atTime(7, 40),
                         today.minusDays(7).atTime(8, 0),
                         "Платеж успешно обработан");
        
        logger.info("Создано 9 тестовых платежей для аренды ID: {}", rental.getId());
    }

    /**
     * Создает один тестовый платеж
     */
    private void createTestPayment(Rental rental, Account account, LocalDate paymentDate, 
                                 BigDecimal amount, DailyPayment.PaymentStatus status,
                                 LocalDateTime createdAt, LocalDateTime processedAt, String notes) {
        DailyPayment payment = new DailyPayment();
        payment.setRental(rental);
        payment.setAccount(account);
        payment.setPaymentDate(paymentDate);
        payment.setAmount(amount);
        payment.setStatus(status);
        payment.setCreatedAt(createdAt);
        payment.setProcessedAt(processedAt);
        payment.setNotes(notes);
        
        DailyPayment savedPayment = dailyPaymentRepository.save(payment);
        logger.info("Создан тестовый платеж ID: {} на дату: {} со статусом: {}", 
                   savedPayment.getId(), paymentDate, status);
    }

    /**
     * Показывает статистику платежей
     */
    public void showPaymentStatistics() {
        logger.info("=== СТАТИСТИКА ПЛАТЕЖЕЙ ===");
        
        long totalPayments = dailyPaymentRepository.count();
        long pendingPayments = dailyPaymentRepository.countByStatus(DailyPayment.PaymentStatus.PENDING);
        long processedPayments = dailyPaymentRepository.countByStatus(DailyPayment.PaymentStatus.PROCESSED);
        long failedPayments = dailyPaymentRepository.countByStatus(DailyPayment.PaymentStatus.FAILED);
        
        logger.info("Всего платежей: {}", totalPayments);
        logger.info("Ожидающих обработки: {}", pendingPayments);
        logger.info("Обработанных: {}", processedPayments);
        logger.info("Ошибок: {}", failedPayments);
        
        // Показываем последние платежи
        List<DailyPayment> recentPayments = dailyPaymentRepository.findTop10ByOrderByPaymentDateDesc();
        logger.info("Последние платежи:");
        for (DailyPayment payment : recentPayments) {
            logger.info("  ID: {}, Дата: {}, Сумма: {}, Статус: {}, Примечания: {}", 
                       payment.getId(), payment.getPaymentDate(), payment.getAmount(), 
                       payment.getStatus(), payment.getNotes());
        }
    }
} 