package ru.anapa.autorent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.anapa.autorent.dto.PaymentHistoryDto;
import ru.anapa.autorent.dto.PaymentStatisticsDto;
import ru.anapa.autorent.model.Car;
import ru.anapa.autorent.model.Rental;
import ru.anapa.autorent.model.RentalStatus;
import ru.anapa.autorent.service.CarService;
import ru.anapa.autorent.service.DailyPaymentService;
import ru.anapa.autorent.service.RentalService;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/rentals")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRentalController {

    private final RentalService rentalService;
    private final CarService carService;
    private final DailyPaymentService dailyPaymentService;

    @Autowired
    public AdminRentalController(RentalService rentalService, CarService carService, DailyPaymentService dailyPaymentService) {
        this.rentalService = rentalService;
        this.carService = carService;
        this.dailyPaymentService = dailyPaymentService;
    }

    @GetMapping
    public String listRentals(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(defaultValue = "id") String sort,
                              @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<Rental> rentals = rentalService.findAllRentals(pageable);
        model.addAttribute("rentals", rentals);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", rentals.getTotalPages());
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("currentStatus", null);

        return "rentals/all-rentals";
    }

    @GetMapping("/status/{status}")
    public String listRentalsByStatus(@PathVariable String status, Model model) {
        List<Rental> rentals = rentalService.findRentalsByStatus(RentalStatus.valueOf(status.toUpperCase()));
        model.addAttribute("rentals", rentals);
        model.addAttribute("currentStatus", status);
        return "rentals/all-rentals";
    }

    @GetMapping("/{id}")
    public String getRentalDetails(@PathVariable Long id, Model model) {
        Rental rental = rentalService.getRentalById(id);
        
        // Получаем историю платежей для этой аренды
        List<PaymentHistoryDto> paymentHistory = dailyPaymentService.getPaymentsByRental(rental)
                .stream()
                .map(PaymentHistoryDto::fromDailyPayment)
                .collect(Collectors.toList());
        
        // Получаем статистику платежей
        List<ru.anapa.autorent.model.DailyPayment> payments = dailyPaymentService.getPaymentsByRental(rental);
        
        long totalPayments = payments.size();
        long processedPayments = payments.stream()
                .filter(p -> p.getStatus() == ru.anapa.autorent.model.DailyPayment.PaymentStatus.PROCESSED)
                .count();
        long failedPayments = payments.stream()
                .filter(p -> p.getStatus() == ru.anapa.autorent.model.DailyPayment.PaymentStatus.FAILED)
                .count();
        long pendingPayments = payments.stream()
                .filter(p -> p.getStatus() == ru.anapa.autorent.model.DailyPayment.PaymentStatus.PENDING)
                .count();
        
        java.math.BigDecimal totalAmount = payments.stream()
                .map(ru.anapa.autorent.model.DailyPayment::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        
        java.math.BigDecimal processedAmount = payments.stream()
                .filter(p -> p.getStatus() == ru.anapa.autorent.model.DailyPayment.PaymentStatus.PROCESSED)
                .map(ru.anapa.autorent.model.DailyPayment::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        
        PaymentStatisticsDto paymentStats = new PaymentStatisticsDto(
            totalPayments, processedPayments, failedPayments, 
            pendingPayments, totalAmount, processedAmount
        );
        
        model.addAttribute("rental", rental);
        model.addAttribute("paymentHistory", paymentHistory);
        model.addAttribute("paymentStats", paymentStats);
        
        return "rentals/admin-rental-details";
    }

    @PostMapping("/{id}/approve")
    public Object approveRental(@PathVariable Long id,
                                RedirectAttributes redirectAttributes,
                                @RequestHeader(name = "X-Requested-With", required = false) String requestedWith) {
        try {
            Rental rental = rentalService.approveRental(id);

            // Если это AJAX-запрос
            if ("XMLHttpRequest".equals(requestedWith)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Аренда успешно одобрена!");
                response.put("newStatus", rental.getStatus());
                response.put("rental", createRentalDataMap(rental));
                return ResponseEntity.ok(response);
            }

            redirectAttributes.addFlashAttribute("success", "Аренда успешно одобрена!");
            return "redirect:/admin/rentals";
        } catch (RuntimeException e) {
            if ("XMLHttpRequest".equals(requestedWith)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", e.getMessage());
                return ResponseEntity.ok(response);
            }

            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/rentals/all-rentals";
        }
    }

    @PostMapping("/{id}/cancel")
    public Object cancelRental(Model model,
                               @PathVariable Long id,
                               @RequestParam(required = false) String cancelReason,
                               RedirectAttributes redirectAttributes,
                               @RequestHeader(name = "X-Requested-With", required = false) String requestedWith) {
        try {
            Rental rental = rentalService.cancelRental(id, cancelReason);

            // Если это AJAX-запрос
            if ("XMLHttpRequest".equals(requestedWith)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Аренда успешно отменена!");
                response.put("newStatus", rental.getStatus());
                response.put("cancelReason", rental.getCancelReason());
                response.put("rental", createRentalDataMap(rental));
                return ResponseEntity.ok(response);
            }

            redirectAttributes.addFlashAttribute("success", "Аренда успешно отменена!");
            return "redirect:/admin/rentals";
        } catch (RuntimeException e) {
            if ("XMLHttpRequest".equals(requestedWith)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", e.getMessage());
                return ResponseEntity.ok(response);
            }
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/rentals";
        }
    }

    @PostMapping("/{id}/complete")
    public Object completeRental(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes,
                                 @RequestHeader(name = "X-Requested-With", required = false) String requestedWith) {
        try {
            Rental rental = rentalService.completeRental(id);

            // Если это AJAX-запрос
            if ("XMLHttpRequest".equals(requestedWith)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Аренда успешно завершена!");
                response.put("newStatus", rental.getStatus());
                response.put("rental", createRentalDataMap(rental));
                return ResponseEntity.ok(response);
            }

            redirectAttributes.addFlashAttribute("success", "Аренда успешно завершена!");
            return "redirect:/rentals/all-rentals";
        } catch (RuntimeException e) {
            if ("XMLHttpRequest".equals(requestedWith)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", e.getMessage());
                return ResponseEntity.ok(response);
            }

            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/rentals/all-rentals";
        }
    }

    @PostMapping("/{id}/confirm-cancellation")
    public Object confirmCancellation(@PathVariable Long id,
                                      RedirectAttributes redirectAttributes,
                                      @RequestHeader(name = "X-Requested-With", required = false) String requestedWith) {
        try {
            Rental rental = rentalService.confirmCancellation(id);

            // Если это AJAX-запрос
            if ("XMLHttpRequest".equals(requestedWith)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Запрос на отмену подтвержден!");
                response.put("newStatus", rental.getStatus());
                response.put("cancelReason", rental.getCancelReason());
                response.put("rental", createRentalDataMap(rental));
                return ResponseEntity.ok(response);
            }

            redirectAttributes.addFlashAttribute("success", "Запрос на отмену подтвержден!");
            return "redirect:/rentals/all-rentals";
        } catch (RuntimeException e) {
            if ("XMLHttpRequest".equals(requestedWith)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", e.getMessage());
                return ResponseEntity.ok(response);
            }

            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/rentals/all-rentals";
        }
    }

    @PostMapping("/{id}/reject-cancellation")
    public Object rejectCancellation(@PathVariable Long id,
                                     @RequestParam(required = false) String adminNotes,
                                     RedirectAttributes redirectAttributes,
                                     @RequestHeader(name = "X-Requested-With", required = false) String requestedWith) {
        try {
            Rental rental = rentalService.rejectCancellation(id, adminNotes);

            // Если это AJAX-запрос
            if ("XMLHttpRequest".equals(requestedWith)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Запрос на отмену отклонен!");
                response.put("newStatus", rental.getStatus());
                response.put("notes", rental.getNotes());
                response.put("rental", createRentalDataMap(rental));
                return ResponseEntity.ok(response);
            }

            redirectAttributes.addFlashAttribute("success", "Запрос на отмену отклонен!");
            return "redirect:/rentals/all-rentals";
        } catch (RuntimeException e) {
            if ("XMLHttpRequest".equals(requestedWith)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", e.getMessage());
                return ResponseEntity.ok(response);
            }

            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/rentals/all-rentals";
        }
    }

    @PostMapping("/{id}/update-notes")
    public Object updateRentalNotes(@PathVariable Long id,
                                    @RequestParam(required = false) String notes,
                                    RedirectAttributes redirectAttributes,
                                    @RequestHeader(name = "X-Requested-With", required = false) String requestedWith) {
        try {
            Rental rental = rentalService.updateRentalNotes(id, notes);

            // Если это AJAX-запрос
            if ("XMLHttpRequest".equals(requestedWith)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Примечания успешно обновлены!");
                response.put("notes", rental.getNotes());
                response.put("rental", createRentalDataMap(rental));
                return ResponseEntity.ok(response);
            }

            redirectAttributes.addFlashAttribute("success", "Примечания успешно обновлены!");
            return "redirect:/rentals/view/" + id;
        } catch (RuntimeException e) {
            if ("XMLHttpRequest".equals(requestedWith)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", e.getMessage());
                return ResponseEntity.ok(response);
            }

            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/rentals/view/" + id;
        }
    }

    // Вспомогательный метод для создания объекта данных аренды
    private Map<String, Object> createRentalDataMap(Rental rental) {
        Map<String, Object> rentalData = new HashMap<>();
        rentalData.put("id", rental.getId());
        rentalData.put("status", rental.getStatus());

        // Форматирование дат
        if (rental.getStartDate() != null) {
            rentalData.put("startDate", rental.getStartDate().toString());
        }
        if (rental.getEndDate() != null) {
            rentalData.put("endDate", rental.getEndDate().toString());
        }
        if (rental.getCreatedAt() != null) {
            rentalData.put("createdAt", rental.getCreatedAt().toString());
        }

        rentalData.put("totalCost", rental.getTotalCost());
        rentalData.put("notes", rental.getNotes());
        rentalData.put("cancelReason", rental.getCancelReason());

        // Данные пользователя
        Map<String, Object> userData = new HashMap<>();
        userData.put("firstName", rental.getUser().getFirstName());
        userData.put("lastName", rental.getUser().getLastName());
        userData.put("email", rental.getUser().getEmail());
        userData.put("phone", rental.getUser().getPhone());
        rentalData.put("user", userData);

        // Изменить строку с totalCost
        rentalData.put("totalCost", String.format("%.2f", rental.getTotalCost())); // форматируем число


        Map<String, Object> carData = new HashMap<>();
        carData.put("brand", rental.getCar().getBrand());
        carData.put("model", rental.getCar().getModel());
        carData.put("year", rental.getCar().getYear());
        carData.put("registrationNumber", rental.getCar().getRegistrationNumber());
        carData.put("dailyRate", String.format("%.2f", rental.getCar().getDailyRate())); // форматируем число
        rentalData.put("car", carData);

        return rentalData;
    }
}