-- Создаем таблицу для изображений автомобилей
CREATE TABLE car_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    car_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    display_order INT,
    is_main BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE CASCADE
);

-- Переносим существующие изображения в новую таблицу
INSERT INTO car_images (car_id, image_url, description, display_order, is_main)
SELECT id, image_url, 'Основное изображение', 0, TRUE
FROM cars
WHERE image_url IS NOT NULL;

-- Удаляем старое поле image_url из таблицы cars
ALTER TABLE cars DROP COLUMN image_url; 