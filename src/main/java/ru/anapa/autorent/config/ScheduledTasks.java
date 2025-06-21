package ru.anapa.autorent.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.anapa.autorent.service.DailyPaymentService;
import ru.anapa.autorent.service.RentalService;

import java.time.LocalDate;

@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    private final RentalService rentalService;
    private final DailyPaymentService dailyPaymentService;

    @Autowired
    public ScheduledTasks(RentalService rentalService, DailyPaymentService dailyPaymentService) {
        this.rentalService = rentalService;
        this.dailyPaymentService = dailyPaymentService;
    }

    @Scheduled(cron = "0 0 * * * *") // Каждый час
    public void updateCarStatuses() {
        logger.info("Запуск синхронизации статусов автомобилей");
        try {
            rentalService.synchronizeAllCarStatuses();
            logger.info("Синхронизация статусов автомобилей завершена успешно");
        } catch (Exception e) {
            logger.error("Ошибка при синхронизации статусов автомобилей: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 1 0 * * *") // Каждый день в 00:01
    public void processDailyRentalPayments() {
        logger.info("Запуск обработки ежедневных платежей по аренде");
        try {
            LocalDate today = LocalDate.now();
            
            // Создаем платежи для активных аренд на сегодня
            dailyPaymentService.createDailyPaymentsForActiveRentals(today);
            
            // Обрабатываем все ожидающие платежи на сегодня
            dailyPaymentService.processDailyPayments(today);
            
            logger.info("Обработка ежедневных платежей по аренде завершена успешно");
        } catch (Exception e) {
            logger.error("Ошибка при обработке ежедневных платежей по аренде: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 6 * * *") // Каждый день в 06:00
    public void retryFailedPayments() {
        logger.info("Запуск повторной обработки неудачных платежей");
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            dailyPaymentService.processDailyPayments(yesterday);
            logger.info("Повторная обработка неудачных платежей завершена успешно");
        } catch (Exception e) {
            logger.error("Ошибка при повторной обработке неудачных платежей: {}", e.getMessage(), e);
        }
    }
}
