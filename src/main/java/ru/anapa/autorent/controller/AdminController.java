package ru.anapa.autorent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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
    public String adminDashboard(Model model) {
        List<User> users = userService.findAllUsers();
        List<Rental> pendingRentals = rentalService.findRentalsByStatus("PENDING");
        List<Rental> activeRentals = rentalService.findRentalsByStatus("ACTIVE");

        model.addAttribute("userCount", users.size());
        model.addAttribute("pendingRentals", pendingRentals);
        model.addAttribute("activeRentals", activeRentals);

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
        userService.updateUser(user);
        return "redirect:/admin/users";
    }
}