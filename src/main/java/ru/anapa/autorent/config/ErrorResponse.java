package ru.anapa.autorent.config;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
class ErrorResponse {
    private String message;
    private String details;
    private LocalDateTime timestamp;
}
