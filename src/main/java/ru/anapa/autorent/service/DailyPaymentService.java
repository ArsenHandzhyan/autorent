package ru.anapa.autorent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.anapa.autorent.model.*;
import ru.anapa.autorent.repository.DailyPaymentRepository;
import ru.anapa.autorent.repository.RentalRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DailyPaymentService {

    private static final Logger logger = LoggerFactory.getLogger(DailyPaymentService.class);

    private final DailyPaymentRepository dailyPaymentRepository;
    private final RentalRepository rentalRepository;
    private final AccountService accountService;
    private final PaymentNotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public DailyPaymentService(DailyPaymentRepository dailyPaymentRepository,
                              RentalRepository rentalRepository,
                              AccountService accountService,
                              @Lazy PaymentNotificationService notificationService,
                              ApplicationEventPublisher eventPublisher) {
        this.dailyPaymentRepository = dailyPaymentRepository;
        this.rentalRepository = rentalRepository;
        this.accountService = accountService;
        this.notificationService = notificationService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Создает ежедневные платежи для активных аренд
     */
    @Transactional
    public void createDailyPaymentsForActiveRentals(LocalDate paymentDate) {
        logger.info("Создание ежедневных платежей для даты: {}", paymentDate);
        
        // Получаем все активные аренды
        List<Rental> activeRentals = rentalRepository.findByStatus(RentalStatus.ACTIVE);
        
        for (Rental rental : activeRentals) {
            // Проверяем, что дата платежа попадает в период аренды
            if (isPaymentDateInRentalPeriod(rental, paymentDate)) {
                createDailyPayment(rental, paymentDate);
            }
        }
    }

    /**
     * Обрабатывает все ожидающие платежи на указанную дату
     */
    @Transactional
    public void processDailyPayments(LocalDate paymentDate) {
        logger.info("Обработка ежедневных платежей для даты: {}", paymentDate);
        
        List<DailyPayment> pendingPayments = dailyPaymentRepository.findActiveRentalsPendingPayments(paymentDate);
        
        for (DailyPayment payment : pendingPayments) {
            try {
                processPayment(payment);
            } catch (Exception e) {
                logger.error("Ошибка при обработке платежа ID {}: {}", payment.getId(), e.getMessage());
                markPaymentAsFailed(payment, e.getMessage());
            }
        }
    }

    /**
     * Создает ежедневный платеж для конкретной аренды
     */
    @Transactional
    public DailyPayment createDailyPayment(Rental rental, LocalDate paymentDate) {
        // Проверяем, не создан ли уже платеж на эту дату
        Optional<DailyPayment> existingPayment = dailyPaymentRepository.findByRentalAndPaymentDate(rental, paymentDate);
        if (existingPayment.isPresent()) {
            logger.info("Платеж для аренды {} на дату {} уже существует", rental.getId(), paymentDate);
            return existingPayment.get();
        }

        // Получаем счет пользователя
        Account account = accountService.getAccountByUserId(rental.getUser().getId());
        
        // Рассчитываем стоимость за день
        BigDecimal dailyAmount = calculateDailyAmount(rental);
        
        DailyPayment payment = new DailyPayment();
        payment.setRental(rental);
        payment.setAccount(account);
        payment.setPaymentDate(paymentDate);
        payment.setAmount(dailyAmount);
        payment.setStatus(DailyPayment.PaymentStatus.PENDING);
        
        DailyPayment savedPayment = dailyPaymentRepository.save(payment);
        logger.info("Создан ежедневный платеж ID {} для аренды {} на сумму {}", 
                   savedPayment.getId(), rental.getId(), dailyAmount);
        
        return savedPayment;
    }

    /**
     * Обрабатывает конкретный платеж
     */
    @Transactional
    public void processPayment(DailyPayment payment) {
        logger.info("Обработка платежа ID {} на сумму {}", payment.getId(), payment.getAmount());
        
        try {
            Account account = payment.getAccount();
            BigDecimal paymentAmount = payment.getAmount();
            BigDecimal currentBalance = account.getBalance();
            BigDecimal newBalance = currentBalance.subtract(paymentAmount);
            
            // Валидируем платеж
            PaymentValidationResult validation = validatePayment(account, paymentAmount);
            
            if (validation.isValid()) {
                // Платеж валиден - списываем средства
                accountService.updateBalance(account.getUser().getId(), paymentAmount.negate());
                payment.setStatus(DailyPayment.PaymentStatus.PROCESSED);
                payment.setProcessedAt(LocalDateTime.now());
                payment.setNotes("Платеж успешно обработан. Новый баланс: " + newBalance + " ₽");
                dailyPaymentRepository.save(payment);
                
                logger.info("Платеж ID {} успешно обработан. Списано: {}, новый баланс: {}", 
                           payment.getId(), paymentAmount, newBalance);
                
                // Публикуем событие успешного платежа
                eventPublisher.publishEvent(new PaymentNotificationEvent(payment, true, null));
                
            } else {
                // Платеж не прошел валидацию - отмечаем как неудачный
                payment.setStatus(DailyPayment.PaymentStatus.FAILED);
                payment.setProcessedAt(LocalDateTime.now());
                payment.setNotes("Ошибка валидации: " + validation.getErrorMessage());
                dailyPaymentRepository.save(payment);
                
                logger.warn("Платеж ID {} не прошел валидацию: {}", payment.getId(), validation.getErrorMessage());
                
                // Публикуем событие неудачного платежа
                eventPublisher.publishEvent(new PaymentNotificationEvent(payment, false, validation.getErrorMessage()));
            }
            
        } catch (Exception e) {
            logger.error("Техническая ошибка при обработке платежа ID {}: {}", payment.getId(), e.getMessage());
            
            // Отмечаем платеж как неудачный из-за технической ошибки
            payment.setStatus(DailyPayment.PaymentStatus.FAILED);
            payment.setProcessedAt(LocalDateTime.now());
            payment.setNotes("Техническая ошибка: " + e.getMessage());
            dailyPaymentRepository.save(payment);
            
            // Публикуем событие технической ошибки
            eventPublisher.publishEvent(new PaymentNotificationEvent(payment, false, "Техническая ошибка: " + e.getMessage()));
        }
    }

    /**
     * Валидация платежа с учетом настроек счета
     */
    private PaymentValidationResult validatePayment(Account account, BigDecimal paymentAmount) {
        BigDecimal currentBalance = account.getBalance();
        BigDecimal newBalance = currentBalance.subtract(paymentAmount);
        
        // Если разрешен отрицательный баланс
        if (account.isAllowNegativeBalance()) {
            // Проверяем кредитный лимит
            if (account.getCreditLimit().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal availableCredit = account.getBalance().add(account.getCreditLimit());
                if (availableCredit.compareTo(paymentAmount) < 0) {
                    return PaymentValidationResult.failed(
                        "Превышен кредитный лимит. Доступно: " + availableCredit + " ₽, требуется: " + paymentAmount + " ₽"
                    );
                }
            }
            // Если кредитный лимит не установлен или не превышен, разрешаем списание
            return PaymentValidationResult.success();
        } else {
            // Отрицательный баланс не разрешен
            if (currentBalance.compareTo(paymentAmount) < 0) {
                return PaymentValidationResult.failed(
                    "Недостаточно средств на счете. Баланс: " + currentBalance + " ₽, требуется: " + paymentAmount + " ₽. " +
                    "Обратитесь к администратору для пополнения счета или разрешения отрицательного баланса."
                );
            }
            return PaymentValidationResult.success();
        }
    }

    /**
     * Отмечает платеж как неудачный
     */
    @Transactional
    public void markPaymentAsFailed(DailyPayment payment, String errorMessage) {
        payment.setStatus(DailyPayment.PaymentStatus.FAILED);
        payment.setProcessedAt(LocalDateTime.now());
        payment.setNotes("Ошибка: " + errorMessage);
        dailyPaymentRepository.save(payment);
    }

    /**
     * Получает все платежи для аренды
     */
    public List<DailyPayment> getPaymentsByRental(Rental rental) {
        return dailyPaymentRepository.findByRentalOrderByPaymentDateDesc(rental);
    }

    /**
     * Получает платежи для аренды за период
     */
    public List<DailyPayment> getPaymentsByRentalAndPeriod(Rental rental, LocalDate startDate, LocalDate endDate) {
        return dailyPaymentRepository.findByRentalAndPaymentDateBetween(rental, startDate, endDate);
    }

    /**
     * Рассчитывает стоимость за день для аренды
     */
    private BigDecimal calculateDailyAmount(Rental rental) {
        // Если у аренды есть автомобиль, используем его стоимость за день
        if (rental.getCar() != null && rental.getCar().getPricePerDay() != null) {
            return rental.getCar().getPricePerDay();
        }
        
        // Если автомобиль не указан, но есть общая стоимость аренды,
        // рассчитываем стоимость за день на основе общей стоимости и длительности
        if (rental.getTotalCost() != null && rental.getStartDate() != null && rental.getEndDate() != null) {
            long days = java.time.Duration.between(rental.getStartDate(), rental.getEndDate()).toDays();
            if (days > 0) {
                return rental.getTotalCost().divide(BigDecimal.valueOf(days), 2, BigDecimal.ROUND_HALF_UP);
            }
        }
        
        // Возвращаем 0, если не удалось рассчитать
        return BigDecimal.ZERO;
    }

    /**
     * Проверяет, попадает ли дата платежа в период аренды
     */
    private boolean isPaymentDateInRentalPeriod(Rental rental, LocalDate paymentDate) {
        if (rental.getStartDate() == null || rental.getEndDate() == null) {
            return false;
        }
        
        LocalDate rentalStartDate = rental.getStartDate().toLocalDate();
        LocalDate rentalEndDate = rental.getEndDate().toLocalDate();
        
        return !paymentDate.isBefore(rentalStartDate) && !paymentDate.isAfter(rentalEndDate);
    }

    /**
     * Получает статистику платежей для аренды
     */
    public PaymentStatistics getPaymentStatistics(Rental rental) {
        List<DailyPayment> payments = getPaymentsByRental(rental);
        
        long totalPayments = payments.size();
        long processedPayments = payments.stream()
                .filter(p -> p.getStatus() == DailyPayment.PaymentStatus.PROCESSED)
                .count();
        long failedPayments = payments.stream()
                .filter(p -> p.getStatus() == DailyPayment.PaymentStatus.FAILED)
                .count();
        long pendingPayments = payments.stream()
                .filter(p -> p.getStatus() == DailyPayment.PaymentStatus.PENDING)
                .count();
        
        BigDecimal totalAmount = payments.stream()
                .map(DailyPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal processedAmount = payments.stream()
                .filter(p -> p.getStatus() == DailyPayment.PaymentStatus.PROCESSED)
                .map(DailyPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new PaymentStatistics(totalPayments, processedPayments, failedPayments, 
                                   pendingPayments, totalAmount, processedAmount);
    }

    /**
     * Обрабатывает конкретный платеж по ID
     */
    @Transactional
    public void processSpecificPayment(Long paymentId) {
        DailyPayment payment = dailyPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Платеж с ID " + paymentId + " не найден"));
        
        if (payment.getStatus() != DailyPayment.PaymentStatus.PENDING && 
            payment.getStatus() != DailyPayment.PaymentStatus.FAILED) {
            throw new RuntimeException("Можно обработать только ожидающий или неудачный платеж");
        }
        
        processPayment(payment);
    }

    /**
     * Получает платеж по ID
     */
    public DailyPayment getPaymentById(Long paymentId) {
        return dailyPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Платеж не найден"));
    }

    /**
     * Находит все платежи пользователя
     */
    public List<DailyPayment> findPaymentsByUser(Long userId) {
        return dailyPaymentRepository.findByAccountUserIdOrderByPaymentDateDesc(userId);
    }

    /**
     * Находит все платежи пользователя с данными автомобилей
     */
    public List<DailyPayment> findPaymentsByUserWithCarData(Long userId) {
        return dailyPaymentRepository.findPaymentsByUserWithCarData(userId);
    }

    /**
     * Класс для хранения статистики платежей
     */
    public static class PaymentStatistics {
        private final long totalPayments;
        private final long processedPayments;
        private final long failedPayments;
        private final long pendingPayments;
        private final BigDecimal totalAmount;
        private final BigDecimal processedAmount;

        public PaymentStatistics(long totalPayments, long processedPayments, long failedPayments,
                               long pendingPayments, BigDecimal totalAmount, BigDecimal processedAmount) {
            this.totalPayments = totalPayments;
            this.processedPayments = processedPayments;
            this.failedPayments = failedPayments;
            this.pendingPayments = pendingPayments;
            this.totalAmount = totalAmount;
            this.processedAmount = processedAmount;
        }

        // Геттеры
        public long getTotalPayments() { return totalPayments; }
        public long getProcessedPayments() { return processedPayments; }
        public long getFailedPayments() { return failedPayments; }
        public long getPendingPayments() { return pendingPayments; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public BigDecimal getProcessedAmount() { return processedAmount; }
    }

    /**
     * Класс для результата валидации платежа
     */
    private static class PaymentValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private PaymentValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static PaymentValidationResult success() {
            return new PaymentValidationResult(true, null);
        }

        public static PaymentValidationResult failed(String errorMessage) {
            return new PaymentValidationResult(false, errorMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * Обрабатывает все платежи со статусом PENDING и FAILED (для автосписания при старте приложения и по расписанию)
     */
    @Transactional
    public void processAllUnprocessedPayments() {
        logger.info("Начинаем актуализацию всех не обработанных платежей");
        
        List<DailyPayment> pending = dailyPaymentRepository.findAllByStatus(DailyPayment.PaymentStatus.PENDING);
        List<DailyPayment> failed = dailyPaymentRepository.findAllByStatus(DailyPayment.PaymentStatus.FAILED);
        List<DailyPayment> all = new java.util.ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        
        // Фильтруем платежи по дате (только те, которые должны быть обработаны сегодня или раньше)
        for (DailyPayment p : pending) {
            if (!p.getPaymentDate().isAfter(today)) {
                all.add(p);
            }
        }
        for (DailyPayment p : failed) {
            if (!p.getPaymentDate().isAfter(today)) {
                all.add(p);
            }
        }
        
        logger.info("Найдено {} платежей для обработки (PENDING: {}, FAILED: {})", 
                   all.size(), pending.size(), failed.size());
        
        int processedCount = 0;
        int failedCount = 0;
        
        for (DailyPayment payment : all) {
            try {
                // Обрабатываем каждый платеж в отдельной транзакции
                processPaymentInNewTransaction(payment);
                processedCount++;
                logger.info("Платеж ID {} успешно обработан", payment.getId());
            } catch (Exception e) {
                failedCount++;
                logger.error("Ошибка при автосписании платежа ID {}: {}", payment.getId(), e.getMessage());
                // Отмечаем платеж как неудачный, но не прерываем обработку остальных
                try {
                    markPaymentAsFailedInNewTransaction(payment, e.getMessage());
                } catch (Exception markError) {
                    logger.error("Не удалось отметить платеж ID {} как неудачный: {}", payment.getId(), markError.getMessage());
                }
            }
        }
        
        logger.info("Актуализация завершена. Обработано: {}, Ошибок: {}", processedCount, failedCount);
    }

    /**
     * Обрабатывает платеж в новой транзакции
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void processPaymentInNewTransaction(DailyPayment payment) {
        processPayment(payment);
    }

    /**
     * Отмечает платеж как неудачный в новой транзакции
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void markPaymentAsFailedInNewTransaction(DailyPayment payment, String errorMessage) {
        markPaymentAsFailed(payment, errorMessage);
    }

    // Старый метод теперь вызывает новый
    public void processAllPendingPayments() {
        processAllUnprocessedPayments();
    }

    // Планировщик для регулярной актуализации (раз в час)
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 * * * *")
    public void scheduledUnprocessedPayments() {
        logger.info("[Scheduler] Актуализация всех не обработанных платежей (PENDING/FAILED)");
        processAllUnprocessedPayments();
    }
} 