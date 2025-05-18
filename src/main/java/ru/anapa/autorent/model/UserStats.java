package ru.anapa.autorent.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserStats {
    private String firstName;
    private String lastName;
    private int rentalCount;
    private double totalSpent;
    private LocalDateTime lastActivity;
} 