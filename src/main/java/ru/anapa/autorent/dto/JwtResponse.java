package ru.anapa.autorent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private final String token;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final Long userId;
} 