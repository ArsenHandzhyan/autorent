package ru.anapa.autorent.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
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

    /**
     * Обработка пропущенных платежей при старте приложения
     */
    @EventListener(ApplicationReadyEvent.class)
    public void processMissedPaymentsOnStartup() {
        logger.info("=== ОБРАБОТКА ПРОПУЩЕННЫХ ПЛАТЕЖЕЙ ПРИ СТАРТЕ ПРИЛОЖЕНИЯ ===");
        try {
            // Сначала диагностируем текущее состояние
            dailyPaymentService.diagnoseAllPayments();
            
            // Затем обрабатываем все пропущенные платежи
            dailyPaymentService.processAllMissedPaymentsOnStartup();
            
            logger.info("Обработка пропущенных платежей при старте завершена успешно");
        } catch (Exception e) {
            logger.error("Критическая ошибка при обработке пропущенных платежей при старте: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 * * * *") // Каждый час
    public void updateCarStatuses() {
        logger.info("=== ЗАПУСК СИНХРОНИЗАЦИИ СТАТУСОВ АВТОМОБИЛЕЙ ===");
        try {
            rentalService.synchronizeAllCarStatuses();
            logger.info("Синхронизация статусов автомобилей завершена успешно");
        } catch (Exception e) {
            logger.error("Ошибка при синхронизации статусов автомобилей: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 1 0 * * *") // Каждый день в 00:01
    public void processDailyRentalPayments() {
        logger.info("=== ЗАПУСК ОБРАБОТКИ ЕЖЕДНЕВНЫХ ПЛАТЕЖЕЙ ПО АРЕНДЕ ===");
        try {
            LocalDate today = LocalDate.now();
            
            // Создаем платежи для активных аренд на сегодня
            logger.info("Создание ежедневных платежей для даты: {}", today);
            dailyPaymentService.createDailyPaymentsForActiveRentals(today);
            
            // Обрабатываем все ожидающие платежи на сегодня
            logger.info("Обработка ежедневных платежей для даты: {}", today);
            dailyPaymentService.processDailyPayments(today);
            
            logger.info("Обработка ежедневных платежей по аренде завершена успешно");
        } catch (Exception e) {
            logger.error("Ошибка при обработке ежедневных платежей по аренде: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 6 * * *") // Каждый день в 06:00
    public void retryFailedPayments() {
        logger.info("=== ЗАПУСК ПОВТОРНОЙ ОБРАБОТКИ НЕУДАЧНЫХ ПЛАТЕЖЕЙ ===");
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            logger.info("Повторная обработка платежей за дату: {}", yesterday);
            dailyPaymentService.processDailyPayments(yesterday);
            logger.info("Повторная обработка неудачных платежей завершена успешно");
        } catch (Exception e) {
            logger.error("Ошибка при повторной обработке неудачных платежей: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 30 2 * * *") // Каждый день в 02:30
    public void processAllUnprocessedPayments() {
        logger.info("=== ЗАПУСК ОБРАБОТКИ ВСЕХ НЕОБРАБОТАННЫХ ПЛАТЕЖЕЙ ===");
        try {
            dailyPaymentService.processAllUnprocessedPayments();
            logger.info("Обработка всех необработанных платежей завершена успешно");
        } catch (Exception e) {
            logger.error("Ошибка при обработке всех необработанных платежей: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 12 * * *") // Каждый день в 12:00
    public void diagnosePayments() {
        logger.info("=== ЗАПУСК ДИАГНОСТИКИ ПЛАТЕЖЕЙ ===");
        try {
            dailyPaymentService.diagnoseAllPayments();
            logger.info("Диагностика платежей завершена успешно");
        } catch (Exception e) {
            logger.error("Ошибка при диагностике платежей: {}", e.getMessage(), e);
        }
    }
}
