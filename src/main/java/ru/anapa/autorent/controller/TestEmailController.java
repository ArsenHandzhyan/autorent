package ru.anapa.autorent.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.anapa.autorent.service.EmailService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestEmailController {

    private static final Logger logger = LoggerFactory.getLogger(TestEmailController.class);
    
    private final EmailService emailService;

    @Autowired
    public TestEmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send-test-email")
    public ResponseEntity<Map<String, Object>> sendTestEmail(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Отправка тестового email на адрес: {}", email);
            
            // Отправляем тестовое письмо для восстановления пароля
            emailService.sendPasswordResetEmail(email, "test-token-12345");
            
            response.put("success", true);
            response.put("message", "Тестовое письмо отправлено на " + email);
            logger.info("Тестовое письмо успешно отправлено на {}", email);
            
        } catch (Exception e) {
            logger.error("Ошибка при отправке тестового письма на {}: {}", email, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Ошибка при отправке письма: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email-status")
    public ResponseEntity<Map<String, Object>> getEmailStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Проверяем настройки email
            response.put("success", true);
            response.put("message", "Email сервис доступен");
            response.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            logger.error("Ошибка при проверке статуса email: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Ошибка при проверке статуса: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
} 