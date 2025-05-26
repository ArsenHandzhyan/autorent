package ru.anapa.autorent.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.anapa.autorent.model.Account;
import ru.anapa.autorent.model.Transaction;
import ru.anapa.autorent.model.User;
import ru.anapa.autorent.service.AccountService;
import ru.anapa.autorent.service.UserService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
    private final AccountService accountService;
    private final UserService userService;

    @GetMapping("/ping")
    public String ping() {
        logger.info("AccountController /api/accounts/ping called");
        return "AccountController is working!";
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#userId)")
    public ResponseEntity<Account> getAccount(@PathVariable Long userId) {
        logger.info("Получение счета пользователя с id={}", userId);
        return ResponseEntity.ok(accountService.getAccountByUserId(userId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Account>> getAllAccounts() {
        logger.info("Получение всех счетов (админ)");
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Account> getCurrentUserAccount() {
        logger.info("Получение счета текущего пользователя");
        return ResponseEntity.ok(accountService.getAccountByUserId(getCurrentUserId()));
    }

    @GetMapping("/me/transactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Transaction>> getCurrentUserTransactions() {
        logger.info("Получение истории транзакций текущего пользователя");
        return ResponseEntity.ok(accountService.getAccountTransactions(getCurrentUserId()));
    }

    @PostMapping("/{userId}/balance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Account> updateBalance(
            @PathVariable Long userId,
            @RequestParam BigDecimal amount) {
        logger.info("Изменение баланса пользователя id={}, сумма={}", userId, amount);
        return ResponseEntity.ok(accountService.updateBalance(userId, amount));
    }

    @PostMapping("/{userId}/credit-limit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Account> setCreditLimit(
            @PathVariable Long userId,
            @RequestParam BigDecimal creditLimit) {
        logger.info("Изменение кредитного лимита пользователя id={}, лимит={}", userId, creditLimit);
        return ResponseEntity.ok(accountService.setCreditLimit(userId, creditLimit));
    }

    @PostMapping("/{userId}/allow-negative")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Account> setAllowNegativeBalance(
            @PathVariable Long userId,
            @RequestParam boolean allow) {
        logger.info("Изменение разрешения минусового баланса пользователя id={}, allow={}", userId, allow);
        return ResponseEntity.ok(accountService.setAllowNegativeBalance(userId, allow));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        return user.getId();
    }
} 