package ru.anapa.autorent.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.anapa.autorent.model.Car;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    // Базовые методы поиска
    List<Car> findByAvailableTrue();
    Page<Car> findByAvailableTrue(Pageable pageable);

    // Оптимизированный метод поиска по бренду с пагинацией
    @Query("SELECT c FROM Car c WHERE LOWER(c.brand) LIKE LOWER(CONCAT('%', :brand, '%'))")
    Page<Car> searchByBrand(@Param("brand") String brand, Pageable pageable);

    // Оптимизированный метод для поиска по категории
    Page<Car> findByCategoryIgnoreCase(String category, Pageable pageable);

    // Оптимизированный метод для получения автомобилей с информацией об арендах
    @Query("SELECT DISTINCT c FROM Car c LEFT JOIN FETCH c.rentals r " +
            "WHERE (r.status IN ('ACTIVE', 'PENDING') OR r IS NULL) " +
            "AND c.id IN :ids")
    List<Car> findByIdInWithRentals(@Param("ids") List<Long> ids);
}