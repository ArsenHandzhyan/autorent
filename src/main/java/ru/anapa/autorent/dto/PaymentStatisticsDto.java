package ru.anapa.autorent.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentStatisticsDto {
    private final long totalPayments;
    private final long processedPayments;
    private final long failedPayments;
    private final long pendingPayments;
    private final BigDecimal totalAmount;
    private final BigDecimal processedAmount;

    public PaymentStatisticsDto(long totalPayments, long processedPayments, long failedPayments,
                               long pendingPayments, BigDecimal totalAmount, BigDecimal processedAmount) {
        this.totalPayments = totalPayments;
        this.processedPayments = processedPayments;
        this.failedPayments = failedPayments;
        this.pendingPayments = pendingPayments;
        this.totalAmount = totalAmount;
        this.processedAmount = processedAmount;
    }
} 