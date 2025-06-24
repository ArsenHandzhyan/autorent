package ru.anapa.autorent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordDto {
    
    @NotBlank(message = "Токен обязателен")
    private String token;
    
    @NotBlank(message = "Новый пароль обязателен")
    @Size(min = 8, message = "Пароль должен содержать минимум 8 символов")
    private String newPassword;
    
    @NotBlank(message = "Подтверждение пароля обязательно")
    private String confirmPassword;
} 