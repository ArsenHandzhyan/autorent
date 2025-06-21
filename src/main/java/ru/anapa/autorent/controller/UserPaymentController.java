package ru.anapa.autorent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.anapa.autorent.model.Account;
import ru.anapa.autorent.model.DailyPayment;
import ru.anapa.autorent.model.Rental;
import ru.anapa.autorent.model.User;
import ru.anapa.autorent.service.AccountService;
import ru.anapa.autorent.service.DailyPaymentService;
import ru.anapa.autorent.service.RentalService;
import ru.anapa.autorent.service.UserService;

import java.util.List;

@Controller
public class UserPaymentController {

    private final DailyPaymentService dailyPaymentService;
    private final RentalService rentalService;
    private final UserService userService;
    private final AccountService accountService;

    @Autowired
    public UserPaymentController(DailyPaymentService dailyPaymentService, RentalService rentalService, UserService userService, AccountService accountService) {
        this.dailyPaymentService = dailyPaymentService;
        this.rentalService = rentalService;
        this.userService = userService;
        this.accountService = accountService;
    }

    /**
     * Страница платежей для пользователя
     */
    @GetMapping("/user/payments")
    public String userPayments(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        String userEmail = authentication.getName();
        User user = userService.findByEmail(userEmail);
        
        if (user == null) {
            return "redirect:/login";
        }

        // Получаем активные аренды пользователя
        List<Rental> activeRentals = rentalService.findActiveRentalsByUser(user.getId());
        
        // Получаем историю платежей пользователя
        List<DailyPayment> userPayments = dailyPaymentService.findPaymentsByUser(user.getId());
        
        // Получаем счет пользователя
        Account userAccount = accountService.findByUserId(user.getId());
        
        model.addAttribute("activeRentals", activeRentals);
        model.addAttribute("userPayments", userPayments);
        model.addAttribute("userAccount", userAccount);
        model.addAttribute("user", user);
        
        return "rentals/user-payments";
    }
} 