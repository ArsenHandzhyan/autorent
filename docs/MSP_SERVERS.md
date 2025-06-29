# MSP Серверы MySQL для AutoRent

## 📋 Обзор

Система AutoRent поддерживает Multiple Server Profile (MSP) конфигурацию для подключения к различным MySQL серверам в зависимости от окружения.

## 🗄️ Доступные профили

### 1. **local** - Локальная разработка
- **Сервер**: `localhost:3306`
- **База данных**: `autorent`
- **Пользователь**: `root`
- **Пароль**: `root`
- **Порт приложения**: `8080`
- **Назначение**: Локальная разработка и тестирование

### 2. **dev** - Разработка
- **Сервер**: `uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com:3306`
- **База данных**: `cmwz7gjxubq6sk64`
- **Пользователь**: `wm02va8ppexvexe1`
- **Пароль**: `srj7xmugajaa2ww3`
- **Порт приложения**: `8080`
- **Назначение**: Общая разработка и тестирование

### 3. **test** - Тестирование
- **Сервер**: `test-mysql-server:3306`
- **База данных**: `autorent_test`
- **Пользователь**: `test_user`
- **Пароль**: `test_password_secure`
- **Порт приложения**: `8081`
- **Назначение**: Автоматизированное тестирование

### 4. **staging** - Предпродакшн
- **Сервер**: `staging-mysql-server:3306`
- **База данных**: `autorent_staging`
- **Пользователь**: `staging_user`
- **Пароль**: `staging_password_secure`
- **Порт приложения**: `8080`
- **Назначение**: Финальное тестирование перед продакшном

### 5. **prod** - Продакшн
- **Сервер**: `uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com:3306`
- **База данных**: `cmwz7gjxubq6sk64`
- **Пользователь**: `wm02va8ppexvexe1`
- **Пароль**: `srj7xmugajaa2ww3`
- **Порт приложения**: `8080`
- **Назначение**: Продакшн окружение

### 6. **backup** - Резервный сервер
- **Сервер**: `backup-mysql-server:3306`
- **База данных**: `autorent_backup`
- **Пользователь**: `backup_user`
- **Пароль**: `backup_password_secure`
- **Порт приложения**: `8082`
- **Назначение**: Резервное копирование и восстановление

## 🚀 Запуск приложения

### Windows
```bash
# Локальная разработка
scripts\start-local.bat

# Разработка
scripts\start-dev.bat

# Тестирование
scripts\start-test.bat

# Предпродакшн
scripts\start-staging.bat

# Продакшн
scripts\start-prod.bat

# Резервный сервер
scripts\start-backup.bat
```

### Linux/Mac
```bash
# Сделать скрипты исполняемыми
chmod +x scripts/*.sh

# Локальная разработка
./scripts/start-local.sh

# Разработка
./scripts/start-dev.sh

# Тестирование
./scripts/start-test.sh

# Предпродакшн
./scripts/start-staging.sh

# Продакшн
./scripts/start-prod.sh

# Резервный сервер
./scripts/start-backup.sh
```

### Maven команды
```bash
# Локальная разработка
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Разработка
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Тестирование
mvn spring-boot:run -Dspring-boot.run.profiles=test

# Предпродакшн
mvn spring-boot:run -Dspring-boot.run.profiles=staging

# Продакшн
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Резервный сервер
mvn spring-boot:run -Dspring-boot.run.profiles=backup
```

## 🔧 Конфигурация

### Файлы конфигурации
- `application.properties` - Основная конфигурация
- `application-local.properties` - Локальная разработка
- `application-dev.properties` - Разработка
- `application-test.properties` - Тестирование
- `application-staging.properties` - Предпродакшн
- `application-prod.properties` - Продакшн
- `application-backup.properties` - Резервный сервер

### Настройки пула соединений (HikariCP)
```properties
# Размер пула соединений
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5

# Таймауты
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.connection-timeout=20000

# Инициализация
spring.datasource.hikari.connection-init-sql=SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci
```

## 📊 Мониторинг

### API endpoints для мониторинга
```bash
# Проверка состояния подключения
GET /api/database/health

# Проверка производительности
GET /api/database/performance

# Проверка доступности таблиц
GET /api/database/tables

# Полная диагностика
GET /api/database/diagnostic

# Информация о конфигурации
GET /api/database/info

# Тестирование подключения
POST /api/database/test-connection
```

### Примеры ответов

#### Health Check
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": "UP",
  "connection": "OK",
  "query": "OK",
  "result": "OK",
  "database": "MySQL",
  "version": "8.0.33",
  "url": "jdbc:mysql://localhost:3306/autorent"
}
```

#### Performance Check
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "queryTime": 15,
  "status": "OK",
  "activeConnections": 3,
  "idleConnections": 7,
  "totalConnections": 10
}
```

## 🔒 Безопасность

### Требования к доступу
- Все API endpoints для мониторинга требуют роль `ADMIN`
- Пароли хранятся в зашифрованном виде
- SSL соединения для всех внешних серверов
- Ограниченный доступ к резервному серверу

### Рекомендации
1. Регулярно обновляйте пароли
2. Используйте VPN для доступа к продакшн серверам
3. Ведите логи всех подключений
4. Настройте алерты при недоступности серверов

## 🛠️ Устранение неполадок

### Частые проблемы

#### 1. Ошибка подключения к базе данных
```bash
# Проверьте статус MySQL сервера
mysql -u root -p -e "SELECT 1"

# Проверьте настройки подключения
cat src/main/resources/application-{profile}.properties
```

#### 2. Недостаточно соединений в пуле
```properties
# Увеличьте размер пула
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=10
```

#### 3. Медленные запросы
```properties
# Увеличьте таймауты
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
```

### Логирование
```properties
# Включите детальное логирование
logging.level.ru.anapa.autorent.service.DatabaseHealthService=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

## 📈 Производительность

### Оптимизация для разных окружений

#### Локальная разработка
- Минимальный размер пула (5 соединений)
- Включенное логирование SQL
- Автоматическое обновление схемы

#### Продакшн
- Оптимальный размер пула (10-20 соединений)
- Отключенное логирование SQL
- Валидация схемы
- Кэширование Thymeleaf

#### Тестирование
- Создание/удаление схемы при запуске
- Изолированная база данных
- Быстрые таймауты

## 🔄 Миграции

### Создание новой базы данных
```sql
-- Создание базы данных
CREATE DATABASE autorent CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Создание пользователя
CREATE USER 'autorent_user'@'%' IDENTIFIED BY 'secure_password';

-- Предоставление прав
GRANT ALL PRIVILEGES ON autorent.* TO 'autorent_user'@'%';
FLUSH PRIVILEGES;
```

### Обновление схемы
```bash
# Автоматическое обновление (dev)
spring.jpa.hibernate.ddl-auto=update

# Валидация схемы (prod)
spring.jpa.hibernate.ddl-auto=validate

# Создание схемы (test)
spring.jpa.hibernate.ddl-auto=create-drop
```

## 📞 Поддержка

При возникновении проблем с MSP серверами:

1. Проверьте логи приложения
2. Используйте API мониторинга
3. Проверьте сетевую доступность серверов
4. Обратитесь к администратору базы данных

---

**Важно**: Всегда используйте соответствующий профиль для каждого окружения и не смешивайте конфигурации между серверами. 