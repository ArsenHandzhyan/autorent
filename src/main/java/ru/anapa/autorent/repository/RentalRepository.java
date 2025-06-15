package ru.anapa.autorent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.anapa.autorent.model.Car;
import ru.anapa.autorent.model.Rental;
import ru.anapa.autorent.model.RentalStatus;
import ru.anapa.autorent.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

    List<Rental> findByUserOrderByCreatedAtDesc(User user);

    List<Rental> findByStatusOrderByCreatedAtDesc(RentalStatus status);

    @Query("SELECT r FROM Rental r WHERE r.car = :car AND r.status IN ('ACTIVE', 'PENDING') " +
            "AND r.id != :rentalId " +
            "AND ((r.startDate BETWEEN :startDate AND :endDate) OR " +
            "(r.endDate BETWEEN :startDate AND :endDate) OR " +
            "(:startDate BETWEEN r.startDate AND r.endDate) OR " +
            "(:endDate BETWEEN r.startDate AND r.endDate))")
    List<Rental> findOverlappingActiveRentals(@Param("car") Car car,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate,
                                              @Param("rentalId") Long rentalId);

    List<Rental> findByStatusAndEndDateBefore(RentalStatus status, LocalDateTime date);

    List<Rental> findByStatusAndStartDateBetween(RentalStatus status, LocalDateTime startDate, LocalDateTime endDate);

    List<Rental> findByCarAndStatusOrderByEndDateAsc(Car car, RentalStatus status);

    List<Rental> findByCarAndStatusInOrderByStartDateAsc(Car car, List<RentalStatus> statuses);

    List<Rental> findByCarAndStatusAndIdNot(Car car, RentalStatus status, Long id);

    List<Rental> findByCarAndStatusIn(Car car, List<RentalStatus> statuses);

    @Query("SELECT r FROM Rental r WHERE r.car.id IN :carIds AND r.status IN :statuses ORDER BY r.endDate ASC")
    List<Rental> findRentalsByCarIdInAndStatusIn(@Param("carIds") List<Long> carIds,
                                                 @Param("statuses") List<RentalStatus> statuses);

    List<Rental> findByStatus(RentalStatus status);

    List<Rental> findByUser(User user);

    List<Rental> findByCar(Car car);

    @Query("SELECT r FROM Rental r WHERE r.car.id = :carId " +
           "AND r.status IN :statuses " +
           "AND ((r.startDate <= :endDate AND r.endDate >= :startDate) " +
           "OR (r.startDate >= :startDate AND r.startDate <= :endDate) " +
           "OR (r.endDate >= :startDate AND r.endDate <= :endDate))")
    List<Rental> findOverlappingRentals(@Param("carId") Long carId,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       @Param("statuses") List<RentalStatus> statuses);
}
