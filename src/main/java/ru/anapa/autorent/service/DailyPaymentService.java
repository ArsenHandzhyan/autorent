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
import java.util.Map;

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
        logger.info("Обработка платежа ID {} на сумму {} (текущий статус: {})", payment.getId(), payment.getAmount(), payment.getStatus());

        // Разрешаем повторную обработку FAILED платежей
        if (payment.getStatus() == DailyPayment.PaymentStatus.FAILED) {
            logger.warn("Повторная попытка обработки платежа ID {} со статусом FAILED", payment.getId());
            payment.setStatus(DailyPayment.PaymentStatus.PENDING);
            dailyPaymentRepository.save(payment);
        }
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
                // Платеж не прошел валидацию, но средства списываем в любом случае
                // (это может быть превышение кредитного лимита или недостаток средств)
                accountService.updateBalance(account.getUser().getId(), paymentAmount.negate());
                payment.setStatus(DailyPayment.PaymentStatus.PROCESSED);
                payment.setProcessedAt(LocalDateTime.now());
                payment.setNotes("Платеж обработан с предупреждением: " + validation.getErrorMessage() + 
                               ". Новый баланс: " + newBalance + " ₽");
                dailyPaymentRepository.save(payment);
                
                logger.warn("Платеж ID {} обработан с предупреждением: {}. Списано: {}, новый баланс: {}", 
                           payment.getId(), validation.getErrorMessage(), paymentAmount, newBalance);
                
                // Публикуем событие платежа с предупреждением
                eventPublisher.publishEvent(new PaymentNotificationEvent(payment, true, validation.getErrorMessage()));
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
    public void processAllUnprocessedPayments() {
        logger.info("=== НАЧАЛО ОБРАБОТКИ ВСЕХ НЕОБРАБОТАННЫХ ПЛАТЕЖЕЙ ===");
        
        LocalDate today = LocalDate.now();
        LocalDate maxProcessDate = today.minusDays(1); // Обрабатываем платежи до вчерашнего дня включительно
        
        // Получаем все необработанные платежи
        List<DailyPayment> unprocessedPayments = dailyPaymentRepository.findUnprocessedPaymentsUpToDate(maxProcessDate);
        
        logger.info("Найдено {} необработанных платежей до даты {}", unprocessedPayments.size(), maxProcessDate);
        
        if (unprocessedPayments.isEmpty()) {
            logger.info("Нет необработанных платежей для обработки");
            return;
        }
        
        // Группируем платежи по датам для лучшего логирования
        Map<LocalDate, List<DailyPayment>> paymentsByDate = unprocessedPayments.stream()
                .collect(java.util.stream.Collectors.groupingBy(DailyPayment::getPaymentDate));
        
        logger.info("Распределение платежей по датам:");
        paymentsByDate.forEach((date, payments) -> {
            long pendingCount = payments.stream().filter(p -> p.getStatus() == DailyPayment.PaymentStatus.PENDING).count();
            long failedCount = payments.stream().filter(p -> p.getStatus() == DailyPayment.PaymentStatus.FAILED).count();
            logger.info("  {}: {} платежей (PENDING: {}, FAILED: {})", date, payments.size(), pendingCount, failedCount);
        });
        
        int processedCount = 0;
        int failedCount = 0;
        int skippedCount = 0;
        
        // Обрабатываем платежи в хронологическом порядке
        for (DailyPayment payment : unprocessedPayments) {
            try {
                logger.debug("Обработка платежа ID {} (дата: {}, статус: {}, сумма: {})", 
                           payment.getId(), payment.getPaymentDate(), payment.getStatus(), payment.getAmount());
                
                // Проверяем, что аренда все еще активна
                if (payment.getRental().getStatus() != RentalStatus.ACTIVE) {
                    logger.warn("Пропускаем платеж ID {} - аренда {} не активна (статус: {})", 
                               payment.getId(), payment.getRental().getId(), payment.getRental().getStatus());
                    skippedCount++;
                    continue;
                }
                
                // Обрабатываем каждый платеж в отдельной транзакции
                processPaymentInNewTransaction(payment);
                processedCount++;
                logger.info("Платеж ID {} успешно обработан (дата: {}, сумма: {})", 
                           payment.getId(), payment.getPaymentDate(), payment.getAmount());
                
            } catch (Exception e) {
                failedCount++;
                logger.error("Ошибка при обработке платежа ID {} (дата: {}, сумма: {}): {}", 
                           payment.getId(), payment.getPaymentDate(), payment.getAmount(), e.getMessage());
                
                // Отмечаем платеж как неудачный, но не прерываем обработку остальных
                try {
                    markPaymentAsFailedInNewTransaction(payment, "Ошибка обработки: " + e.getMessage());
                } catch (Exception markError) {
                    logger.error("Не удалось отметить платеж ID {} как неудачный: {}", payment.getId(), markError.getMessage());
                }
            }
        }
        
        logger.info("=== ЗАВЕРШЕНИЕ ОБРАБОТКИ НЕОБРАБОТАННЫХ ПЛАТЕЖЕЙ ===");
        logger.info("Итого: обработано {}, ошибок {}, пропущено {}", processedCount, failedCount, skippedCount);
    }

    /**
     * Обрабатывает пропущенные платежи за указанный период
     */
    @Transactional
    public void processMissedPaymentsForPeriod(LocalDate startDate, LocalDate endDate) {
        logger.info("=== ОБРАБОТКА ПРОПУЩЕННЫХ ПЛАТЕЖЕЙ ЗА ПЕРИОД {} - {} ===", startDate, endDate);
        
        List<DailyPayment> missedPayments = dailyPaymentRepository.findActiveRentalsUnprocessedPaymentsInPeriod(startDate, endDate);
        
        logger.info("Найдено {} пропущенных платежей за период", missedPayments.size());
        
        if (missedPayments.isEmpty()) {
            logger.info("Нет пропущенных платежей за указанный период");
            return;
        }
        
        int processedCount = 0;
        int failedCount = 0;
        
        for (DailyPayment payment : missedPayments) {
            try {
                logger.info("Обработка пропущенного платежа ID {} (дата: {}, сумма: {})", 
                           payment.getId(), payment.getPaymentDate(), payment.getAmount());
                
                processPaymentInNewTransaction(payment);
                processedCount++;
                
            } catch (Exception e) {
                failedCount++;
                logger.error("Ошибка при обработке пропущенного платежа ID {}: {}", payment.getId(), e.getMessage());
                
                try {
                    markPaymentAsFailedInNewTransaction(payment, "Ошибка обработки пропущенного платежа: " + e.getMessage());
                } catch (Exception markError) {
                    logger.error("Не удалось отметить пропущенный платеж ID {} как неудачный: {}", payment.getId(), markError.getMessage());
                }
            }
        }
        
        logger.info("Обработка пропущенных платежей завершена. Обработано: {}, ошибок: {}", processedCount, failedCount);
    }

    /**
     * Диагностика и обработка всех пропущенных платежей при старте приложения
     */
    public void processAllMissedPaymentsOnStartup() {
        logger.info("=== ОБРАБОТКА ПРОПУЩЕННЫХ ПЛАТЕЖЕЙ ПРИ СТАРТЕ ПРИЛОЖЕНИЯ ===");
        
        LocalDate today = LocalDate.now();
        
        // Получаем все даты с необработанными платежами
        List<LocalDate> unprocessedDates = dailyPaymentRepository.findUnprocessedPaymentDates(today);
        
        if (unprocessedDates.isEmpty()) {
            logger.info("Нет пропущенных платежей для обработки при старте");
            return;
        }
        
        logger.info("Найдены необработанные платежи для дат: {}", unprocessedDates);
        
        // Обрабатываем платежи за каждую дату
        for (LocalDate date : unprocessedDates) {
            logger.info("Обработка платежей за дату: {}", date);
            processMissedPaymentsForPeriod(date, date);
        }
        
        logger.info("Обработка пропущенных платежей при старте завершена");
    }

    /**
     * Улучшенная диагностика всех платежей с детальной информацией
     */
    public void diagnoseAllPayments() {
        logger.info("=== ДЕТАЛЬНАЯ ДИАГНОСТИКА ВСЕХ ПЛАТЕЖЕЙ ===");
        
        List<DailyPayment> allPayments = dailyPaymentRepository.findAll();
        logger.info("Всего платежей в системе: {}", allPayments.size());
        
        // Статистика по статусам
        Map<DailyPayment.PaymentStatus, Long> statusCounts = allPayments.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    DailyPayment::getStatus, 
                    java.util.stream.Collectors.counting()
                ));
        
        logger.info("Распределение по статусам:");
        for (DailyPayment.PaymentStatus status : DailyPayment.PaymentStatus.values()) {
            long count = statusCounts.getOrDefault(status, 0L);
            logger.info("  {}: {}", status, count);
        }
        
        // Статистика по датам
        LocalDate today = LocalDate.now();
        long overduePending = dailyPaymentRepository.countOverduePendingPayments(today);
        long overdueFailed = dailyPaymentRepository.countOverdueFailedPayments(today);
        
        logger.info("Просроченные платежи (до {}):", today);
        logger.info("  PENDING: {}", overduePending);
        logger.info("  FAILED: {}", overdueFailed);
        
        // Детали платежей с превышением кредитного лимита
        logger.info("Платежи с превышением кредитного лимита:");
        for (DailyPayment payment : allPayments) {
            if (payment.getNotes() != null && payment.getNotes().contains("кредитный лимит")) {
                logger.info("  Платеж ID {}: статус={}, сумма={}, дата={}, примечания={}", 
                           payment.getId(), payment.getStatus(), payment.getAmount(), 
                           payment.getPaymentDate(), payment.getNotes());
            }
        }
        
        // Все FAILED платежи
        logger.info("Все FAILED платежи:");
        for (DailyPayment payment : allPayments) {
            if (payment.getStatus() == DailyPayment.PaymentStatus.FAILED) {
                logger.info("  Платеж ID {}: сумма={}, дата={}, примечания={}", 
                           payment.getId(), payment.getAmount(), payment.getPaymentDate(), payment.getNotes());
            }
        }
        
        // Платежи по датам (последние 7 дней)
        logger.info("Платежи за последние 7 дней:");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            List<DailyPayment> datePayments = allPayments.stream()
                    .filter(p -> p.getPaymentDate().equals(date))
                    .collect(java.util.stream.Collectors.toList());
            
            if (!datePayments.isEmpty()) {
                Map<DailyPayment.PaymentStatus, Long> dateStatusCounts = datePayments.stream()
                        .collect(java.util.stream.Collectors.groupingBy(
                            DailyPayment::getStatus, 
                            java.util.stream.Collectors.counting()
                        ));
                
                logger.info("  {}: {} платежей (PENDING: {}, PROCESSED: {}, FAILED: {})", 
                           date, datePayments.size(),
                           dateStatusCounts.getOrDefault(DailyPayment.PaymentStatus.PENDING, 0L),
                           dateStatusCounts.getOrDefault(DailyPayment.PaymentStatus.PROCESSED, 0L),
                           dateStatusCounts.getOrDefault(DailyPayment.PaymentStatus.FAILED, 0L));
            }
        }
        
        logger.info("=== КОНЕЦ ДИАГНОСТИКИ ===");
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