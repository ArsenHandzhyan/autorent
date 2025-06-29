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
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;

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
            logger.info("Результат валидации платежа ID {}: valid={}, error={}", payment.getId(), validation.isValid(), validation.getErrorMessage());

            // Списываем средства всегда, даже при превышении лимита
            try {
                accountService.updateBalance(account.getUser().getId(), paymentAmount.negate());
                logger.info("Списание средств прошло успешно для платежа ID {}. Сумма: {}. Новый баланс: {}", payment.getId(), paymentAmount, newBalance);
            } catch (Exception ex) {
                logger.error("Техническая ошибка при списании средств для платежа ID {}: {}", payment.getId(), ex.getMessage(), ex);
                // Техническая ошибка — платеж FAILED
                payment.setStatus(DailyPayment.PaymentStatus.FAILED);
                payment.setProcessedAt(LocalDateTime.now());
                payment.setNotes("Техническая ошибка при списании: " + ex.getMessage());
                dailyPaymentRepository.save(payment);
                eventPublisher.publishEvent(new PaymentNotificationEvent(payment, false, "Техническая ошибка: " + ex.getMessage()));
                return;
            }

            // Проверяем превышение лимита (логика теперь в AccountService, но для notes и уведомлений фиксируем явно)
            boolean creditLimitExceeded = false;
            boolean creditLimitWarning = false;
            BigDecimal creditLimit = account.getCreditLimit();
            BigDecimal balance = account.getBalance();
            if (creditLimit.compareTo(BigDecimal.ZERO) > 0) {
                // Превышение лимита: баланс отрицательный и модуль больше лимита
                if (balance.compareTo(BigDecimal.ZERO) < 0 && balance.abs().compareTo(creditLimit) > 0) {
                    creditLimitExceeded = true;
                    logger.warn("Платеж ID {} обработан с превышением кредитного лимита! Баланс: {}, лимит: {}", payment.getId(), balance, creditLimit);
                    payment.setNotes((payment.getNotes() != null ? payment.getNotes() + "\n" : "") +
                        "ВНИМАНИЕ: Превышен кредитный лимит! Баланс: " + balance + ", лимит: " + creditLimit);
                    // Уведомления
                    notificationService.sendPaymentWarningNotification(payment, "Превышен кредитный лимит!");
                    notificationService.sendAdminPaymentWarningNotification(payment, "Превышен кредитный лимит!");
                } else if (balance.compareTo(BigDecimal.ZERO) < 0) {
                    // Предупреждение: если осталось менее 10% лимита
                    BigDecimal limitThreshold = creditLimit.multiply(new BigDecimal("0.1"));
                    BigDecimal available = creditLimit.subtract(balance.abs());
                    if (available.compareTo(limitThreshold) <= 0 && available.compareTo(BigDecimal.ZERO) > 0) {
                        creditLimitWarning = true;
                        String warnMsg = "ВНИМАНИЕ: Баланс близок к кредитному лимиту! Осталось: " + available + " ₽ из " + creditLimit + " ₽.";
                        payment.setNotes((payment.getNotes() != null ? payment.getNotes() + "\n" : "") + warnMsg);
                        notificationService.sendPaymentWarningNotification(payment, warnMsg);
                        notificationService.sendAdminPaymentWarningNotification(payment, warnMsg);
                    }
                }
            }

            // Если нет технической ошибки — платеж всегда PROCESSED
            payment.setStatus(DailyPayment.PaymentStatus.PROCESSED);
            payment.setProcessedAt(LocalDateTime.now());
            if (!creditLimitExceeded) {
                payment.setNotes((payment.getNotes() != null ? payment.getNotes() + "\n" : "") + "Платеж успешно обработан. Новый баланс: " + account.getBalance() + " ₽");
            }
            dailyPaymentRepository.save(payment);
            logger.info("Платеж ID {} успешно обработан. Списано: {}, новый баланс: {}", payment.getId(), paymentAmount, account.getBalance());
            eventPublisher.publishEvent(new PaymentNotificationEvent(payment, true, creditLimitExceeded ? "Превышен кредитный лимит" : null));
        } catch (Exception e) {
            logger.error("Техническая ошибка при обработке платежа ID {}: {}. Stacktrace:", payment.getId(), e.getMessage(), e);
            logger.error("Детали платежа: ID={}, amount={}, accountId={}, userId={}, status={}, notes={}", payment.getId(), payment.getAmount(), payment.getAccount() != null ? payment.getAccount().getId() : null, payment.getAccount() != null && payment.getAccount().getUser() != null ? payment.getAccount().getUser().getId() : null, payment.getStatus(), payment.getNotes());
            payment.setStatus(DailyPayment.PaymentStatus.FAILED);
            payment.setProcessedAt(LocalDateTime.now());
            payment.setNotes("Техническая ошибка: " + e.getMessage());
            dailyPaymentRepository.save(payment);
            eventPublisher.publishEvent(new PaymentNotificationEvent(payment, false, "Техническая ошибка: " + e.getMessage()));
        }
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
     * Валидация платежа с учетом настроек счета и уведомлений
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
                    // Отправляем уведомление администратору и пользователю о превышении лимита
                    sendCreditLimitExceededNotifications(account, paymentAmount, availableCredit);
                    // НЕ возвращаем failed! Только логируем и уведомляем, но разрешаем списание
                    return PaymentValidationResult.success();
                }
            }
            // Если кредитный лимит не установлен или не превышен, разрешаем списание
            return PaymentValidationResult.success();
        } else {
            // Отрицательный баланс не разрешен
            if (currentBalance.compareTo(paymentAmount) < 0) {
                // Отправляем уведомление о недостатке средств
                sendInsufficientFundsNotifications(account, paymentAmount, currentBalance);
                return PaymentValidationResult.failed(
                    "Недостаточно средств на счете. Баланс: " + currentBalance + " ₽, требуется: " + paymentAmount + " ₽. " +
                    "Обратитесь к администратору для пополнения счета или разрешения отрицательного баланса."
                );
            }
            return PaymentValidationResult.success();
        }
    }

    /**
     * Отправляет уведомления о превышении кредитного лимита
     */
    private void sendCreditLimitExceededNotifications(Account account, BigDecimal paymentAmount, BigDecimal availableCredit) {
        try {
            User user = account.getUser();
            String message = String.format(
                "Превышен кредитный лимит для пользователя %s %s (ID: %d). " +
                "Требуется: %s ₽, доступно: %s ₽. " +
                "Средства будут списаны, но необходимо пополнить счет.",
                user.getFirstName(), user.getLastName(), user.getId(),
                paymentAmount, availableCredit
            );
            
            // Уведомление администратора через существующий метод
            notificationService.sendAdminPaymentWarningNotification(null, message);
            
            // Уведомление пользователя через email
            notificationService.sendPaymentWarningNotification(null, 
                "Ваш кредитный лимит превышен. Средства будут списаны, но необходимо пополнить счет.");
            
            logger.warn("Отправлены уведомления о превышении кредитного лимита для пользователя ID {}", user.getId());
        } catch (Exception e) {
            logger.error("Ошибка при отправке уведомлений о превышении кредитного лимита: {}", e.getMessage());
        }
    }

    /**
     * Отправляет уведомления о недостатке средств
     */
    private void sendInsufficientFundsNotifications(Account account, BigDecimal paymentAmount, BigDecimal currentBalance) {
        try {
            User user = account.getUser();
            String message = String.format(
                "Недостаточно средств для пользователя %s %s (ID: %d). " +
                "Баланс: %s ₽, требуется: %s ₽. " +
                "Средства будут списаны, но необходимо пополнить счет.",
                user.getFirstName(), user.getLastName(), user.getId(),
                currentBalance, paymentAmount
            );
            
            // Уведомление администратора через существующий метод
            notificationService.sendAdminPaymentWarningNotification(null, message);
            
            // Уведомление пользователя через email
            notificationService.sendPaymentWarningNotification(null, 
                "На вашем счете недостаточно средств. Средства будут списаны, но необходимо пополнить счет.");
            
            logger.warn("Отправлены уведомления о недостатке средств для пользователя ID {}", user.getId());
        } catch (Exception e) {
            logger.error("Ошибка при отправке уведомлений о недостатке средств: {}", e.getMessage());
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
        logger.info("=== ОБРАБОТКА КОНКРЕТНОГО ПЛАТЕЖА ID {} ===", paymentId);
        
        DailyPayment payment = dailyPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Платеж с ID " + paymentId + " не найден"));
        
        logger.info("Найден платеж: ID={}, статус={}, сумма={}, дата={}", 
                   payment.getId(), payment.getStatus(), payment.getAmount(), payment.getPaymentDate());
        
        if (payment.getStatus() == DailyPayment.PaymentStatus.PROCESSED) {
            logger.warn("Платеж ID {} уже обработан, пропускаем", paymentId);
            return;
        }
        
        if (payment.getStatus() != DailyPayment.PaymentStatus.PENDING && 
            payment.getStatus() != DailyPayment.PaymentStatus.FAILED) {
            logger.warn("Платеж ID {} имеет статус {}, который не подлежит обработке", 
                       paymentId, payment.getStatus());
            return;
        }
        
        try {
            // Обрабатываем платеж в отдельной транзакции
            processPaymentInNewTransaction(payment);
            logger.info("Платеж ID {} успешно обработан", paymentId);
            
        } catch (Exception e) {
            logger.error("Ошибка при обработке платежа ID {}: {}", paymentId, e.getMessage(), e);
            throw new RuntimeException("Не удалось обработать платеж ID " + paymentId + ": " + e.getMessage(), e);
        }
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
                
                // Логируем статус аренды, но не пропускаем платежи
                if (payment.getRental().getStatus() != RentalStatus.ACTIVE) {
                    logger.warn("Обрабатываем платеж ID {} для неактивной аренды {} (статус: {})", 
                               payment.getId(), payment.getRental().getId(), payment.getRental().getStatus());
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
        
        // Получаем все необработанные платежи за период, независимо от статуса аренды
        List<DailyPayment> allPayments = dailyPaymentRepository.findAll();
        List<DailyPayment> missedPayments = allPayments.stream()
                .filter(p -> p.getPaymentDate().compareTo(startDate) >= 0 && 
                           p.getPaymentDate().compareTo(endDate) <= 0 &&
                           (p.getStatus() == DailyPayment.PaymentStatus.PENDING || 
                            p.getStatus() == DailyPayment.PaymentStatus.FAILED))
                .collect(java.util.stream.Collectors.toList());
        
        logger.info("Найдено {} пропущенных платежей за период", missedPayments.size());
        
        if (missedPayments.isEmpty()) {
            logger.info("Нет пропущенных платежей за указанный период");
            return;
        }
        
        int processedCount = 0;
        int failedCount = 0;
        
        for (DailyPayment payment : missedPayments) {
            try {
                logger.info("Обработка пропущенного платежа ID {} (дата: {}, сумма: {}, статус аренды: {})", 
                           payment.getId(), payment.getPaymentDate(), payment.getAmount(), 
                           payment.getRental().getStatus());
                
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
     * Создает платежи на день вперед при создании новой аренды
     */
    @Transactional
    public void createAdvancePaymentForRental(Rental rental) {
        logger.info("Создание авансового платежа для аренды ID {}", rental.getId());
        
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        
        // Проверяем, не создан ли уже платеж на завтра
        Optional<DailyPayment> existingPayment = dailyPaymentRepository.findByRentalAndPaymentDate(rental, tomorrow);
        if (existingPayment.isPresent()) {
            logger.info("Платеж на {} для аренды {} уже существует", tomorrow, rental.getId());
            return;
        }
        
        // Создаем платеж на завтра
        DailyPayment advancePayment = createDailyPayment(rental, tomorrow);
        logger.info("Создан авансовый платеж ID {} на сумму {} для аренды {} на дату {}", 
                   advancePayment.getId(), advancePayment.getAmount(), rental.getId(), tomorrow);
        
        // Сразу обрабатываем авансовый платеж
        try {
            processPayment(advancePayment);
            logger.info("Авансовый платеж ID {} успешно обработан", advancePayment.getId());
        } catch (Exception e) {
            logger.error("Ошибка при обработке авансового платежа ID {}: {}", advancePayment.getId(), e.getMessage());
        }
    }

    /**
     * Обрабатывает все пропущенные платежи при запуске сервиса
     */
    @EventListener(ApplicationReadyEvent.class)
    public void processAllMissedPaymentsOnStartup() {
        logger.info("=== ОБРАБОТКА ПРОПУЩЕННЫХ ПЛАТЕЖЕЙ ПРИ СТАРТЕ СЕРВИСА ===");
        
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
        
        // Обрабатываем все текущие необработанные платежи
        logger.info("Обработка всех текущих необработанных платежей");
        processAllUnprocessedPayments();
        
        logger.info("Обработка пропущенных платежей при старте завершена");
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