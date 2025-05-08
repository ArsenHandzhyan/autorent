package ru.anapa.autorent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.anapa.autorent.model.Rental;
import ru.anapa.autorent.service.RentalService;

import java.util.List;

@Controller
@RequestMapping("/admin/rentals") // Изменяем базовый URL
@PreAuthorize("hasRole('ADMIN')")
@Transactional
public class AdminRentalController {

    private final RentalService rentalService;

    @Autowired
    public AdminRentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping("")
    public String allRentals(Model model) {
        List<Rental> rentals = rentalService.findAllRentals();
        model.addAttribute("rentals", rentals);
        return "rentals/all-rentals";
    }

    @GetMapping("/{id}")
    public String rentalDetails(@PathVariable Long id, Model model) {
        Rental rental = rentalService.findById(id);
        model.addAttribute("rental", rental);
        return "rentals/rental-details";
    }

    @GetMapping("/status/{status}")
    public String rentalsByStatus(@PathVariable String status, Model model) {
        List<Rental> rentals = rentalService.findRentalsByStatus(status.toUpperCase());
        model.addAttribute("rentals", rentals);
        model.addAttribute("currentStatus", status.toUpperCase());
        return "rentals/all-rentals";
    }

    @PostMapping("/{id}/confirm-cancellation")
    public String confirmCancellation(@PathVariable Long id,
                                      @RequestParam(required = false) String notes,
                                      RedirectAttributes redirectAttributes) {
        try {
            Rental rental = rentalService.findById(id);

            // Добавляем примечания администратора
            if (notes != null && !notes.trim().isEmpty()) {
                String currentNotes = rental.getNotes() != null ? rental.getNotes() + "\n\n" : "";
                currentNotes += "Примечания администратора при отмене: " + notes;
                rental.setNotes(currentNotes);
                rentalService.updateRental(rental);
            }

            // Отменяем аренду
            rentalService.cancelRental(id);
            redirectAttributes.addFlashAttribute("success", "Запрос на отмену аренды подтвержден!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/pending-cancellations";
    }

    @PostMapping("/{id}/reject-cancellation")
    public String rejectCancellation(@PathVariable Long id,
                                     @RequestParam(required = false) String notes,
                                     RedirectAttributes redirectAttributes) {
        try {
            // Отклоняем запрос на отмену
            rentalService.rejectCancellationRequest(id, notes);
            redirectAttributes.addFlashAttribute("success", "Запрос на отмену аренды отклонен!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/pending-cancellations";
    }

    @PostMapping("/{id}/approve")
    public String approveRental(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            rentalService.approveRental(id);
            redirectAttributes.addFlashAttribute("success", "Аренда успешно одобрена!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/rentals";
    }

    @PostMapping("/{id}/complete")
    public String completeRental(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            rentalService.completeRental(id);
            redirectAttributes.addFlashAttribute("success", "Аренда успешно завершена!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/rentals";
    }

    @PostMapping("/{id}/cancel")
    public String cancelRental(@PathVariable Long id,
                               @RequestParam(required = false) String notes,
                               RedirectAttributes redirectAttributes) {
        try {
            Rental rental = rentalService.findById(id);

            // Добавляем примечания администратора
            if (notes != null && !notes.trim().isEmpty()) {
                String currentNotes = rental.getNotes() != null ? rental.getNotes() + "\n\n" : "";
                currentNotes += "Примечания администратора при отмене: " + notes;
                rental.setNotes(currentNotes);
                rentalService.updateRental(rental);
            }

            // Отменяем аренду
            rentalService.cancelRental(id);
            redirectAttributes.addFlashAttribute("success", "Аренда успешно отменена!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/rentals";
    }

    @PostMapping("/{id}/update-notes")
    public String updateRentalNotes(@PathVariable Long id,
                                    @RequestParam String notes,
                                    RedirectAttributes redirectAttributes) {
        try {
            Rental rental = rentalService.findById(id);
            rental.setNotes(notes);
            rentalService.updateRental(rental);
            redirectAttributes.addFlashAttribute("success", "Примечания успешно обновлены!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/rentals/" + id;
    }
}