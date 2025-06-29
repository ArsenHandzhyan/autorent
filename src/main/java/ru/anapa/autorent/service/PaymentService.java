package ru.anapa.autorent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.anapa.autorent.model.DailyPayment;
import ru.anapa.autorent.repository.DailyPaymentRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final DailyPaymentRepository dailyPaymentRepository;

    /**
     * Получение всех платежей с защитой от превышения лимитов
     */
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional(readOnly = true)
    public List<DailyPayment> getAllPayments() {
        log.info("Получение всех платежей");
        return dailyPaymentRepository.findAll();
    }

    /**
     * Получение платежа по ID с защитой от превышения лимитов
     */
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional(readOnly = true)
    public Optional<DailyPayment> getPaymentById(Long id) {
        log.info("Получение платежа по ID: {}", id);
        return dailyPaymentRepository.findById(id);
    }

    /**
     * Получение платежей по ID аренды с защитой от превышения лимитов
     */
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional(readOnly = true)
    public List<DailyPayment> getPaymentsByRentalId(Long rentalId) {
        log.info("Получение платежей для аренды: {}", rentalId);
        return dailyPaymentRepository.findByRentalId(rentalId);
    }

    /**
     * Сохранение платежа с защитой от превышения лимитов
     */
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional
    public DailyPayment savePayment(DailyPayment payment) {
        log.info("Сохранение платежа: {}", payment.getId());
        return dailyPaymentRepository.save(payment);
    }

    /**
     * Обновление примечания платежа с защитой от превышения лимитов
     */
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional
    public DailyPayment updatePaymentNotes(Long paymentId, String notes) {
        log.info("Обновление примечания для платежа: {}", paymentId);
        Optional<DailyPayment> paymentOpt = dailyPaymentRepository.findById(paymentId);
        if (paymentOpt.isPresent()) {
            DailyPayment payment = paymentOpt.get();
            payment.setNotes(notes);
            return dailyPaymentRepository.save(payment);
        }
        throw new RuntimeException("Платеж не найден: " + paymentId);
    }

    /**
     * Исправление поврежденных примечаний платежей
     */
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    @Transactional
    public int fixCorruptedPaymentNotes() {
        log.info("Исправление поврежденных примечаний платежей");
        
        // Получаем все платежи с поврежденными примечаниями
        List<DailyPayment> corruptedPayments = dailyPaymentRepository.findAll().stream()
            .filter(payment -> payment.getNotes() != null && 
                (payment.getNotes().contains("ÐŸÐ»Ð°Ñ‚ÐµÐ¶") || 
                 payment.getNotes().contains("Ð·Ð° Ð´ÐµÐ½ÑŒ") ||
                 payment.getNotes().contains("Ð°Ñ€ÐµÐ½Ð´Ñ‹")))
            .toList();

        log.info("Найдено {} платежей с поврежденными примечаниями", corruptedPayments.size());

        // Исправляем примечания
        for (DailyPayment payment : corruptedPayments) {
            payment.setNotes("Платеж за день аренды. Средства списаны.");
            dailyPaymentRepository.save(payment);
        }

        return corruptedPayments.size();
    }

    /**
     * Получение статистики платежей
     */
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional(readOnly = true)
    public PaymentStatistics getPaymentStatistics() {
        log.info("Получение статистики платежей");
        
        List<DailyPayment> allPayments = dailyPaymentRepository.findAll();
        
        long totalPayments = allPayments.size();
        long processedPayments = allPayments.stream()
            .filter(p -> "PROCESSED".equals(p.getStatus()))
            .count();
        long pendingPayments = allPayments.stream()
            .filter(p -> "PENDING".equals(p.getStatus()))
            .count();
        long paymentsWithNotes = allPayments.stream()
            .filter(p -> p.getNotes() != null && !p.getNotes().isEmpty())
            .count();

        return PaymentStatistics.builder()
            .totalPayments(totalPayments)
            .processedPayments(processedPayments)
            .pendingPayments(pendingPayments)
            .paymentsWithNotes(paymentsWithNotes)
            .build();
    }

    /**
     * Статистика платежей
     */
    public static class PaymentStatistics {
        private final long totalPayments;
        private final long processedPayments;
        private final long pendingPayments;
        private final long paymentsWithNotes;

        public PaymentStatistics(long totalPayments, long processedPayments, 
                               long pendingPayments, long paymentsWithNotes) {
            this.totalPayments = totalPayments;
            this.processedPayments = processedPayments;
            this.pendingPayments = pendingPayments;
            this.paymentsWithNotes = paymentsWithNotes;
        }

        // Геттеры
        public long getTotalPayments() { return totalPayments; }
        public long getProcessedPayments() { return processedPayments; }
        public long getPendingPayments() { return pendingPayments; }
        public long getPaymentsWithNotes() { return paymentsWithNotes; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private long totalPayments;
            private long processedPayments;
            private long pendingPayments;
            private long paymentsWithNotes;

            public Builder totalPayments(long totalPayments) {
                this.totalPayments = totalPayments;
                return this;
            }

            public Builder processedPayments(long processedPayments) {
                this.processedPayments = processedPayments;
                return this;
            }

            public Builder pendingPayments(long pendingPayments) {
                this.pendingPayments = pendingPayments;
                return this;
            }

            public Builder paymentsWithNotes(long paymentsWithNotes) {
                this.paymentsWithNotes = paymentsWithNotes;
                return this;
            }

            public PaymentStatistics build() {
                return new PaymentStatistics(totalPayments, processedPayments, pendingPayments, paymentsWithNotes);
            }
        }
    }
} 