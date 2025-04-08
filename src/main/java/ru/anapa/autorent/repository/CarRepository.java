package ru.anapa.autorent.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.anapa.autorent.model.Car;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    // Find cars by availability
    List<Car> findByAvailableTrue();
    List<Car> findByAvailableFalse();

    // Search by brand or model
    List<Car> findByBrandContainingIgnoreCase(String brand);
    List<Car> findByModelContainingIgnoreCase(String model);
    List<Car> findByBrandAndModel(String brand, String model);

    // Find by year
    List<Car> findByYear(Integer year);
    List<Car> findByYearBetween(Integer startYear, Integer endYear);

    // Find by price range
    List<Car> findByDailyRateBetween(BigDecimal min, BigDecimal max);
    List<Car> findByDailyRateLessThanEqual(BigDecimal maxRate);

    // Find available cars with pagination
    Page<Car> findByAvailableTrue(Pageable pageable);

    // Custom queries
    @Query("SELECT c FROM Car c WHERE c.available = true AND c.brand = :brand AND c.dailyRate <= :maxRate")
    List<Car> findAvailableCarsByBrandAndMaxRate(String brand, BigDecimal maxRate);

    @Query("SELECT c FROM Car c WHERE c.id NOT IN (SELECT r.car.id FROM Rental r WHERE " +
            "r.status IN ('PENDING', 'ACTIVE') AND " +
            "((r.startDate BETWEEN :startDate AND :endDate) OR " +
            "(r.endDate BETWEEN :startDate AND :endDate) OR " +
            "(:startDate BETWEEN r.startDate AND r.endDate)))")
    List<Car> findAvailableCarsForDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    @Query("SELECT c FROM Car c LEFT JOIN c.rentals r GROUP BY c ORDER BY COUNT(r) DESC")
    List<Car> findMostPopularCars(Pageable pageable);
}