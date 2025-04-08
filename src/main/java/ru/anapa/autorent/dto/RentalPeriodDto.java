package ru.anapa.autorent.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RentalPeriodDto {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}