package ru.anapa.autorent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.anapa.autorent.model.CarImage;
import ru.anapa.autorent.repository.CarImageRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class ImageMigrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageMigrationService.class);
    
    @Autowired
    private CarImageRepository carImageRepository;
    
    /**
     * Мигрирует все существующие изображения из файловой системы в базу данных
     */
    @Transactional
    public void migrateAllImages() {
        logger.info("Начинаем миграцию изображений из файловой системы в БД...");
        
        List<CarImage> imagesWithUrls = carImageRepository.findAll().stream()
                .filter(img -> img.getImageUrl() != null && !img.getImageUrl().isEmpty())
                .filter(img -> img.getImageData() == null || img.getImageData().length == 0)
                .toList();
        
        logger.info("Найдено {} изображений для миграции", imagesWithUrls.size());
        
        int migratedCount = 0;
        int errorCount = 0;
        
        for (CarImage image : imagesWithUrls) {
            try {
                if (migrateImage(image)) {
                    migratedCount++;
                    logger.debug("Изображение {} успешно мигрировано", image.getId());
                } else {
                    errorCount++;
                    logger.warn("Не удалось мигрировать изображение {}", image.getId());
                }
            } catch (Exception e) {
                errorCount++;
                logger.error("Ошибка при миграции изображения {}: {}", image.getId(), e.getMessage());
            }
        }
        
        logger.info("Миграция завершена. Успешно: {}, Ошибок: {}", migratedCount, errorCount);
    }
    
    /**
     * Мигрирует одно изображение из файловой системы в БД
     */
    @Transactional
    public boolean migrateImage(CarImage image) {
        try {
            String imageUrl = image.getImageUrl();
            if (imageUrl == null || imageUrl.isEmpty()) {
                logger.warn("URL изображения пустой для ID: {}", image.getId());
                return false;
            }
            
            // Убираем начальный слеш если есть
            String filePath = imageUrl.startsWith("/") ? imageUrl.substring(1) : imageUrl;
            
            // Проверяем существование файла
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                logger.warn("Файл не найден: {}", filePath);
                return false;
            }
            
            // Читаем данные файла
            byte[] imageData = Files.readAllBytes(path);
            
            // Определяем content type по расширению файла
            String contentType = determineContentType(filePath);
            
            // Обновляем изображение в БД
            image.setImageData(imageData);
            image.setContentType(contentType);
            image.setFileName(new File(filePath).getName());
            
            carImageRepository.save(image);
            
            logger.info("Изображение {} мигрировано, размер: {} байт", image.getId(), imageData.length);
            return true;
            
        } catch (IOException e) {
            logger.error("Ошибка чтения файла для изображения {}: {}", image.getId(), e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Ошибка миграции изображения {}: {}", image.getId(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Определяет MIME тип по расширению файла
     */
    private String determineContentType(String filePath) {
        String extension = filePath.substring(filePath.lastIndexOf('.') + 1).toLowerCase();
        
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "bmp" -> "image/bmp";
            default -> "image/jpeg"; // По умолчанию
        };
    }
    
    /**
     * Проверяет статус миграции
     */
    public void checkMigrationStatus() {
        long totalImages = carImageRepository.count();
        long imagesInDb = carImageRepository.findAll().stream()
                .filter(img -> img.getImageData() != null && img.getImageData().length > 0)
                .count();
        long imagesWithUrl = carImageRepository.findAll().stream()
                .filter(img -> img.getImageUrl() != null && !img.getImageUrl().isEmpty())
                .count();
        
        logger.info("Статус миграции изображений:");
        logger.info("- Всего изображений: {}", totalImages);
        logger.info("- В БД (BLOB): {}", imagesInDb);
        logger.info("- С URL: {}", imagesWithUrl);
        logger.info("- Требуют миграции: {}", imagesWithUrl - imagesInDb);
    }
} 