-- Миграция для обновления структуры таблицы car_images
-- Добавляем новые поля для хранения изображений в БД

ALTER TABLE car_images 
ADD COLUMN image_data LONGBLOB NULL COMMENT 'Данные изображения в формате BLOB',
ADD COLUMN content_type VARCHAR(100) NULL COMMENT 'MIME тип изображения',
ADD COLUMN file_name VARCHAR(255) NULL COMMENT 'Оригинальное имя файла';

-- Обновляем существующие записи, устанавливая content_type по умолчанию
UPDATE car_images 
SET content_type = 'image/jpeg' 
WHERE content_type IS NULL AND image_url IS NOT NULL;

-- Добавляем индекс для оптимизации поиска по car_id
CREATE INDEX idx_car_images_car_id ON car_images(car_id);

-- Добавляем индекс для оптимизации поиска по display_order
CREATE INDEX idx_car_images_display_order ON car_images(display_order); 