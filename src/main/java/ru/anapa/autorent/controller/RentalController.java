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

    // Этот метод перенаправляет на административный контроллер
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all-rentals")
    public String allRentals() {
        return "redirect:/admin/rentals";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/cancel/{id}")
    public String showCancelRentalForm(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(authentication.getName());
        Rental rental = rentalService.findById(id);

        // Проверяем, принадлежит ли аренда текущему пользователю или пользователь админ
        if (!rental.getUser().getId().equals(user.getId()) &&
                !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/rentals";
        }

        // Проверяем, можно ли отменить аренду
        if (rental.getStatus().equals("COMPLETED") || rental.getStatus().equals("CANCELLED")) {
            return "redirect:/rentals";
        }

        model.addAttribute("rental", rental);
        return "rentals/cancel";
    }

    // Метод для запроса отмены аренды пользователем
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cancel/{id}")
    public String requestCancellation(@PathVariable Long id,
                                      @RequestParam(required = false) String cancelReason,
                                      RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(authentication.getName());
        Rental rental = rentalService.findById(id);

        // Проверяем, принадлежит ли аренда текущему пользователю или пользователь админ
        if (!rental.getUser().getId().equals(user.getId()) &&
                !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/rentals";
        }

        try {
            rentalService.requestCancellation(id, cancelReason);
            redirectAttributes.addFlashAttribute("success", "Запрос на отмену аренды отправлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/rentals";
    }

    // Удаляем или комментируем все методы, которые конфликтуют с AdminRentalController
    // Например:
    /*
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/all-rentals/{id}/complete")
    public String completeRental(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // ...
    }
    */

    // Вместо этого можно добавить методы, которые перенаправляют на административный контроллер
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String redirectToAdminRentals() {
        return "redirect:/admin/rentals";
    }
}