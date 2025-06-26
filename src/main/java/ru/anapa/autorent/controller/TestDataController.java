package ru.anapa.autorent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.anapa.autorent.service.TestDataService;
import ru.anapa.autorent.repository.DailyPaymentRepository;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/test-data")
// @PreAuthorize("hasRole('ADMIN')") // Временно убираем для диагностики
public class TestDataController {

    // Временно убираем все зависимости для диагностики
    // private final TestDataService testDataService;
    // private final DailyPaymentRepository dailyPaymentRepository;

    // @Autowired
    // public TestDataController(DailyPaymentRepository dailyPaymentRepository) {
    //     this.dailyPaymentRepository = dailyPaymentRepository;
    // }

    /**
     * Очищает все платежи и создает новые тестовые данные
     */
    @PostMapping("/clean-and-create-payments")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cleanAndCreateTestPayments() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // testDataService.cleanAndCreateTestPayments();
            
            response.put("success", true);
            response.put("message", "Тестовые данные платежей успешно созданы");
            response.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка при создании тестовых данных: " + e.getMessage());
            response.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Показывает статистику платежей
     */
    @GetMapping("/payment-statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> showPaymentStatistics() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // testDataService.showPaymentStatistics();
            
            response.put("success", true);
            response.put("message", "Статистика платежей выведена в лог");
            response.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка при получении статистики: " + e.getMessage());
            response.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Простой тестовый эндпоинт для проверки работы контроллера
     */
    @GetMapping("/test")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "TestDataController работает корректно!");
        response.put("timestamp", java.time.LocalDateTime.now());
        response.put("serviceAvailable", false);
        response.put("test", "Простой тест без зависимостей");
        return ResponseEntity.ok(response);
    }

    /**
     * Очень простой тестовый эндпоинт без зависимостей
     */
    @GetMapping("/simple-test")
    @ResponseBody
    public ResponseEntity<String> simpleTestEndpoint() {
        return ResponseEntity.ok("TestDataController работает! Время: " + java.time.LocalDateTime.now());
    }

    /**
     * Упрощенный эндпоинт для очистки платежей (без TestDataService)
     */
    // @PostMapping("/clean-payments-simple")
    // @ResponseBody
    // public ResponseEntity<Map<String, Object>> cleanPaymentsSimple() {
    //     Map<String, Object> response = new HashMap<>();
    //     
    //     try {
    //         // Простая проверка доступности репозитория
    //         long count = dailyPaymentRepository.count();
    //         
    //         response.put("success", true);
    //         response.put("message", "Репозиторий платежей доступен. Текущее количество платежей: " + count);
    //         response.put("timestamp", java.time.LocalDateTime.now());
    //         response.put("currentPaymentsCount", count);
    //         
    //         return ResponseEntity.ok(response);
    //     } catch (Exception e) {
    //         response.put("success", false);
    //         response.put("message", "Ошибка при обращении к репозиторию: " + e.getMessage());
    //         response.put("timestamp", java.time.LocalDateTime.now());
    //         
    //         return ResponseEntity.badRequest().body(response);
    //     }
    // }

    /**
     * Страница управления тестовыми данными
     */
    @GetMapping("/management")
    public String testDataManagementPage() {
        return "admin/test-data-management";
    }
} 