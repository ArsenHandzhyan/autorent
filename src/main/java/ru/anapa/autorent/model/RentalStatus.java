package ru.anapa.autorent.model;

public enum RentalStatus {
    PENDING("Ожидает подтверждения"),
    APPROVED("Подтверждена"),
    REJECTED("Отклонена"),
    ACTIVE("Активна"),
    COMPLETED("Завершена"),
    CANCELLED("Отменена"),
    PENDING_CANCELLATION("Ожидает отмены");

    private final String displayName;

    RentalStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 