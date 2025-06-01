package ru.anapa.autorent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.anapa.autorent.model.Account;
import ru.anapa.autorent.model.AccountHistory;

import java.util.List;

public interface AccountHistoryRepository extends JpaRepository<AccountHistory, Long> {
    List<AccountHistory> findByAccountOrderByChangeDateDesc(Account account);
} 