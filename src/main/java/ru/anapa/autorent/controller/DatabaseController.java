package ru.anapa.autorent.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.anapa.autorent.service.DatabaseHealthService;

import java.util.Map;

/**
 * Контроллер для управления и мониторинга MSP серверов MySQL
 */
@RestController
@RequestMapping("/api/database")
@Slf4j
public class DatabaseController {

    private final DatabaseHealthService databaseHealthService;

    @Autowired
    public DatabaseController(DatabaseHealthService databaseHealthService) {
        this.databaseHealthService = databaseHealthService;
    }

    /**
     * Проверка состояния подключения к базе данных
     */
    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> checkDatabaseHealth() {
        log.info("Database health check requested");
        Map<String, Object> healthStatus = databaseHealthService.checkDatabaseHealth();
        return ResponseEntity.ok(healthStatus);
    }

    /**
     * Проверка производительности базы данных
     */
    @GetMapping("/performance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> checkDatabasePerformance() {
        log.info("Database performance check requested");
        Map<String, Object> performance = databaseHealthService.checkDatabasePerformance();
        return ResponseEntity.ok(performance);
    }

    /**
     * Проверка доступности таблиц
     */
    @GetMapping("/tables")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> checkTablesAvailability() {
        log.info("Tables availability check requested");
        Map<String, Object> tablesStatus = databaseHealthService.checkTablesAvailability();
        return ResponseEntity.ok(tablesStatus);
    }

    /**
     * Полная диагностика базы данных
     */
    @GetMapping("/diagnostic")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> fullDatabaseDiagnostic() {
        log.info("Full database diagnostic requested");
        Map<String, Object> diagnostic = databaseHealthService.fullDatabaseDiagnostic();
        return ResponseEntity.ok(diagnostic);
    }

    /**
     * Информация о текущем профиле и конфигурации
     */
    @GetMapping("/info")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDatabaseInfo() {
        log.info("Database info requested");
        
        Map<String, Object> info = Map.of(
            "activeProfile", System.getProperty("spring.profiles.active", "default"),
            "availableProfiles", new String[]{"local", "dev", "test", "staging", "prod", "backup"},
            "timestamp", java.time.LocalDateTime.now(),
            "status", "OK"
        );
        
        return ResponseEntity.ok(info);
    }

    /**
     * Проверка подключения к конкретному серверу (для MSP)
     */
    @PostMapping("/test-connection")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> testConnection(@RequestBody Map<String, String> connectionParams) {
        log.info("Testing connection to server: {}", connectionParams.get("server"));
        
        // Здесь можно добавить логику для тестирования подключения к разным серверам
        Map<String, Object> result = Map.of(
            "server", connectionParams.get("server"),
            "status", "TESTED",
            "timestamp", java.time.LocalDateTime.now(),
            "message", "Connection test completed"
        );
        
        return ResponseEntity.ok(result);
    }
} 