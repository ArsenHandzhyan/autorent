package ru.anapa.autorent.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "allow_negative_balance", nullable = false)
    private boolean allowNegativeBalance = true;

    @Column(name = "credit_limit")
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(name = "account_number", unique = true)
    private String accountNumber;

    @Column(name = "initial_balance", nullable = false)
    private BigDecimal initialBalance = BigDecimal.ZERO;

    @Column(name = "max_rental_amount")
    private BigDecimal maxRentalAmount;

    @Column(name = "max_rental_duration")
    private Integer maxRentalDuration;

    @PrePersist
    public void generateAccountNumber() {
        if (accountNumber == null) {
            // Генерируем номер счета в формате: ACC-YYYYMMDD-XXXXX
            // где XXXXX - случайное число от 10000 до 99999
            String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            int random = (int) (Math.random() * 90000) + 10000;
            accountNumber = String.format("ACC-%s-%d", timestamp, random);
        }
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }

    public BigDecimal getMaxRentalAmount() {
        return maxRentalAmount;
    }

    public void setMaxRentalAmount(BigDecimal maxRentalAmount) {
        this.maxRentalAmount = maxRentalAmount;
    }

    public Integer getMaxRentalDuration() {
        return maxRentalDuration;
    }

    public void setMaxRentalDuration(Integer maxRentalDuration) {
        this.maxRentalDuration = maxRentalDuration;
    }
} 