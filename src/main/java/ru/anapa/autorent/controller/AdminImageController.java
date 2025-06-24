package ru.anapa.autorent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.anapa.autorent.service.ImageMigrationService;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/images")
@PreAuthorize("hasRole('ADMIN')")
public class AdminImageController {

    @Autowired
    private ImageMigrationService imageMigrationService;

    /**
     * Проверяет статус миграции изображений
     */
    @GetMapping("/migration-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMigrationStatus() {
        try {
            imageMigrationService.checkMigrationStatus();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Статус миграции проверен. Смотрите логи для деталей.");
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Ошибка при проверке статуса: " + e.getMessage());
            response.put("status", "error");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Запускает миграцию всех изображений из файловой системы в БД
     */
    @PostMapping("/migrate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> migrateImages() {
        try {
            imageMigrationService.migrateAllImages();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Миграция изображений запущена. Смотрите логи для деталей.");
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Ошибка при миграции: " + e.getMessage());
            response.put("status", "error");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 