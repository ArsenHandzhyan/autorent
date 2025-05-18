package ru.anapa.autorent.dto;

import lombok.Data;
import ru.anapa.autorent.model.CarStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CarSummaryDto {
    private Long id;
    private String brand;
    private String model;
    private Integer year;
    private String imageUrl;
    private BigDecimal dailyRate;
    private boolean available;
    private CarStatus status;
    private LocalDateTime nextAvailableDate;
    private String transmission;
    private String fuelType;
    private Integer seats;
    private String category;
}