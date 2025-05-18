package ru.anapa.autorent.model;

public enum RentalStatus {
    PENDING,            // Ожидает подтверждения
    ACTIVE,            // Активная аренда
    COMPLETED,         // Завершена
    CANCELLED,         // Отменена
    PENDING_CANCELLATION  // Ожидает подтверждения отмены
} 