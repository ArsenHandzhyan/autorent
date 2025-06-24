package ru.anapa.autorent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.anapa.autorent.model.PasswordResetLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetLogRepository extends JpaRepository<PasswordResetLog, Long> {
    
    /**
     * Найти логи по email
     */
    List<PasswordResetLog> findByEmailOrderByCreatedAtDesc(String email);
    
    /**
     * Найти логи по токену
     */
    Optional<PasswordResetLog> findByToken(String token);
    
    /**
     * Найти логи по IP адресу за последние 24 часа
     */
    @Query("SELECT p FROM PasswordResetLog p WHERE p.ipAddress = :ipAddress " +
           "AND p.createdAt >= :since ORDER BY p.createdAt DESC")
    List<PasswordResetLog> findByIpAddressSince(@Param("ipAddress") String ipAddress, 
                                               @Param("since") LocalDateTime since);
    
    /**
     * Найти логи по email за последние 24 часа
     */
    @Query("SELECT p FROM PasswordResetLog p WHERE p.email = :email " +
           "AND p.createdAt >= :since ORDER BY p.createdAt DESC")
    List<PasswordResetLog> findByEmailSince(@Param("email") String email, 
                                           @Param("since") LocalDateTime since);
    
    /**
     * Найти неудачные попытки по email за последние 24 часа
     */
    @Query("SELECT COUNT(p) FROM PasswordResetLog p WHERE p.email = :email " +
           "AND p.status = 'FAILED' AND p.createdAt >= :since")
    long countFailedAttemptsByEmailSince(@Param("email") String email, 
                                        @Param("since") LocalDateTime since);
    
    /**
     * Найти неудачные попытки по IP за последние 24 часа
     */
    @Query("SELECT COUNT(p) FROM PasswordResetLog p WHERE p.ipAddress = :ipAddress " +
           "AND p.status = 'FAILED' AND p.createdAt >= :since")
    long countFailedAttemptsByIpSince(@Param("ipAddress") String ipAddress, 
                                     @Param("since") LocalDateTime since);
    
    /**
     * Удалить старые логи (старше 30 дней)
     */
    @Query("DELETE FROM PasswordResetLog p WHERE p.createdAt < :cutoffDate")
    void deleteOldLogs(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Подсчитать логи созданные после указанной даты
     */
    long countByCreatedAtAfter(LocalDateTime since);
    
    /**
     * Подсчитать логи по статусу и дате создания
     */
    long countByStatusAndCreatedAtAfter(PasswordResetLog.Status status, LocalDateTime since);
    
    /**
     * Подсчитать логи по типу действия и дате создания
     */
    long countByActionTypeAndCreatedAtAfter(PasswordResetLog.ActionType actionType, LocalDateTime since);
    
    /**
     * Найти последние 50 логов
     */
    @Query("SELECT p FROM PasswordResetLog p ORDER BY p.createdAt DESC LIMIT 50")
    List<PasswordResetLog> findTop50ByOrderByCreatedAtDesc();
    
    /**
     * Найти последние 100 логов
     */
    @Query("SELECT p FROM PasswordResetLog p ORDER BY p.createdAt DESC LIMIT 100")
    List<PasswordResetLog> findTop100ByOrderByCreatedAtDesc();
    
    /**
     * Найти логи по типу действия
     */
    List<PasswordResetLog> findByActionTypeOrderByCreatedAtDesc(PasswordResetLog.ActionType actionType);
    
    /**
     * Найти логи по статусу
     */
    List<PasswordResetLog> findByStatusOrderByCreatedAtDesc(PasswordResetLog.Status status);
} 