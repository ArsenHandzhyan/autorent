package ru.anapa.autorent.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarSummaryDto {
    private Long id;
    private String brand;
    private String model;
    private Integer year;
    private String licensePlate;
    private BigDecimal dailyRate;
    private String imageUrl;
}