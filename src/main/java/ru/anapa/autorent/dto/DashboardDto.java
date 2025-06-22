package ru.anapa.autorent.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardDto {
    private Long totalUsers;
    private Long totalCars;
    private Long availableCars;
    private Long activeRentals;
    private Long completedRentals;
    private BigDecimal totalRevenue;
    private List<RentalDetailsDto> recentRentals;
    private List<CarSummaryDto> popularCars;
}