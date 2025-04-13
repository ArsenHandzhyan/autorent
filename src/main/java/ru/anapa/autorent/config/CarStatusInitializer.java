package ru.anapa.autorent.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.anapa.autorent.service.RentalService;

@Component
public class CarStatusInitializer implements CommandLineRunner {

    private final RentalService rentalService;
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    public CarStatusInitializer(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @Override
    public void run(String... args) {
        // Синхронизируем статусы автомобилей при запуске приложения
        rentalService.synchronizeAllCarStatuses();
        logger.info("Статусы автомобилей синхронизированы при запуске приложения");
    }
}
