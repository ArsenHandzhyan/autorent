package ru.anapa.autorent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.anapa.autorent.model.DailyPayment;
import ru.anapa.autorent.model.Rental;
import ru.anapa.autorent.model.RentalStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyPaymentRepository extends JpaRepository<DailyPayment, Long> {

    List<DailyPayment> findByRentalOrderByPaymentDateDesc(Rental rental);

    @Query("SELECT dp FROM DailyPayment dp WHERE dp.rental.id = :rentalId ORDER BY dp.paymentDate DESC")
    List<DailyPayment> findByRentalId(@Param("rentalId") Long rentalId);

    List<DailyPayment> findByRentalAndPaymentDateBetween(Rental rental, LocalDate startDate, LocalDate endDate);

    @Query("SELECT dp FROM DailyPayment dp WHERE dp.rental = :rental AND dp.paymentDate = :paymentDate")
    Optional<DailyPayment> findByRentalAndPaymentDate(@Param("rental") Rental rental, @Param("paymentDate") LocalDate paymentDate);

    @Query("SELECT dp FROM DailyPayment dp WHERE dp.status = 'PENDING' AND dp.paymentDate <= :paymentDate")
    List<DailyPayment> findPendingPaymentsByDate(@Param("paymentDate") LocalDate paymentDate);

    @Query("SELECT dp FROM DailyPayment dp WHERE dp.rental.status = :status AND dp.paymentDate = :paymentDate")
    List<DailyPayment> findByRentalStatusAndPaymentDate(@Param("status") RentalStatus status, @Param("paymentDate") LocalDate paymentDate);

    @Query("SELECT COUNT(dp) FROM DailyPayment dp WHERE dp.rental = :rental AND dp.status = 'PROCESSED'")
    long countProcessedPaymentsByRental(@Param("rental") Rental rental);

    @Query("SELECT dp FROM DailyPayment dp WHERE dp.rental.status = 'ACTIVE' AND dp.paymentDate = :paymentDate AND dp.status = 'PENDING'")
    List<DailyPayment> findActiveRentalsPendingPayments(@Param("paymentDate") LocalDate paymentDate);

    @Query("SELECT dp FROM DailyPayment dp WHERE dp.account.user.id = :userId ORDER BY dp.paymentDate DESC")
    List<DailyPayment> findByAccountUserIdOrderByPaymentDateDesc(@Param("userId") Long userId);

    @Query("SELECT DISTINCT dp FROM DailyPayment dp " +
           "LEFT JOIN FETCH dp.rental r " +
           "LEFT JOIN FETCH r.car c " +
           "WHERE dp.account.user.id = :userId " +
           "ORDER BY dp.paymentDate DESC")
    List<DailyPayment> findPaymentsByUserWithCarData(@Param("userId") Long userId);

    List<DailyPayment> findAllByStatus(DailyPayment.PaymentStatus status);

    // Новые методы для обработки пропущенных платежей
    @Query("SELECT dp FROM DailyPayment dp WHERE dp.status IN ('PENDING', 'FAILED') AND dp.paymentDate <= :maxDate ORDER BY dp.paymentDate ASC")
    List<DailyPayment> findUnprocessedPaymentsUpToDate(@Param("maxDate") LocalDate maxDate);

    @Query("SELECT dp FROM DailyPayment dp WHERE dp.status = 'PENDING' AND dp.paymentDate < :date ORDER BY dp.paymentDate ASC")
    List<DailyPayment> findOverduePendingPayments(@Param("date") LocalDate date);

    @Query("SELECT dp FROM DailyPayment dp WHERE dp.status = 'FAILED' AND dp.paymentDate < :date ORDER BY dp.paymentDate ASC")
    List<DailyPayment> findOverdueFailedPayments(@Param("date") LocalDate date);

    @Query("SELECT COUNT(dp) FROM DailyPayment dp WHERE dp.status = 'PENDING' AND dp.paymentDate < :date")
    long countOverduePendingPayments(@Param("date") LocalDate date);

    @Query("SELECT COUNT(dp) FROM DailyPayment dp WHERE dp.status = 'FAILED' AND dp.paymentDate < :date")
    long countOverdueFailedPayments(@Param("date") LocalDate date);

    @Query("SELECT DISTINCT dp.paymentDate FROM DailyPayment dp WHERE dp.status IN ('PENDING', 'FAILED') AND dp.paymentDate < :date ORDER BY dp.paymentDate ASC")
    List<LocalDate> findUnprocessedPaymentDates(@Param("date") LocalDate date);

    @Query("SELECT dp FROM DailyPayment dp WHERE dp.rental.status = 'ACTIVE' AND dp.paymentDate BETWEEN :startDate AND :endDate AND dp.status IN ('PENDING', 'FAILED') ORDER BY dp.paymentDate ASC")
    List<DailyPayment> findActiveRentalsUnprocessedPaymentsInPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Методы для статистики
    long countByStatus(DailyPayment.PaymentStatus status);
    
    List<DailyPayment> findTop10ByOrderByPaymentDateDesc();
} 