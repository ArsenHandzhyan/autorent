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

    // Search by brand or model
    List<Car> findByBrandContainingIgnoreCase(String brand);
}