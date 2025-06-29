package ru.anapa.autorent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import ru.anapa.autorent.service.DailyPaymentService;
import ru.anapa.autorent.model.DailyPayment;
import ru.anapa.autorent.model.Rental;
import ru.anapa.autorent.service.RentalService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TestController {

    private final DailyPaymentService dailyPaymentService;
    private final RentalService rentalService;

    @Autowired
    public TestController(DailyPaymentService dailyPaymentService, RentalService rentalService) {
        this.dailyPaymentService = dailyPaymentService;
        this.rentalService = rentalService;
    }

    /**
     * Простой тестовый эндпоинт для проверки статуса системы
     */
    @GetMapping("/test-status")
    public String testStatus() {
        return "SUCCESS: Система AutoRent работает. Время: " + LocalDate.now();
    }

    /**
     * Тестовый эндпоинт для обработки всех платежей
     */
    @GetMapping("/test-payments")
    public String testPayments() {
        try {
            dailyPaymentService.processAllUnprocessedPayments();
            return "SUCCESS: Все платежи обработаны. Проверьте логи сервера.";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * Тестовый эндпоинт для диагностики платежей
     */
    @GetMapping("/test-diagnose")
    public String testDiagnose() {
        try {
            dailyPaymentService.diagnoseAllPayments();
            return "SUCCESS: Диагностика выполнена. Проверьте логи сервера.";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * Тестовый эндпоинт для обработки пропущенных платежей при старте
     */
    @GetMapping("/test-startup-payments")
    public String testStartupPayments() {
        try {
            dailyPaymentService.processAllMissedPaymentsOnStartup();
            return "SUCCESS: Обработка пропущенных платежей при старте выполнена. Проверьте логи сервера.";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * Тестовый эндпоинт для обработки платежей за период
     */
    @GetMapping("/test-period-payments")
    public String testPeriodPayments(@RequestParam String startDate, @RequestParam String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);
            
            dailyPaymentService.processMissedPaymentsForPeriod(start, end);
            return String.format("SUCCESS: Обработка платежей за период %s - %s выполнена. Проверьте логи сервера.", startDate, endDate);
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * Тестовый эндпоинт для обработки платежей за конкретную дату
     */
    @GetMapping("/test-date-payments/{date}")
    public String testDatePayments(@PathVariable String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate paymentDate = LocalDate.parse(date, formatter);
            
            dailyPaymentService.processMissedPaymentsForPeriod(paymentDate, paymentDate);
            return String.format("SUCCESS: Обработка платежей за дату %s выполнена. Проверьте логи сервера.", date);
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * Тестовый эндпоинт для полной диагностики и обработки
     */
    @GetMapping("/test-full-process")
    public String testFullProcess() {
        try {
            StringBuilder result = new StringBuilder();
            
            result.append("=== НАЧАЛО ПОЛНОЙ ОБРАБОТКИ ===\n");
            
            // 1. Диагностика
            result.append("1. Выполняем диагностику...\n");
            dailyPaymentService.diagnoseAllPayments();
            
            // 2. Обработка пропущенных платежей при старте
            result.append("2. Обрабатываем пропущенные платежи при старте...\n");
            dailyPaymentService.processAllMissedPaymentsOnStartup();
            
            // 3. Обработка всех необработанных платежей
            result.append("3. Обрабатываем все необработанные платежи...\n");
            dailyPaymentService.processAllUnprocessedPayments();
            
            // 4. Финальная диагностика
            result.append("4. Выполняем финальную диагностику...\n");
            dailyPaymentService.diagnoseAllPayments();
            
            result.append("=== ПОЛНАЯ ОБРАБОТКА ЗАВЕРШЕНА ===\n");
            result.append("Проверьте логи сервера для детальной информации.");
            
            return result.toString();
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * Тестовый эндпоинт для обработки конкретного платежа по ID
     */
    @GetMapping("/test-process-payment/{paymentId}")
    public String testProcessSpecificPayment(@PathVariable Long paymentId) {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== ОБРАБОТКА ПЛАТЕЖА ID ").append(paymentId).append(" ===\n");
            
            // Получаем платеж
            DailyPayment payment = dailyPaymentService.getPaymentById(paymentId);
            if (payment == null) {
                return "ERROR: Платеж с ID " + paymentId + " не найден";
            }
            
            result.append("Найден платеж:\n");
            result.append("  ID: ").append(payment.getId()).append("\n");
            result.append("  Статус: ").append(payment.getStatus()).append("\n");
            result.append("  Сумма: ").append(payment.getAmount()).append("\n");
            result.append("  Дата: ").append(payment.getPaymentDate()).append("\n");
            result.append("  Примечания: ").append(payment.getNotes()).append("\n");
            
            if (payment.getAccount() != null) {
                result.append("  Счет ID: ").append(payment.getAccount().getId()).append("\n");
                result.append("  Баланс: ").append(payment.getAccount().getBalance()).append("\n");
                result.append("  Кредитный лимит: ").append(payment.getAccount().getCreditLimit()).append("\n");
                result.append("  Разрешен минус: ").append(payment.getAccount().isAllowNegativeBalance()).append("\n");
            }
            
            // Обрабатываем платеж
            result.append("\nНачинаем обработку...\n");
            dailyPaymentService.processSpecificPayment(paymentId);
            
            // Получаем обновленный платеж
            DailyPayment updatedPayment = dailyPaymentService.getPaymentById(paymentId);
            result.append("\nРезультат обработки:\n");
            result.append("  Новый статус: ").append(updatedPayment.getStatus()).append("\n");
            result.append("  Новые примечания: ").append(updatedPayment.getNotes()).append("\n");
            
            if (updatedPayment.getAccount() != null) {
                result.append("  Новый баланс: ").append(updatedPayment.getAccount().getBalance()).append("\n");
            }
            
            result.append("\nПроверьте логи сервера для детальной информации.");
            return result.toString();
            
        } catch (Exception e) {
            return "ERROR: " + e.getMessage() + "\nStacktrace: " + e.getStackTrace()[0];
        }
    }

    /**
     * Тест кодировки платежей пользователя
     */
    @GetMapping("/test-user-payments-encoding")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testUserPaymentsEncoding() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Получаем платежи для пользователя с ID 1 (admin61@example.com)
            List<DailyPayment> payments = dailyPaymentService.findPaymentsByUserWithCarData(1L);
            
            List<Map<String, Object>> paymentData = new ArrayList<>();
            for (DailyPayment payment : payments) {
                Map<String, Object> paymentInfo = new HashMap<>();
                paymentInfo.put("id", payment.getId());
                paymentInfo.put("notes", payment.getNotes());
                paymentInfo.put("notesHex", payment.getNotes() != null ? 
                    java.util.Arrays.toString(payment.getNotes().getBytes("UTF-8")) : "null");
                paymentInfo.put("paymentDate", payment.getPaymentDate());
                paymentInfo.put("amount", payment.getAmount());
                paymentInfo.put("status", payment.getStatus());
                paymentData.add(paymentInfo);
            }
            
            response.put("success", true);
            response.put("payments", paymentData);
            response.put("totalPayments", payments.size());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Ошибка при получении платежей: " + e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Тест проверки платежей аренды 54
     */
    @GetMapping("/test-rental-54-payments")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testRental54Payments() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Получаем аренду 54
            Rental rental = rentalService.findById(54L);
            if (rental == null) {
                response.put("success", false);
                response.put("error", "Аренда с ID 54 не найдена");
                return ResponseEntity.ok(response);
            }
            
            // Получаем платежи для аренды 54
            List<DailyPayment> payments = dailyPaymentService.getPaymentsByRental(rental);
            
            List<Map<String, Object>> paymentData = new ArrayList<>();
            for (DailyPayment payment : payments) {
                Map<String, Object> paymentInfo = new HashMap<>();
                paymentInfo.put("id", payment.getId());
                paymentInfo.put("paymentDate", payment.getPaymentDate());
                paymentInfo.put("amount", payment.getAmount());
                paymentInfo.put("status", payment.getStatus());
                paymentInfo.put("notes", payment.getNotes());
                paymentInfo.put("notesHex", payment.getNotes() != null ? 
                    java.util.Arrays.toString(payment.getNotes().getBytes("UTF-8")) : "null");
                paymentData.add(paymentInfo);
            }
            
            response.put("success", true);
            response.put("rental", Map.of(
                "id", rental.getId(),
                "startDate", rental.getStartDate(),
                "endDate", rental.getEndDate(),
                "totalCost", rental.getTotalCost()
            ));
            response.put("payments", paymentData);
            response.put("totalPayments", payments.size());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Ошибка при получении платежей: " + e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(response);
    }
} 