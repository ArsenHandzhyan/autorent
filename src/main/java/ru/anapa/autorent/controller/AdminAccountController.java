package ru.anapa.autorent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.validation.BindingResult;
import ru.anapa.autorent.model.AccountHistory;
import ru.anapa.autorent.service.AccountService;
import ru.anapa.autorent.service.UserService;
import ru.anapa.autorent.model.Account;
import ru.anapa.autorent.model.Transaction;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestParam;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import ru.anapa.autorent.dto.AccountEventDto;
import java.util.ArrayList;
import java.util.Comparator;
import java.text.DecimalFormat;

@Controller
@RequestMapping("/admin/accounts")
public class AdminAccountController {

    private final AccountService accountService;
    private final UserService userService;

    @Autowired
    public AdminAccountController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    @GetMapping
    public String listAccounts(Model model) {
        model.addAttribute("accounts", accountService.getAllAccounts());
        return "admin/accounts";
    }

    @GetMapping("/{id}/edit")
    public String editAccount(@PathVariable Long id, Model model) {
        Account account = accountService.getAccountById(id);
        model.addAttribute("account", account);
        return "admin/account-edit";
    }

    @PostMapping("/{id}/edit")
    public String updateAccount(@PathVariable Long id, @ModelAttribute Account account,
                              @RequestParam(required = false) String reason,
                              Authentication authentication) {
        String changedBy = authentication.getName();
        accountService.updateAccountFromAdmin(id, account, changedBy, reason);
        String successMessage = URLEncoder.encode("Настройки счета обновлены", StandardCharsets.UTF_8);
        return "redirect:/admin/accounts/" + id + "?success=" + successMessage;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showAccountDetails(@PathVariable Long id, Model model) {
        Account account = accountService.getAccountById(id);
        List<Transaction> transactions = accountService.getAccountTransactions(account.getUser().getId());
        List<AccountHistory> accountHistory = accountService.getAccountHistory(id);

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
            String translatedOldValue = "allowNegativeBalance".equalsIgnoreCase(h.getFieldName())
                    ? translateBooleanValue(h.getOldValue())
                    : h.getOldValue();

            event.setDescription("Изменение: " + translatedFieldName);
            event.setFieldName(translatedFieldName);
            event.setOldValue(translatedOldValue);
            event.setNewValue(translatedNewValue);
            event.setChangedBy(h.getChangedBy());
            accountEvents.add(event);
        }

        accountEvents.sort(Comparator.comparing(AccountEventDto::getEventDate).reversed());

        model.addAttribute("account", account);
        model.addAttribute("accountEvents", accountEvents);
        
        return "admin/accounts/details";
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
} 