package ru.anapa.autorent.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_tokens")
@Getter
@Setter
@NoArgsConstructor
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "token_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "is_used", nullable = false)
    private boolean used = false;

    public enum TokenType {
        EMAIL_VERIFICATION,
        PASSWORD_RESET,
        SMS_VERIFICATION
    }

    // Создает новый токен для верификации email
    public static VerificationToken createEmailToken(String email, String verificationCode) {
        VerificationToken token = new VerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setEmail(email);
        token.setVerificationCode(verificationCode);
        token.setTokenType(TokenType.EMAIL_VERIFICATION);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusHours(24)); // Токен действителен 24 часа
        return token;
    }

    // Создает новый токен для сброса пароля
    public static VerificationToken createPasswordResetToken(String email) {
        VerificationToken token = new VerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setEmail(email);
        token.setTokenType(TokenType.PASSWORD_RESET);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusHours(1)); // Токен для сброса пароля действителен 1 час
        return token;
    }

    // Создает новый токен для SMS верификации
    public static VerificationToken createSmsToken(String phone, String verificationCode) {
        VerificationToken token = new VerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setEmail(phone); // Используем поле email для хранения телефона
        token.setVerificationCode(verificationCode);
        token.setTokenType(TokenType.SMS_VERIFICATION);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(15)); // SMS коды действительны 15 минут
        return token;
    }

    // Проверяет, истек ли срок действия токена
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // Помечает токен как использованный
    public void markAsUsed() {
        this.used = true;
        this.verifiedAt = LocalDateTime.now();
    }
} 