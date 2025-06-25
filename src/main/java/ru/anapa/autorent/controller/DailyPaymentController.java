package ru.anapa.autorent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.anapa.autorent.model.Account;
import ru.anapa.autorent.model.DailyPayment;
import ru.anapa.autorent.model.Rental;
import ru.anapa.autorent.model.User;
import ru.anapa.autorent.service.AccountService;
import ru.anapa.autorent.service.DailyPaymentService;
import ru.anapa.autorent.service.RentalService;
import ru.anapa.autorent.service.UserService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/payments")
@PreAuthorize("hasRole('ADMIN')")
public class DailyPaymentController {

    private final DailyPaymentService dailyPaymentService;
    private final RentalService rentalService;
    private final UserService userService;
    private final AccountService accountService;

    @Autowired
    public DailyPaymentController(DailyPaymentService dailyPaymentService, RentalService rentalService, UserService userService, AccountService accountService) {
        this.dailyPaymentService = dailyPaymentService;
        this.rentalService = rentalService;
        this.userService = userService;
        this.accountService = accountService;
    }

    /**
     * Страница со списком всех платежей
     */
    @GetMapping
    public String listPayments(Model model) {
        List<Rental> activeRentals = rentalService.findRentalsByStatus(ru.anapa.autorent.model.RentalStatus.ACTIVE);
        model.addAttribute("activeRentals", activeRentals);
        return "admin/payments/list";
    }

    /**
     * Страница с платежами для конкретной аренды
     */
    @GetMapping("/rental/{rentalId}")
    public String rentalPayments(@PathVariable Long rentalId, Model model) {
        Rental rental = rentalService.findById(rentalId);
        List<DailyPayment> payments = dailyPaymentService.getPaymentsByRental(rental);
        DailyPaymentService.PaymentStatistics statistics = dailyPaymentService.getPaymentStatistics(rental);
        
        model.addAttribute("rental", rental);
        model.addAttribute("payments", payments);
        model.addAttribute("statistics", statistics);
        
        return "admin/payments/rental-payments";
    }

    /**
     * API для создания платежей на конкретную дату
     */
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createPayments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paymentDate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            dailyPaymentService.createDailyPaymentsForActiveRentals(paymentDate);
            response.put("success", true);
            response.put("message", "Платежи успешно созданы для даты " + paymentDate);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Ошибка при создании платежей: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * API для обработки платежей на конкретную дату
     */
    @PostMapping("/process")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processPayments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paymentDate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            dailyPaymentService.processDailyPayments(paymentDate);
            response.put("success", true);
            response.put("message", "Платежи успешно обработаны для даты " + paymentDate);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Ошибка при обработке платежей: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * API для получения статистики платежей по аренде
     */
    @GetMapping("/statistics/{rentalId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPaymentStatistics(@PathVariable Long rentalId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Rental rental = rentalService.findById(rentalId);
            DailyPaymentService.PaymentStatistics statistics = dailyPaymentService.getPaymentStatistics(rental);
            
            response.put("success", true);
            response.put("statistics", statistics);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Ошибка при получении статистики: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * API для ручной обработки конкретного платежа
     */
    @PostMapping("/process/{paymentId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processSpecificPayment(@PathVariable Long paymentId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            dailyPaymentService.processSpecificPayment(paymentId);
            response.put("success", true);
            response.put("message", "Платеж успешно обработан");
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Ошибка при обработке платежа: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * API для ручной актуализации всех не обработанных платежей (PENDING/FAILED)
     */
    @PostMapping("/process-all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processAllUnprocessedPayments() {
        Map<String, Object> response = new HashMap<>();
        try {
            dailyPaymentService.processAllUnprocessedPayments();
            response.put("success", true);
            response.put("message", "Все не обработанные платежи успешно актуализированы");
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Ошибка при актуализации: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
} 