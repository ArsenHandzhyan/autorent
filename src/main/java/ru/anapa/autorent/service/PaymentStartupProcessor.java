package ru.anapa.autorent.service;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentStartupProcessor {
    private final DailyPaymentService dailyPaymentService;

    public PaymentStartupProcessor(DailyPaymentService dailyPaymentService) {
        this.dailyPaymentService = dailyPaymentService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void processPendingPaymentsOnStartup() {
        dailyPaymentService.processAllPendingPayments();
    }
} 