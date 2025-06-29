package ru.anapa.autorent.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Конфигурация для управления подключениями к различным MySQL серверам
 * Поддерживает профили: local, dev, test, staging, prod, backup
 */
@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String dataSourceUrl;

    @Value("${spring.datasource.username}")
    private String dataSourceUsername;

    @Value("${spring.datasource.password}")
    private String dataSourcePassword;

    @Value("${spring.datasource.driver-class-name}")
    private String dataSourceDriverClassName;

    @Value("${spring.datasource.hikari.maximum-pool-size:10}")
    private int maximumPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle:5}")
    private int minimumIdle;

    @Value("${spring.datasource.hikari.idle-timeout:300000}")
    private long idleTimeout;

    @Value("${spring.datasource.hikari.max-lifetime:1800000}")
    private long maxLifetime;

    @Value("${spring.datasource.hikari.connection-timeout:20000}")
    private long connectionTimeout;

    @Value("${spring.datasource.hikari.connection-init-sql}")
    private String connectionInitSql;

    /**
     * Основной DataSource для приложения
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dataSourceUrl);
        config.setUsername(dataSourceUsername);
        config.setPassword(dataSourcePassword);
        config.setDriverClassName(dataSourceDriverClassName);
        
        // Настройки пула соединений
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setConnectionTimeout(connectionTimeout);
        config.setConnectionInitSql(connectionInitSql);
        
        // Дополнительные настройки для стабильности
        config.setLeakDetectionThreshold(60000);
        config.setValidationTimeout(5000);
        config.setConnectionTestQuery("SELECT 1");
        
        return new HikariDataSource(config);
    }

    /**
     * JdbcTemplate для прямых SQL запросов
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * Конфигурация для локальной разработки
     */
    @Configuration
    @Profile("local")
    static class LocalDatabaseConfig {
        // Специфичные настройки для локальной разработки
    }

    /**
     * Конфигурация для тестового окружения
     */
    @Configuration
    @Profile("test")
    static class TestDatabaseConfig {
        // Специфичные настройки для тестового окружения
    }

    /**
     * Конфигурация для staging окружения
     */
    @Configuration
    @Profile("staging")
    static class StagingDatabaseConfig {
        // Специфичные настройки для staging окружения
    }

    /**
     * Конфигурация для продакшн окружения
     */
    @Configuration
    @Profile("prod")
    static class ProductionDatabaseConfig {
        // Специфичные настройки для продакшн окружения
    }

    /**
     * Конфигурация для резервного сервера
     */
    @Configuration
    @Profile("backup")
    static class BackupDatabaseConfig {
        // Специфичные настройки для резервного сервера
    }
} 