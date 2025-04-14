package ru.anapa.autorent.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.anapa.autorent.service.RentalService;

@Component
public class ScheduledTasks {

    private final RentalService rentalService;

    @Autowired
    public ScheduledTasks(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @Scheduled(cron = "0 0 * * * *") // Каждый час
    public void updateCarStatuses() {
        rentalService.synchronizeAllCarStatuses();
    }
}
