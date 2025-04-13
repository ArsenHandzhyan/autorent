package ru.anapa.autorent.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewCreateRequest {

    @NotBlank(message = "Имя пользователя не может быть пустым")
    private String userName;

    @NotNull(message = "Оценка обязательна")
    @Min(value = 1, message = "Минимальная оценка - 1")
    @Max(value = 5, message = "Максимальная оценка - 5")
    private Integer rating;

    @NotBlank(message = "Текст отзыва не может быть пустым")
    private String reviewText;

    @NotNull(message = "ID проката обязателен")
    private Long rentalId;
}