package ru.anapa.autorent.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.anapa.autorent.service.ImageMigrationService;

/**
 * Тестовый инициализатор для проверки работы с изображениями в БД
 * Запускается только в dev профиле
 */
@Component
@Profile("dev")
public class ImageTestInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageTestInitializer.class);
    
    @Autowired
    private ImageMigrationService imageMigrationService;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("=== ТЕСТИРОВАНИЕ СИСТЕМЫ ИЗОБРАЖЕНИЙ ===");
        
        // Проверяем статус миграции
        logger.info("Проверка статуса миграции изображений...");
        imageMigrationService.checkMigrationStatus();
        
        logger.info("=== ТЕСТИРОВАНИЕ ЗАВЕРШЕНО ===");
        logger.info("Для миграции изображений используйте: POST /admin/images/migrate");
        logger.info("Для проверки статуса используйте: GET /admin/images/migration-status");
    }
} 