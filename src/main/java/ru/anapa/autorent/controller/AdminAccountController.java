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
        return "redirect:/admin/accounts/" + id + "?success=Настройки счета обновлены";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showAccountDetails(@PathVariable Long id, Model model) {
        Account account = accountService.getAccountById(id);
        List<Transaction> transactions = accountService.getAccountTransactions(account.getUser().getId());
        List<AccountHistory> accountHistory = accountService.getAccountHistory(id);
        
        model.addAttribute("account", account);
        model.addAttribute("transactions", transactions);
        model.addAttribute("accountHistory", accountHistory);
        
        return "admin/accounts/details";
    }
} 