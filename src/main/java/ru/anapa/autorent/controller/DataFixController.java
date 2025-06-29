package ru.anapa.autorent.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.anapa.autorent.service.PaymentService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/data-fix")
@RequiredArgsConstructor
@Slf4j
public class DataFixController {

    private final PaymentService paymentService;

    /**
     * Исправление поврежденных примечаний платежей
     */
    @PostMapping("/fix-payment-notes")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> fixPaymentNotes() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int fixedCount = paymentService.fixCorruptedPaymentNotes();
            
            response.put("success", true);
            response.put("message", "Исправлено " + fixedCount + " поврежденных примечаний");
            response.put("fixedCount", fixedCount);
            response.put("timestamp", LocalDateTime.now());
            
            log.info("Исправлено {} поврежденных примечаний платежей", fixedCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Ошибка при исправлении примечаний платежей", e);
            
            response.put("success", false);
            response.put("error", "Ошибка при исправлении: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Получение статистики платежей
     */
    @GetMapping("/payment-statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPaymentStatistics() {
        log.info("Запрос статистики платежей");
        
        try {
            PaymentService.PaymentStatistics stats = paymentService.getPaymentStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", Map.of(
                "totalPayments", stats.getTotalPayments(),
                "processedPayments", stats.getProcessedPayments(),
                "pendingPayments", stats.getPendingPayments(),
                "paymentsWithNotes", stats.getPaymentsWithNotes()
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Ошибка при получении статистики платежей", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Ошибка при получении статистики: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Проверка состояния базы данных
     */
    @GetMapping("/health-check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("Проверка состояния базы данных");
        
        try {
            PaymentService.PaymentStatistics stats = paymentService.getPaymentStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("status", "OK");
            response.put("message", "База данных доступна");
            response.put("totalPayments", stats.getTotalPayments());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Ошибка при проверке состояния базы данных", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("status", "ERROR");
            response.put("message", "Ошибка подключения к базе данных: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 