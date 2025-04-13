package ru.anapa.autorent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.anapa.autorent.model.Rental;
import ru.anapa.autorent.model.User;
import ru.anapa.autorent.service.RentalService;
import ru.anapa.autorent.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final RentalService rentalService;

    @Autowired
    public AdminController(UserService userService, @Lazy RentalService rentalService) {
        this.userService = userService;
        this.rentalService = rentalService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        // Проверяем, авторизован ли пользователь
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Необходимо авторизоваться для доступа к админ-панели");
            return "redirect:/auth/login";
        }

        // Проверяем, имеет ли пользователь роль ADMIN
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            // Если пользователь не админ, показываем сообщение и перенаправляем
            redirectAttributes.addFlashAttribute("error",
                    "У вас недостаточно прав для доступа к админ-панели. Пожалуйста, войдите как администратор.");

            // Сохраняем информацию о необходимости входа как администратор
            redirectAttributes.addFlashAttribute("adminLoginRequired", true);

            return "redirect:/auth/login";
        }

        List<User> users = userService.findAllUsers();
        List<Rental> pendingRentals = rentalService.findRentalsByStatus("PENDING");
        List<Rental> activeRentals = rentalService.findRentalsByStatus("ACTIVE");
        List<Rental> pendingCancellations = rentalService.findRentalsByStatus("PENDING_CANCELLATION");

        model.addAttribute("userCount", users.size());
        model.addAttribute("pendingRentals", pendingRentals);
        model.addAttribute("activeRentals", activeRentals);
        model.addAttribute("pendingCancellations", pendingCancellations);

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/users/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        List<Rental> userRentals = rentalService.findRentalsByUser(user);

        model.addAttribute("user", user);
        model.addAttribute("rentals", userRentals);

        return "admin/user-details";
    }

    @PostMapping("/users/{id}/promote")
    public String promoteToAdmin(@PathVariable Long id) {
        userService.promoteToAdmin(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/disable")
    public String disableUser(@PathVariable Long id) {
        User user = userService.findById(id);
        user.setEnabled(false);
        userService.updateUser(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/enable")
    public String enableUser(@PathVariable Long id) {
        User user = userService.findById(id);
        user.setEnabled(true);
        userService.updateUser(user);
        return "redirect:/admin/users";
    }

    @GetMapping("/pending-cancellations")
    public String pendingCancellations(Model model) {
        List<Rental> rentals = rentalService.findRentalsByStatus("PENDING_CANCELLATION");
        model.addAttribute("rentals", rentals);
        return "admin/pending-cancellations";
    }

    @GetMapping("/statistics")
    public String statistics(Model model) {
        // Получаем количество аренд по статусам
        int pendingCount = rentalService.findRentalsByStatus("PENDING").size();
        int activeCount = rentalService.findRentalsByStatus("ACTIVE").size();
        int completedCount = rentalService.findRentalsByStatus("COMPLETED").size();
        int cancelledCount = rentalService.findRentalsByStatus("CANCELLED").size();
        int pendingCancellationCount = rentalService.findRentalsByStatus("PENDING_CANCELLATION").size();

        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("cancelledCount", cancelledCount);
        model.addAttribute("pendingCancellationCount", pendingCancellationCount);

        return "admin/statistics";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/synchronize-car-statuses")
    public String synchronizeCarStatuses(RedirectAttributes redirectAttributes) {
        try {
            rentalService.synchronizeAllCarStatuses();
            redirectAttributes.addFlashAttribute("success", "Статусы автомобилей успешно синхронизированы");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при синхронизации статусов: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}