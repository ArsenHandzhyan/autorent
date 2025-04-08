package ru.anapa.autorent.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CarDetailsDto {

    private Long id;
    private String brand;
    private String model;
    private Integer year;
    private String licensePlate;
    private BigDecimal dailyRate;
    private String imageUrl;
    private String description;
    private boolean available;
    private Double averageRating;
    private List<ReviewDto> reviews;
    private List<RentalPeriodDto> bookedPeriods;
}