package ru.anapa.autorent.service;

import ru.anapa.autorent.model.DailyPayment;

public class PaymentNotificationEvent {
    private final DailyPayment payment;
    private final boolean processed;
    private final String errorMsg;

    public PaymentNotificationEvent(DailyPayment payment, boolean processed, String errorMsg) {
        this.payment = payment;
        this.processed = processed;
        this.errorMsg = errorMsg;
    }

    public DailyPayment getPayment() {
        return payment;
    }

    public boolean isProcessed() {
        return processed;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
} 