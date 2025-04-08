package ru.anapa.autorent.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewDto {

    private Long id;

    @NotNull(message = "Оценка обязательна")
    @Min(value = 1, message = "Минимальная оценка - 1")
    @Max(value = 5, message = "Максимальная оценка - 5")
    private Integer rating;

    @NotBlank(message = "Комментарий обязателен")
    private String comment;

    private Long carId;

    private String userFullName;

    private LocalDateTime createdAt;
}