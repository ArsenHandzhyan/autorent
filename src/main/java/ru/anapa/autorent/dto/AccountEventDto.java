package ru.anapa.autorent.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AccountEventDto {
    private LocalDateTime eventDate;
    private String eventType; // "Изменение" или "Транзакция"
    private String description;
    private String amount; // Для транзакций
    private String balanceAfter; // Для транзакций
    private String fieldName; // Для изменений
    private String oldValue; // Для изменений
    private String newValue; // Для изменений
    private String changedBy; // Для изменений
} 