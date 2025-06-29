package ru.anapa.autorent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Сервис для мониторинга состояния подключений к MySQL серверам
 */
@Service
@Slf4j
public class DatabaseHealthService {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseHealthService(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Проверка состояния подключения к базе данных
     */
    public Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("timestamp", LocalDateTime.now());
        healthStatus.put("status", "UNKNOWN");

        try {
            // Проверка подключения
            try (Connection connection = dataSource.getConnection()) {
                healthStatus.put("status", "UP");
                healthStatus.put("connection", "OK");
                
                // Проверка выполнения простого запроса
                String result = jdbcTemplate.queryForObject("SELECT 'OK' as status", String.class);
                healthStatus.put("query", "OK");
                healthStatus.put("result", result);
                
                // Информация о подключении
                healthStatus.put("database", connection.getMetaData().getDatabaseProductName());
                healthStatus.put("version", connection.getMetaData().getDatabaseProductVersion());
                healthStatus.put("url", connection.getMetaData().getURL());
                
                log.info("Database health check passed: {}", healthStatus);
            }
        } catch (SQLException e) {
            healthStatus.put("status", "DOWN");
            healthStatus.put("error", e.getMessage());
            healthStatus.put("errorCode", e.getErrorCode());
            healthStatus.put("sqlState", e.getSQLState());
            
            log.error("Database health check failed: {}", e.getMessage(), e);
        }

        return healthStatus;
    }

    /**
     * Проверка производительности базы данных
     */
    public Map<String, Object> checkDatabasePerformance() {
        Map<String, Object> performance = new HashMap<>();
        performance.put("timestamp", LocalDateTime.now());

        try {
            // Измерение времени выполнения простого запроса
            long startTime = System.currentTimeMillis();
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            long endTime = System.currentTimeMillis();
            
            performance.put("queryTime", endTime - startTime);
            performance.put("status", "OK");
            
            // Проверка количества активных соединений (если доступно)
            if (dataSource instanceof com.zaxxer.hikari.HikariDataSource) {
                com.zaxxer.hikari.HikariDataSource hikariDataSource = (com.zaxxer.hikari.HikariDataSource) dataSource;
                performance.put("activeConnections", hikariDataSource.getHikariPoolMXBean().getActiveConnections());
                performance.put("idleConnections", hikariDataSource.getHikariPoolMXBean().getIdleConnections());
                performance.put("totalConnections", hikariDataSource.getHikariPoolMXBean().getTotalConnections());
            }
            
            log.info("Database performance check completed: {}", performance);
        } catch (Exception e) {
            performance.put("status", "ERROR");
            performance.put("error", e.getMessage());
            log.error("Database performance check failed: {}", e.getMessage(), e);
        }

        return performance;
    }

    /**
     * Проверка доступности таблиц
     */
    public Map<String, Object> checkTablesAvailability() {
        Map<String, Object> tablesStatus = new HashMap<>();
        tablesStatus.put("timestamp", LocalDateTime.now());

        try {
            // Список основных таблиц для проверки
            String[] tables = {"users", "cars", "rentals", "payments", "reviews"};
            
            for (String table : tables) {
                try {
                    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
                    tablesStatus.put(table, "AVAILABLE");
                } catch (Exception e) {
                    tablesStatus.put(table, "UNAVAILABLE");
                    tablesStatus.put(table + "_error", e.getMessage());
                }
            }
            
            tablesStatus.put("status", "OK");
            log.info("Tables availability check completed: {}", tablesStatus);
        } catch (Exception e) {
            tablesStatus.put("status", "ERROR");
            tablesStatus.put("error", e.getMessage());
            log.error("Tables availability check failed: {}", e.getMessage(), e);
        }

        return tablesStatus;
    }

    /**
     * Полная диагностика базы данных
     */
    public Map<String, Object> fullDatabaseDiagnostic() {
        Map<String, Object> diagnostic = new HashMap<>();
        diagnostic.put("timestamp", LocalDateTime.now());

        // Проверка здоровья
        diagnostic.put("health", checkDatabaseHealth());
        
        // Проверка производительности
        diagnostic.put("performance", checkDatabasePerformance());
        
        // Проверка доступности таблиц
        diagnostic.put("tables", checkTablesAvailability());

        return diagnostic;
    }
} 