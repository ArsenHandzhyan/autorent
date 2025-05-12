package ru.anapa.autorent.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
public class UserRegistrationDto {
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, message = "Минимальная длина пароля 8 символов")
    private String password;

    @NotBlank(message = "Подтверждение пароля обязательно")
    private String confirmPassword;

    @NotBlank(message = "Имя обязательно")
    @Size(min = 2, message = "Минимальная длина имени 2 символа")
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    @Size(min = 2, message = "Минимальная длина фамилии 2 символа")
    private String lastName;

    @NotBlank(message = "Телефон обязателен")
    @Pattern(regexp = "^\\+7\\(\\d{3}\\)\\d{3}-\\d{2}-\\d{2}$",
            message = "Неверный формат телефона: +7(XXX)XXX-XX-XX")
    private String phone;
}
