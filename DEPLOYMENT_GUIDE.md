# Руководство по развертыванию AutoRent v1.5.0

## 🆕 Новые возможности v1.5.0

### Сохранение изображений в базе данных
- Изображения автомобилей теперь хранятся как BLOB данные в БД
- Универсальная доступность при деплое на любой сервер
- Отсутствие зависимости от файловой системы
- Оптимизированная загрузка через специальный контроллер

## 📋 Предварительные требования

- Java 17+
- MySQL 8.0+
- Maven 3.6+
- Docker (опционально)

## 🗄️ Миграция базы данных

### 1. Выполните SQL-скрипт для обновления структуры БД:

```sql
-- Добавляем новые поля для хранения изображений в БД
ALTER TABLE car_images 
ADD COLUMN image_data LONGBLOB NULL COMMENT 'Данные изображения в формате BLOB',
ADD COLUMN content_type VARCHAR(100) NULL COMMENT 'MIME тип изображения',
ADD COLUMN file_name VARCHAR(255) NULL COMMENT 'Оригинальное имя файла';

-- Обновляем существующие записи
UPDATE car_images 
SET content_type = 'image/jpeg' 
WHERE content_type IS NULL AND image_url IS NOT NULL;

-- Добавляем индексы для оптимизации
CREATE INDEX idx_car_images_car_id ON car_images(car_id);
CREATE INDEX idx_car_images_display_order ON car_images(display_order);
```

### 2. Миграция существующих изображений

После запуска приложения выполните миграцию существующих изображений:

```bash
# Проверка статуса миграции
curl -X GET http://localhost:8080/admin/images/migration-status \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Запуск миграции
curl -X POST http://localhost:8080/admin/images/migrate \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

Или используйте веб-интерфейс администратора.

## 🚀 Развертывание

### Локальное развертывание

1. **Клонирование и настройка:**
```bash
git clone <repository-url>
cd autorent
```

2. **Настройка базы данных:**
```sql
CREATE DATABASE autorent CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'autorent'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON autorent.* TO 'autorent'@'localhost';
FLUSH PRIVILEGES;
```

3. **Настройка конфигурации:**
```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/autorent
spring.datasource.username=autorent
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
```

4. **Запуск приложения:**
```bash
mvn spring-boot:run
```

### Docker развертывание

1. **Сборка образа:**
```bash
docker build -t autorent:v1.5.0 .
```

2. **Запуск с Docker Compose:**
```bash
docker-compose up -d
```

### Продакшн развертывание

1. **Настройка продакшн профиля:**
```properties
# application-prod.properties
spring.profiles.active=prod
spring.jpa.hibernate.ddl-auto=validate
spring.thymeleaf.cache=true
```

2. **Настройка переменных окружения:**
```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://your-db-host:3306/autorent
export SPRING_DATASOURCE_USERNAME=your-username
export SPRING_DATASOURCE_PASSWORD=your-password
export JWT_SECRET=your-secret-key
```

3. **Запуск JAR файла:**
```bash
java -jar autorent-1.5.0.jar --spring.profiles.active=prod
```

## 🔧 Конфигурация

### Основные настройки

```properties
# База данных
spring.datasource.url=jdbc:mysql://localhost:3306/autorent
spring.datasource.username=autorent
spring.datasource.password=password

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Изображения (новое в v1.5.0)
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

# JWT
jwt.secret=your-jwt-secret-key
jwt.expiration=86400000

# SMS (SMS.ru)
sms.api.key=your-sms-api-key
```

### Настройки для продакшна

```properties
# Производительность
spring.jpa.hibernate.ddl-auto=validate
spring.thymeleaf.cache=true
spring.web.resources.cache.cachecontrol.max-age=365d
server.compression.enabled=true

# Безопасность
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.http-only=true

# Логирование
logging.level.org.springframework=WARN
logging.level.ru.anapa.autorent=INFO
logging.file.name=/var/log/autorent/application.log
```

## 🔍 Мониторинг и диагностика

### Проверка работы изображений

1. **Проверка API изображений:**
```bash
curl -I http://localhost:8080/images/car/1
```

2. **Проверка статуса миграции:**
```bash
curl -X GET http://localhost:8080/admin/images/migration-status
```

3. **Проверка логов:**
```bash
tail -f logs/autorent-dev.log
```

### Ключевые метрики

- Количество изображений в БД
- Размер BLOB данных
- Время загрузки изображений
- Количество ошибок при загрузке

## 🐛 Устранение неполадок

### Проблемы с изображениями

1. **Изображения не отображаются:**
   - Проверьте выполнение миграции БД
   - Убедитесь, что контроллер `/images/car/{imageId}` работает
   - Проверьте логи на наличие ошибок

2. **Ошибка "No static resource":**
   - Убедитесь, что изображения мигрированы в БД
   - Проверьте, что старый обработчик `/uploads/**` отключен

3. **Медленная загрузка изображений:**
   - Проверьте размер BLOB данных
   - Рассмотрите возможность сжатия изображений
   - Настройте кэширование

### Проблемы с базой данных

1. **Ошибка "LONGBLOB column":**
   - Убедитесь, что миграция БД выполнена
   - Проверьте версию MySQL (требуется 8.0+)

2. **Нехватка места на диске:**
   - Мониторьте размер БД
   - Рассмотрите архивирование старых изображений

## 📊 Производительность

### Рекомендации по оптимизации

1. **Размер изображений:**
   - Сжимайте изображения перед загрузкой
   - Используйте современные форматы (WebP)
   - Ограничьте максимальный размер файла

2. **Кэширование:**
   - Настройте HTTP кэширование для изображений
   - Используйте CDN для статических ресурсов

3. **База данных:**
   - Мониторьте размер БД
   - Регулярно выполняйте оптимизацию таблиц

## 🔒 Безопасность

### Рекомендации

1. **Валидация файлов:**
   - Проверяйте MIME типы
   - Ограничивайте размер файлов
   - Сканируйте на вирусы

2. **Доступ к API:**
   - Используйте аутентификацию для административных функций
   - Ограничивайте доступ к API изображений при необходимости

3. **Шифрование:**
   - Используйте HTTPS в продакшне
   - Шифруйте чувствительные данные в БД

## 📞 Поддержка

При возникновении проблем:

1. Проверьте логи приложения
2. Убедитесь в корректности миграции БД
3. Проверьте настройки конфигурации
4. Обратитесь к документации API

---

**AutoRent v1.5.0** - надежное решение для аренды автомобилей с современной системой управления изображениями. 