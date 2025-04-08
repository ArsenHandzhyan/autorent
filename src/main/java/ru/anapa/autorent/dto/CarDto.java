package ru.anapa.autorent.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarDto {

    private Long id;

    @NotBlank(message = "Марка автомобиля обязательна")
    private String brand;

    @NotBlank(message = "Модель автомобиля обязательна")
    private String model;

    @NotNull(message = "Год выпуска обязателен")
    @Min(value = 1900, message = "Год выпуска должен быть не ранее 1900")
    @Max(value = 2100, message = "Некорректный год выпуска")
    private Integer year;

    @NotBlank(message = "Номер автомобиля обязателен")
    @Pattern(regexp = "^[А-Я0-9]{6,10}$", message = "Пожалуйста, введите корректный номер автомобиля")
    private String licensePlate;

    @NotNull(message = "Стоимость аренды в день обязательна")
    @DecimalMin(value = "0.01", message = "Стоимость аренды должна быть больше нуля")
    private BigDecimal dailyRate;

    private String imageUrl;

    private String description;

    private boolean available = true;

    // Дополнительные поля для расширенной информации об автомобиле
    private String transmission; // Тип трансмиссии (АКПП, МКПП и т.д.)

    private String fuelType; // Тип топлива (бензин, дизель, электро и т.д.)

    private Integer seats; // Количество мест

    private String color; // Цвет автомобиля

    private String category; // Категория автомобиля (эконом, бизнес, премиум и т.д.)
}