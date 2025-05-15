package ru.anapa.autorent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.anapa.autorent.model.VerificationToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    
    Optional<VerificationToken> findByToken(String token);
    
    Optional<VerificationToken> findByEmailAndTokenType(String email, VerificationToken.TokenType tokenType);
    
    @Query("SELECT t FROM VerificationToken t WHERE t.email = :email AND t.tokenType = :tokenType " +
           "AND t.used = false AND t.expiresAt > :now ORDER BY t.createdAt DESC")
    Optional<VerificationToken> findLatestActiveToken(@Param("email") String email, 
                                                     @Param("tokenType") VerificationToken.TokenType tokenType,
                                                     @Param("now") LocalDateTime now);
    
    @Query("SELECT t FROM VerificationToken t WHERE t.email = :email AND t.tokenType = :tokenType " +
           "AND t.verificationCode = :code AND t.used = false AND t.expiresAt > :now")
    Optional<VerificationToken> findValidTokenByCode(@Param("email") String email,
                                                 @Param("tokenType") VerificationToken.TokenType tokenType,
                                                 @Param("code") String code,
                                                 @Param("now") LocalDateTime now);
    
    List<VerificationToken> findByExpiresAtBeforeAndUsedFalse(LocalDateTime dateTime);
    
    void deleteByEmailAndTokenType(String email, VerificationToken.TokenType tokenType);
    
    @Query("SELECT COUNT(t) > 0 FROM VerificationToken t WHERE t.email = :email AND t.tokenType = :tokenType " +
           "AND t.createdAt > :timeThreshold")
    boolean existsRecentToken(@Param("email") String email,
                             @Param("tokenType") VerificationToken.TokenType tokenType,
                             @Param("timeThreshold") LocalDateTime timeThreshold);
} 