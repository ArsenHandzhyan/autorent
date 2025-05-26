package ru.anapa.autorent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.anapa.autorent.model.Car;
import ru.anapa.autorent.model.CarStatusHistory;

import java.util.List;

@Repository
public interface CarStatusHistoryRepository extends JpaRepository<CarStatusHistory, Long> {
    List<CarStatusHistory> findByCarOrderByChangeDateDesc(Car car);
    List<CarStatusHistory> findByCarIdOrderByChangeDateDesc(Long carId);
} 