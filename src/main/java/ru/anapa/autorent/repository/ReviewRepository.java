package ru.anapa.autorent.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.anapa.autorent.model.Car;
import ru.anapa.autorent.model.Review;
import ru.anapa.autorent.model.User;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByCar(Car car);
    Page<Review> findByCar(Car car, Pageable pageable);

    List<Review> findByUser(User user);

    List<Review> findByRating(Integer rating);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.car = :car")
    Double getAverageRatingForCar(Car car);

    @Query("SELECT r.car, AVG(r.rating) as avgRating FROM Review r GROUP BY r.car ORDER BY avgRating DESC")
    List<Object[]> findTopRatedCars(Pageable pageable);
}