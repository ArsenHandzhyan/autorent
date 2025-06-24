package ru.anapa.autorent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
public class PaymentNotificationListener {
    private static final Logger logger = LoggerFactory.getLogger(PaymentNotificationListener.class);
    private final PaymentNotificationService notificationService;

    public PaymentNotificationListener(PaymentNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentNotification(PaymentNotificationEvent event) {
        try {
            if (event.isProcessed()) {
                notificationService.sendPaymentProcessedNotification(event.getPayment());
            } else {
                notificationService.sendPaymentFailedNotification(event.getPayment(), event.getErrorMsg());
                notificationService.sendAdminPaymentFailureNotification(event.getPayment(), event.getErrorMsg());
            }
        } catch (Exception e) {
            logger.error("Ошибка при отправке уведомлений (Listener): {}", e.getMessage());
        }
    }
} 