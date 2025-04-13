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
    @Pattern(regexp = "^\\+7\\(\\d{3}\\)\\d{3}-\\d{2}-\\d{2}$", message = "Пожалуйста, введите корректный номер телефона в формате +7(XXX)XXX-XX-XX")
    private String phone;
}