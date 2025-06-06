package ru.anapa.autorent.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class RentalDto {

    private Long id;

    @NotNull(message = "Дата начала аренды обязательна")
    @FutureOrPresent(message = "Дата начала аренды должна быть не раньше текущей даты")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)  // Это соответствует формату input type="datetime-local"
    private LocalDateTime startDate;

    @NotNull(message = "Дата окончания аренды обязательна")
    @Future(message = "Дата окончания аренды должна быть в будущем")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)  // Это соответствует формату input type="datetime-local"
    private LocalDateTime endDate;

    @NotNull(message = "Длительность аренды обязательна")
    @Min(value = 1, message = "Длительность аренды должна быть не менее 1 дня")
    private Integer durationDays;

    private String notes;

    private String cancelReason;
}