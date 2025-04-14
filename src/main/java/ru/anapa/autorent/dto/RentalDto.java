package ru.anapa.autorent.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class RentalDto {

    private Long id;

    @NotNull(message = "Дата начала аренды обязательна")
    @FutureOrPresent(message = "Дата начала аренды должна быть не раньше текущей даты")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startDate;

    @NotNull(message = "Дата окончания аренды обязательна")
    @Future(message = "Дата окончания аренды должна быть в будущем")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endDate;

    private String notes;

    private String cancelReason;
}