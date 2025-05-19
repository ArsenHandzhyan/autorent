package ru.anapa.autorent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.anapa.autorent.model.CarImage;

import java.util.List;

@Repository
public interface CarImageRepository extends JpaRepository<CarImage, Long> {
    List<CarImage> findByCarIdOrderByDisplayOrderAsc(Long carId);
    void deleteByCarId(Long carId);
    List<CarImage> findByCarIdInOrderByDisplayOrderAsc(List<Long> carIds);
} 