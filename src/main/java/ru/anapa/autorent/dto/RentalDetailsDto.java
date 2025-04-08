package ru.anapa.autorent.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RentalDetailsDto {

    private Long id;
    private UserSummaryDto user;
    private CarSummaryDto car;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime actualReturnDate;
    private BigDecimal totalCost;
    private String status;
    private String notes;
}