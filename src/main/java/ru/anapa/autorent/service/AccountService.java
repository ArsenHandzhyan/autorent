package ru.anapa.autorent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.anapa.autorent.model.Account;
import ru.anapa.autorent.model.Transaction;
import ru.anapa.autorent.model.User;
import ru.anapa.autorent.repository.AccountRepository;
import ru.anapa.autorent.repository.TransactionRepository;
import ru.anapa.autorent.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

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
        
        if (!account.isAllowNegativeBalance() && newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Negative balance is not allowed for this account");
        }
        
        if (account.getCreditLimit().compareTo(BigDecimal.ZERO) > 0 
            && newBalance.abs().compareTo(account.getCreditLimit()) > 0) {
            throw new RuntimeException("Credit limit exceeded");
        }
        
        account.setBalance(newBalance);
        account = accountRepository.save(account);

        // Создаем запись о транзакции
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(amount);
        transaction.setType(amount.compareTo(BigDecimal.ZERO) > 0 ? 
            Transaction.TransactionType.DEPOSIT : Transaction.TransactionType.RENT_PAYMENT);
        transaction.setDescription(amount.compareTo(BigDecimal.ZERO) > 0 ? 
            "Пополнение счета" : "Оплата аренды");
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
        Account account = getAccountByUserId(userId);
        return transactionRepository.findByAccountIdOrderByDateDesc(account.getId());
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account getAccountById(Long id) {
        return accountRepository.findById(id).orElseThrow(() -> new RuntimeException("Счет не найден: " + id));
    }

    @Transactional
    public void updateAccountFromAdmin(Long id, Account updated) {
        Account account = getAccountById(id);
        account.setBalance(updated.getBalance());
        account.setCreditLimit(updated.getCreditLimit());
        account.setAllowNegativeBalance(updated.isAllowNegativeBalance());
        accountRepository.save(account);
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
} 