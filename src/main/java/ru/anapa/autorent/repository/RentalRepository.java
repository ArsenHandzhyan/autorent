package ru.anapa.autorent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.anapa.autorent.model.Rental;
import ru.anapa.autorent.model.User;

import java.util.List;
import java.util.Optional;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    @Query("SELECT r FROM Rental r JOIN FETCH r.car WHERE r.user = :user")
    List<Rental> findByUser(@Param("user") User user);

    // Добавляем метод для поиска аренд по статусу
    @Query("SELECT r FROM Rental r JOIN FETCH r.car JOIN FETCH r.user WHERE r.status = :status")
    List<Rental> findByStatus(@Param("status") String status);

    @Query("SELECT r FROM Rental r JOIN FETCH r.car JOIN FETCH r.user")
    List<Rental> findAllWithCarAndUser();
}