package ru.anapa.autorent.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.anapa.autorent.dto.RentalDto;
import ru.anapa.autorent.model.Rental;
import ru.anapa.autorent.model.User;
import ru.anapa.autorent.service.CarService;
import ru.anapa.autorent.service.RentalService;
import ru.anapa.autorent.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/rentals")
public class RentalController {

    private final RentalService rentalService;
    private final UserService userService;
    private final CarService carService;

    @Autowired
    public RentalController(RentalService rentalService, UserService userService, CarService carService) {
        this.rentalService = rentalService;
        this.userService = userService;
        this.carService = carService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public String myRentals(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(authentication.getName());
        List<Rental> rentals = rentalService.findRentalsByUser(user);
        model.addAttribute("rentals", rentals);
        return "rentals/my-rentals";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/new/{carId}")
    public String showRentalForm(@PathVariable Long carId, Model model) {
        model.addAttribute("car", carService.findCarById(carId));
        model.addAttribute("rental", new RentalDto());
        return "rentals/new";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/new/{carId}")
    public String createRental(@PathVariable Long carId,
                               @Valid @ModelAttribute("rental") RentalDto rentalDto,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("car", carService.findCarById(carId));
            return "rentals/new";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(authentication.getName());

        try {
            Rental rental = rentalService.createRental(
                    user,
                    carId,
                    rentalDto.getStartDate(),
                    rentalDto.getEndDate()
            );

            // Если есть примечания, сохраняем их
            if (rentalDto.getNotes() != null && !rentalDto.getNotes().trim().isEmpty()) {
                rental.setNotes(rentalDto.getNotes());
                rentalService.updateRental(rental);
            }

            redirectAttributes.addFlashAttribute("success", "Аренда успешно создана!");
            return "redirect:/rentals";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("car", carService.findCarById(carId));
            return "rentals/new";
        }
    }

    // Добавляем специальный обработчик для URL /rentals/all-rentals
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all-rentals")
    public String allRentalsAlternative(Model model) {
        // Перенаправляем на правильный URL
        return "redirect:/rentals/all-rentals";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public String rentalDetails(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(authentication.getName());
        Rental rental = rentalService.findById(id);

        // Проверяем, принадлежит ли аренда текущему пользователю или пользователь админ
        if (!rental.getUser().getId().equals(user.getId()) &&
                !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/rentals";
        }

        model.addAttribute("rental", rental);
        return "rentals/details";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/cancel")
    public String cancelRental(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(authentication.getName());

        Rental rental = rentalService.findById(id);

        // Проверяем, принадлежит ли аренда текущему пользователю или пользователь админ
        if (rental.getUser().getId().equals(user.getId()) ||
                authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            rentalService.cancelRental(id);
            redirectAttributes.addFlashAttribute("success", "Аренда успешно отменена!");
        } else {
            redirectAttributes.addFlashAttribute("error", "У вас нет прав для отмены этой аренды!");
        }

        // Если админ, возвращаем на страницу всех аренд, иначе на страницу аренд пользователя
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/rentals/all-rentals";
        } else {
            return "redirect:/rentals";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public String allRentals(Model model) {
        List<Rental> rentals = rentalService.findAllRentals();
        model.addAttribute("rentals", rentals);
        return "admin/all-rentals";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/complete")
    public String completeRental(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            rentalService.completeRental(id);
            redirectAttributes.addFlashAttribute("success", "Аренда успешно завершена!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rentals/all-rentals";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/notes")
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
        return "redirect:/rentals/" + id;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status/{status}")
    public String getRentalsByStatus(@PathVariable String status, Model model) {
        List<Rental> rentals = rentalService.findRentalsByStatus(status.toUpperCase());
        model.addAttribute("rentals", rentals);
        model.addAttribute("currentStatus", status.toUpperCase());
        return "admin/all-rentals";
    }

    // Исправляем метод для одобрения аренды
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/approve")
    public String approveRental(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            rentalService.approveRental(id);
            redirectAttributes.addFlashAttribute("success", "Аренда успешно одобрена!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rentals/all-rentals";
    }
}