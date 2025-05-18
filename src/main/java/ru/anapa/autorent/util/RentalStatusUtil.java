package ru.anapa.autorent.util;

public class RentalStatusUtil {
    
    public static String getStatusText(String status) {
        if (status == null) {
            return "";
        }
        
        switch (status) {
            case "PENDING":
                return "Ожидает подтверждения";
            case "ACTIVE":
                return "Активна";
            case "COMPLETED":
                return "Завершена";
            case "CANCELLED":
                return "Отменена";
            case "CANCELLATION_REQUESTED":
            case "PENDING_CANCELLATION":
                return "Запрос на отмену";
            default:
                return status;
        }
    }
}