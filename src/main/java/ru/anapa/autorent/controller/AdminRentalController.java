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
import ru.anapa.autorent.model.Rental;
import ru.anapa.autorent.service.RentalService;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/rentals")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRentalController {

    private final RentalService rentalService;

    @Autowired
    public AdminRentalController(RentalService rentalService) {
        this.rentalService = rentalService;
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

        return "admin/rentals";
    }

    @GetMapping("/status/{status}")
    public String listRentalsByStatus(@PathVariable String status, Model model) {
        List<Rental> rentals = rentalService.findRentalsByStatus(status);
        model.addAttribute("rentals", rentals);
        model.addAttribute("status", status);
        return "admin/rentals-by-status";
    }

    @GetMapping("/{id}")
    public String getRentalDetails(@PathVariable Long id, Model model) {
        Rental rental = rentalService.getRentalById(id);
        model.addAttribute("rental", rental);
        return "admin/rental-details";
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
            return "redirect:/admin/rentals";
        }
    }

    @PostMapping("/{id}/cancel")
    public Object cancelRental(@PathVariable Long id,
                               @RequestParam(required = false) String notes,
                               RedirectAttributes redirectAttributes,
                               @RequestHeader(name = "X-Requested-With", required = false) String requestedWith) {
        try {
            Rental rental = rentalService.cancelRental(id, notes);

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
            return "redirect:/admin/rentals/" + id;
        } catch (RuntimeException e) {
            if ("XMLHttpRequest".equals(requestedWith)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", e.getMessage());
                return ResponseEntity.ok(response);
            }

            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/rentals/" + id;
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

        // Данные автомобиля
        Map<String, Object> carData = new HashMap<>();
        carData.put("brand", rental.getCar().getBrand());
        carData.put("model", rental.getCar().getModel());
        carData.put("year", rental.getCar().getYear());
        carData.put("registrationNumber", rental.getCar().getRegistrationNumber());
        carData.put("pricePerDay", rental.getCar().getPricePerDay());
        rentalData.put("car", carData);

        return rentalData;
    }
}