package ru.anapa.autorent.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.anapa.autorent.model.Account;
import ru.anapa.autorent.model.Transaction;
import ru.anapa.autorent.model.User;
import ru.anapa.autorent.service.AccountService;
import ru.anapa.autorent.service.UserService;
import ru.anapa.autorent.dto.AccountEventDto;
import ru.anapa.autorent.model.AccountHistory;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
    private final AccountService accountService;
    private final UserService userService;

    /**
     * Страница профиля пользователя
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public String profile(Model model) {
        logger.info("Отображение страницы профиля пользователя");
        
        User currentUser = getCurrentUser();
        Account userAccount = accountService.getAccountByUserId(currentUser.getId());
        List<AccountEventDto> accountEvents = getAccountEvents(currentUser.getId());
        
        model.addAttribute("user", currentUser);
        model.addAttribute("account", userAccount);
        model.addAttribute("accountEvents", accountEvents);
        model.addAttribute("pageTitle", "Мой профиль");
        
        return "account/profile";
    }

    /**
     * Страница настроек аккаунта
     */
    @GetMapping("/settings")
    @PreAuthorize("isAuthenticated()")
    public String settings(Model model) {
        logger.info("Отображение страницы настроек аккаунта");
        
        User currentUser = getCurrentUser();
        model.addAttribute("user", currentUser);
        model.addAttribute("pageTitle", "Настройки аккаунта");
        
        return "account/settings";
    }

    /**
     * Страница истории счета
     */
    @GetMapping("/transactions")
    @PreAuthorize("isAuthenticated()")
    public String transactions(Model model) {
        logger.info("Отображение страницы истории счета");
        
        User currentUser = getCurrentUser();
        List<AccountEventDto> accountEvents = getAccountEvents(currentUser.getId());
        
        model.addAttribute("accountEvents", accountEvents);
        model.addAttribute("pageTitle", "История счета");
        
        return "account/account-history";
    }

    private List<AccountEventDto> getAccountEvents(Long userId) {
        Account userAccount = accountService.getAccountByUserId(userId);
        List<Transaction> transactions = accountService.getAccountTransactions(userId);
        List<AccountHistory> accountHistory = accountService.getAccountHistory(userAccount.getId());

        List<AccountEventDto> accountEvents = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("#,##0.00 '₽'");

        for (Transaction t : transactions) {
            AccountEventDto event = new AccountEventDto();
            event.setEventDate(t.getCreatedAt());
            event.setEventType("Транзакция");
            event.setDescription(t.getDescription());
            event.setAmount(df.format(t.getAmount()));
            event.setBalanceAfter(t.getType().toString());
            accountEvents.add(event);
        }

        for (AccountHistory h : accountHistory) {
            AccountEventDto event = new AccountEventDto();
            event.setEventDate(h.getChangeDate());
            event.setEventType("Изменение");

            String translatedFieldName = translateFieldName(h.getFieldName());
            String translatedNewValue = "allowNegativeBalance".equalsIgnoreCase(h.getFieldName())
                    ? translateBooleanValue(h.getNewValue())
                    : h.getNewValue();

            event.setDescription("Изменение: " + translatedFieldName);
            event.setFieldName(translatedFieldName);
            event.setNewValue(translatedNewValue);
            event.setOldValue("allowNegativeBalance".equalsIgnoreCase(h.getFieldName())
                    ? translateBooleanValue(h.getOldValue())
                    : h.getOldValue());
            event.setChangedBy(h.getChangedBy());
            accountEvents.add(event);
        }

        accountEvents.sort(Comparator.comparing(AccountEventDto::getEventDate).reversed());
        return accountEvents;
    }

    private String translateFieldName(String fieldName) {
        return switch (fieldName) {
            case "balance" -> "Баланс";
            case "creditLimit" -> "Кредитный лимит";
            case "allowNegativeBalance" -> "Минусовый баланс";
            case "initialBalance" -> "Начальный баланс";
            default -> fieldName;
        };
    }

    private String translateBooleanValue(String value) {
        if ("true".equalsIgnoreCase(value)) return "Да";
        if ("false".equalsIgnoreCase(value)) return "Нет";
        return value;
    }

    /**
     * Обновление профиля пользователя
     */
    @PostMapping("/settings/update-profile")
    @PreAuthorize("isAuthenticated()")
    public String updateProfile(@RequestParam String firstName,
                               @RequestParam String lastName,
                               @RequestParam String phone,
                               RedirectAttributes redirectAttributes) {
        logger.info("Обновление профиля пользователя");
        
        try {
            User currentUser = getCurrentUser();
            currentUser.setFirstName(firstName);
            currentUser.setLastName(lastName);
            currentUser.setPhone(phone);
            
            userService.updateUser(currentUser);
            
            redirectAttributes.addFlashAttribute("success", "Профиль успешно обновлен");
        } catch (Exception e) {
            logger.error("Ошибка при обновлении профиля", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении профиля");
        }
        
        return "redirect:/account/settings";
    }

    /**
     * Изменение пароля пользователя
     */
    @PostMapping("/settings/change-password")
    @PreAuthorize("isAuthenticated()")
    public String changePassword(@RequestParam String currentPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                RedirectAttributes redirectAttributes) {
        logger.info("Изменение пароля пользователя");
        
        try {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Новые пароли не совпадают");
                return "redirect:/account/settings";
            }
            
            User currentUser = getCurrentUser();
            User updatedUser = userService.changePassword(currentUser.getId(), currentPassword, newPassword);
            
            if (updatedUser != null) {
                redirectAttributes.addFlashAttribute("success", "Пароль успешно изменен");
            } else {
                redirectAttributes.addFlashAttribute("error", "Неверный текущий пароль");
            }
        } catch (Exception e) {
            logger.error("Ошибка при изменении пароля", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при изменении пароля: " + e.getMessage());
        }
        
        return "redirect:/account/settings";
    }

    /**
     * Удаление аккаунта пользователя
     */
    @PostMapping("/settings/delete-account")
    @PreAuthorize("isAuthenticated()")
    public String deleteAccount(RedirectAttributes redirectAttributes) {
        logger.info("Удаление аккаунта пользователя");
        
        try {
            User currentUser = getCurrentUser();
            userService.deleteUser(currentUser.getId());
            
            redirectAttributes.addFlashAttribute("success", "Аккаунт успешно удален");
            return "redirect:/logout";
        } catch (Exception e) {
            logger.error("Ошибка при удалении аккаунта", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении аккаунта");
            return "redirect:/account/settings";
        }
    }

    // REST API методы (оставляем для совместимости)
    @GetMapping("/api/ping")
    @ResponseBody
    public String ping() {
        logger.info("AccountController /api/accounts/ping called");
        return "AccountController is working!";
    }

    @GetMapping("/api/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#userId)")
    @ResponseBody
    public ResponseEntity<Account> getAccount(@PathVariable Long userId) {
        logger.info("Получение счета пользователя с id={}", userId);
        return ResponseEntity.ok(accountService.getAccountByUserId(userId));
    }

    @GetMapping("/api")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<List<Account>> getAllAccounts() {
        logger.info("Получение всех счетов (админ)");
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping("/api/me")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Account> getCurrentUserAccount() {
        logger.info("Получение счета текущего пользователя");
        return ResponseEntity.ok(accountService.getAccountByUserId(getCurrentUserId()));
    }

    @GetMapping("/api/me/transactions")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<List<Transaction>> getCurrentUserTransactions() {
        logger.info("Получение истории транзакций текущего пользователя");
        return ResponseEntity.ok(accountService.getAccountTransactions(getCurrentUserId()));
    }

    @PostMapping("/api/{userId}/balance")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Account> updateBalance(
            @PathVariable Long userId,
            @RequestParam BigDecimal amount) {
        logger.info("Изменение баланса пользователя id={}, сумма={}", userId, amount);
        return ResponseEntity.ok(accountService.updateBalance(userId, amount));
    }

    @PostMapping("/api/{userId}/credit-limit")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Account> setCreditLimit(
            @PathVariable Long userId,
            @RequestParam BigDecimal creditLimit) {
        logger.info("Изменение кредитного лимита пользователя id={}, лимит={}", userId, creditLimit);
        return ResponseEntity.ok(accountService.setCreditLimit(userId, creditLimit));
    }

    @PostMapping("/api/{userId}/allow-negative")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Account> setAllowNegativeBalance(
            @PathVariable Long userId,
            @RequestParam boolean allow) {
        logger.info("Изменение разрешения минусового баланса пользователя id={}, allow={}", userId, allow);
        return ResponseEntity.ok(accountService.setAllowNegativeBalance(userId, allow));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userService.findByEmail(email);
    }

    private Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
} 