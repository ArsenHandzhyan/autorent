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

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

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

        // Фильтруем активные аренды
        List<Rental> activeRentals = rentals.stream()
                .filter(rental ->
                        "ACTIVE".equals(rental.getStatus()) ||
                                "PENDING".equals(rental.getStatus()) ||
                                "PENDING_CANCELLATION".equals(rental.getStatus()))
                .collect(Collectors.toList());

        // Фильтруем историю аренд
        List<Rental> historyRentals = rentals.stream()
                .filter(rental ->
                        "COMPLETED".equals(rental.getStatus()) ||
                                "CANCELLED".equals(rental.getStatus()))
                .collect(Collectors.toList());

        model.addAttribute("rentals", rentals);
        model.addAttribute("activeRentals", activeRentals);
        model.addAttribute("historyRentals", historyRentals);

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

    // Вместо этого можно добавить методы, которые перенаправляют на административный контроллер
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String redirectToAdminRentals() {
        return "redirect:/admin/rentals";
    }

    @GetMapping("/{id}")
    public String getRentalDetails(@PathVariable Long id, Model model, Principal principal) {
        // Получаем текущего пользователя
        String email = principal.getName();

        // Получаем аренду по ID
        Rental rental = rentalService.getRentalById(id);

        // Проверяем, принадлежит ли аренда текущему пользователю
        if (!rental.getUser().getEmail().equals(email)) {
            return "redirect:/rentals?error=Доступ запрещен";
        }

        model.addAttribute("rental", rental);
        return "rentals/rental-details"; // Имя шаблона для отображения деталей аренды
    }
}