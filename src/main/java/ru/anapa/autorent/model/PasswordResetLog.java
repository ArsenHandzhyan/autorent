package ru.anapa.autorent.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_logs")
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "action_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public enum ActionType {
        REQUEST_SENT,      // Запрос на восстановление отправлен
        TOKEN_VALIDATED,   // Токен проверен
        PASSWORD_RESET,    // Пароль сброшен
        TOKEN_EXPIRED,     // Токен истек
        INVALID_TOKEN,     // Неверный токен
        USER_NOT_FOUND     // Пользователь не найден
    }

    public enum Status {
        SUCCESS,
        FAILED,
        PENDING
    }

    public PasswordResetLog(String email, String token, String ipAddress, String userAgent, ActionType actionType) {
        this.email = email;
        this.token = token;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.actionType = actionType;
        this.status = Status.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void markAsCompleted(Status status) {
        this.status = status;
        this.completedAt = LocalDateTime.now();
    }

    public void setError(String errorMessage) {
        this.errorMessage = errorMessage;
        this.status = Status.FAILED;
        this.completedAt = LocalDateTime.now();
    }
} 