package ru.anapa.autorent.model;

import lombok.Data;

@Data
public class CarStats {
    private String brand;
    private String model;
    private int rentalCount;
    private double revenue;
    private double averageRating;
} 