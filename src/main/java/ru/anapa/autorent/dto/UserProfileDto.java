package ru.anapa.autorent.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserProfileDto {

    private Long id;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Пожалуйста, введите корректный email")
    private String email;

    @NotBlank(message = "Имя обязательно")
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    private String lastName;

    @NotBlank(message = "Телефон обязателен")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Пожалуйста, введите корректный номер телефона")
    private String phone;

    // No password field - separate DTO for password changes
}