package ru.anapa.autorent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Конфигурация приложения для управления порядком инициализации бинов
 */
@Configuration
public class AppConfig {

    /**
     * Создаем PasswordEncoder независимо от SecurityConfig для избежания циклических зависимостей
     * @return экземпляр BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 