package ru.anapa.autorent.model;

public enum CarStatus {
    AVAILABLE,    // Доступен для аренды
    RENTED,       // Арендован
    MAINTENANCE,  // На обслуживании
    RESERVED,     // Забронирован
    PENDING       // Ожидает подтверждения
} 