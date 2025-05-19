package ru.anapa.autorent.dto;

import lombok.Data;

@Data
public class CarImageDto {
    private Long id;
    private String imageUrl;
    private String description;
    private Integer displayOrder;
    private boolean main;
    private Integer rotation = 0;
} 