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
            case "PENDING_CANCELLATION":
                return "Ожидает отмены";
            case "APPROVED":
                return "Подтверждена";
            case "REJECTED":
                return "Отклонена";
            default:
                return status;
        }
    }
}