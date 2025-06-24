package ru.anapa.autorent.model;

public enum CarStatus {
    AVAILABLE("Доступен"),    // Доступен для аренды
    RENTED("Арендован"),       // Арендован
    MAINTENANCE("На обслуживании"),  // На обслуживании
    RESERVED("Забронирован"),     // Забронирован
    PENDING("Ожидает подтверждения"); // Ожидает подтверждения

    private final String displayName;

    CarStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 