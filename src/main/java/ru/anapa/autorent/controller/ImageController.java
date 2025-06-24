package ru.anapa.autorent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.anapa.autorent.model.CarImage;
import ru.anapa.autorent.repository.CarImageRepository;

import java.util.Optional;

@Controller
@RequestMapping("/images")
public class ImageController {

    @Autowired
    private CarImageRepository carImageRepository;

    @GetMapping("/car/{imageId}")
    public ResponseEntity<byte[]> getCarImage(@PathVariable Long imageId) {
        Optional<CarImage> imageOpt = carImageRepository.findById(imageId);
        
        if (imageOpt.isPresent()) {
            CarImage image = imageOpt.get();
            
            // Если есть данные изображения в БД
            if (image.getImageData() != null && image.getImageData().length > 0) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(image.getContentType()));
                headers.setContentLength(image.getImageData().length);
                
                return new ResponseEntity<>(image.getImageData(), headers, HttpStatus.OK);
            }
            
            // Если нет данных в БД, но есть URL (для обратной совместимости)
            if (image.getImageUrl() != null && !image.getImageUrl().isEmpty()) {
                // Возвращаем ошибку, так как файл не найден
                return ResponseEntity.notFound().build();
            }
        }
        
        return ResponseEntity.notFound().build();
    }
} 