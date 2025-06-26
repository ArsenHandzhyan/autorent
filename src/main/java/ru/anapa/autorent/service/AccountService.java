package ru.anapa.autorent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.anapa.autorent.model.Account;
import ru.anapa.autorent.model.AccountHistory;
import ru.anapa.autorent.model.Transaction;
import ru.anapa.autorent.model.User;
import ru.anapa.autorent.repository.AccountRepository;
import ru.anapa.autorent.repository.AccountHistoryRepository;
import ru.anapa.autorent.repository.TransactionRepository;
import ru.anapa.autorent.repository.UserRepository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AccountHistoryRepository accountHistoryRepository;

    @Transactional
    public Account createAccount(User user) {
        Account account = new Account();
        account.setUser(user);
        return accountRepository.save(account);
    }

    @Transactional
    public Account getAccountByUserId(Long userId) {
        return accountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Account not found for user: " + userId));
    }

    @Transactional
    public Account updateBalance(Long userId, BigDecimal amount) {
        Account account = getAccountByUserId(userId);
        BigDecimal newBalance = account.getBalance().add(amount);
        
        // Если запрещён минус и кредитный лимит = 0, выбрасываем ошибку
        if (!account.isAllowNegativeBalance() && account.getCreditLimit().compareTo(BigDecimal.ZERO) == 0 && newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Negative balance is not allowed for this account");
        }
        
        boolean creditLimitExceeded = false;
        if (account.getCreditLimit().compareTo(BigDecimal.ZERO) > 0 
            && newBalance.abs().compareTo(account.getCreditLimit()) > 0) {
            creditLimitExceeded = true;
            org.slf4j.LoggerFactory.getLogger(AccountService.class).warn(
                "Превышен кредитный лимит для пользователя id={}: баланс после списания = {}, лимит = {}", 
                userId, newBalance, account.getCreditLimit()
            );
        }
        
        account.setBalance(newBalance);
        account = accountRepository.save(account);

        // Создаём запись о транзакции
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(amount);
        transaction.setType(amount.compareTo(BigDecimal.ZERO) > 0 ? 
            Transaction.TransactionType.DEPOSIT : Transaction.TransactionType.RENT_PAYMENT);
        String description = amount.compareTo(BigDecimal.ZERO) > 0 ? 
            "Пополнение счета" : "Оплата аренды";
        if (creditLimitExceeded) {
            description += " (ВНИМАНИЕ: превышен кредитный лимит)";
        }
        transaction.setDescription(description);
        transactionRepository.save(transaction);

        return account;
    }

    @Transactional
    public Account setCreditLimit(Long userId, BigDecimal creditLimit) {
        Account account = getAccountByUserId(userId);
        account.setCreditLimit(creditLimit);
        return accountRepository.save(account);
    }

    @Transactional
    public Account setAllowNegativeBalance(Long userId, boolean allow) {
        Account account = getAccountByUserId(userId);
        account.setAllowNegativeBalance(allow);
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getAccountTransactions(Long userId) {
        Account account = findByUserId(userId);
        if (account == null) {
            return Collections.emptyList();
        }
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(account.getId());
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account getAccountById(Long id) {
        return accountRepository.findById(id).orElseThrow(() -> new RuntimeException("Счет не найден: " + id));
    }

    @Transactional
    public void updateAccountFromAdmin(Long id, Account updated, String changedBy, String reason) {
        Account account = getAccountById(id);
        
        // Проверяем и сохраняем изменения для каждого поля
        if (!account.getBalance().equals(updated.getBalance())) {
            saveAccountHistory(account, changedBy, "balance", 
                account.getBalance().toString(), 
                updated.getBalance().toString(), 
                reason);
            account.setBalance(updated.getBalance());
        }
        
        if (!account.getInitialBalance().equals(updated.getInitialBalance())) {
            saveAccountHistory(account, changedBy, "initialBalance", 
                account.getInitialBalance().toString(), 
                updated.getInitialBalance().toString(), 
                reason);
            account.setInitialBalance(updated.getInitialBalance());
        }
        
        if (!account.getCreditLimit().equals(updated.getCreditLimit())) {
            saveAccountHistory(account, changedBy, "creditLimit", 
                account.getCreditLimit().toString(), 
                updated.getCreditLimit().toString(), 
                reason);
            account.setCreditLimit(updated.getCreditLimit());
        }
        
        if (account.isAllowNegativeBalance() != updated.isAllowNegativeBalance()) {
            saveAccountHistory(account, changedBy, "allowNegativeBalance", 
                String.valueOf(account.isAllowNegativeBalance()), 
                String.valueOf(updated.isAllowNegativeBalance()), 
                reason);
            account.setAllowNegativeBalance(updated.isAllowNegativeBalance());
        }
        
        if ((account.getMaxRentalAmount() == null && updated.getMaxRentalAmount() != null) ||
            (account.getMaxRentalAmount() != null && !account.getMaxRentalAmount().equals(updated.getMaxRentalAmount()))) {
            saveAccountHistory(account, changedBy, "maxRentalAmount", 
                account.getMaxRentalAmount() != null ? account.getMaxRentalAmount().toString() : "null", 
                updated.getMaxRentalAmount() != null ? updated.getMaxRentalAmount().toString() : "null", 
                reason);
            account.setMaxRentalAmount(updated.getMaxRentalAmount());
        }
        
        if ((account.getMaxRentalDuration() == null && updated.getMaxRentalDuration() != null) ||
            (account.getMaxRentalDuration() != null && !account.getMaxRentalDuration().equals(updated.getMaxRentalDuration()))) {
            saveAccountHistory(account, changedBy, "maxRentalDuration", 
                account.getMaxRentalDuration() != null ? account.getMaxRentalDuration().toString() : "null", 
                updated.getMaxRentalDuration() != null ? updated.getMaxRentalDuration().toString() : "null", 
                reason);
            account.setMaxRentalDuration(updated.getMaxRentalDuration());
        }
        
        accountRepository.save(account);
    }

    private void saveAccountHistory(Account account, String changedBy, String fieldName, 
                                  String oldValue, String newValue, String reason) {
        AccountHistory history = new AccountHistory(account, changedBy, fieldName, 
            oldValue, newValue, reason);
        accountHistoryRepository.save(history);
    }

    public List<AccountHistory> getAccountHistory(Long accountId) {
        Account account = getAccountById(accountId);
        return accountHistoryRepository.findByAccountOrderByChangeDateDesc(account);
    }

    public BigDecimal getTotalAccountsBalance() {
        return accountRepository.findAll().stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getAccountsCount() {
        return (int) accountRepository.count();
    }

    public Account getAccountByUserIdOrNull(Long userId) {
        return accountRepository.findByUserId(userId).orElse(null);
    }

    public Account findByUserId(Long userId) {
        return accountRepository.findByUserId(userId).orElse(null);
    }

    public Account findById(Long id) {
        return accountRepository.findById(id).orElse(null);
    }

    public void validateRentalConstraints(Long userId, BigDecimal rentalAmount, int durationDays) {
        Account account = getAccountByUserId(userId);
        
        // Проверка максимальной суммы аренды
        if (account.getMaxRentalAmount() != null && 
            rentalAmount.compareTo(account.getMaxRentalAmount()) > 0) {
            throw new RuntimeException("Сумма аренды превышает максимально допустимую для вашего счета");
        }
        
        // Проверка максимальной длительности аренды
        if (account.getMaxRentalDuration() != null && 
            durationDays > account.getMaxRentalDuration()) {
            throw new RuntimeException("Длительность аренды превышает максимально допустимую для вашего счета");
        }
        
        // Проверка достаточности средств
        BigDecimal requiredBalance = rentalAmount;
        if (!account.isAllowNegativeBalance()) {
            if (account.getBalance().compareTo(requiredBalance) < 0) {
                throw new RuntimeException("Недостаточно средств на счете");
            }
        } // если разрешён минус — всегда разрешаем, только логируем превышение лимита
        // (старую проверку кредитного лимита убираем)
    }
} 