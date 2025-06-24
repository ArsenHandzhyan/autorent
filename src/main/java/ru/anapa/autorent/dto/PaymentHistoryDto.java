package ru.anapa.autorent.dto;

import lombok.Data;
import ru.anapa.autorent.model.DailyPayment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PaymentHistoryDto {
    private Long id;
    private LocalDate paymentDate;
    private BigDecimal amount;
    private String status;
    private String statusDisplayName;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String notes;
    private String accountNumber;
    private String userName;

    public static PaymentHistoryDto fromDailyPayment(DailyPayment payment) {
        PaymentHistoryDto dto = new PaymentHistoryDto();
        dto.setId(payment.getId());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setAmount(payment.getAmount());
        dto.setStatus(payment.getStatus().name());
        dto.setStatusDisplayName(payment.getStatus().getDisplayName());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setProcessedAt(payment.getProcessedAt());
        dto.setNotes(payment.getNotes());
        
        if (payment.getAccount() != null) {
            dto.setAccountNumber(payment.getAccount().getAccountNumber());
            if (payment.getAccount().getUser() != null) {
                dto.setUserName(payment.getAccount().getUser().getFirstName() + " " + 
                              payment.getAccount().getUser().getLastName());
            }
        }
        
        return dto;
    }
} 