# 🔧 Инструкция по исправлению поврежденных данных в облачной базе AutoRent

## 📋 Проблема
В облачной базе данных AWS RDS обнаружены платежи с поврежденными примечаниями, отображающимися как кракозябры:
- `ÐŸÐ»Ð°Ñ‚ÐµÐ¶ Ð·Ð° Ð´ÐµÐ½ÑŒ Ð°Ñ€ÐµÐ½Ð´Ñ‹` вместо "Платеж за день аренды"
- `Ð¡Ñ€ÐµÐ´ÑÑ‚Ð²Ð° ÑÐ¿Ð¸ÑÐ°Ð½Ñ‹` вместо "Средства списаны"

## 🎯 Решение

### 1. Автоматическое исправление через API
После запуска приложения используйте API endpoint:

```bash
# Исправление поврежденных примечаний
curl -X POST http://localhost:8080/api/data-fix/fix-payment-notes \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"

# Проверка статистики
curl -X GET http://localhost:8080/api/data-fix/payment-statistics \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Проверка состояния БД
curl -X GET http://localhost:8080/api/data-fix/health-check
```

### 2. Ручное исправление через SQL
Если API недоступен, используйте SQL-скрипт:

```bash
# Запуск batch-скрипта
./fix_payments.bat

# Или прямой вызов MySQL
docker run --rm mysql:8.0 mysql \
  -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com \
  -P 3306 \
  -u wm02va8ppexvexe1 \
  -psrj7xmugajaa2ww3 \
  -D cmwz7gjxubq6sk64 \
  < fix_payment_notes.sql
```

### 3. SQL-скрипт для исправления
Файл `fix_payment_notes.sql` содержит:

```sql
-- Исправление поврежденных примечаний платежей
UPDATE daily_payments 
SET notes = 'Платеж за день аренды. Средства списаны.'
WHERE notes LIKE '%ÐŸÐ»Ð°Ñ‚ÐµÐ¶%' 
   OR notes LIKE '%Ð·Ð° Ð´ÐµÐ½ÑŒ%'
   OR notes LIKE '%Ð°Ñ€ÐµÐ½Ð´Ñ‹%'
   OR notes LIKE '%Ð¡Ñ€ÐµÐ´ÑÑ‚Ð²Ð°%'
   OR notes LIKE '%ÑÐ¿Ð¸ÑÐ°Ð½Ñ‹%';

-- Проверка результата
SELECT id, amount, notes, status, rental_id 
FROM daily_payments 
WHERE notes IS NOT NULL 
ORDER BY id DESC 
LIMIT 10;
```

## 🛡️ Защита от превышения лимитов запросов

### Настройки в application-dev.properties:
```properties
# Оптимизированный пул соединений
spring.datasource.hikari.maximum-pool-size=3
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.validation-timeout=5000

# Spring Retry для автоматических повторных попыток
# Настроено в PaymentService с аннотациями @Retryable
```

### Логика повторных попыток:
- **Максимум попыток**: 3
- **Задержка**: 1 секунда с экспоненциальным увеличением
- **Максимальная задержка**: 10 секунд
- **Обрабатываемые исключения**: SQLException, DataAccessException

## 📊 Мониторинг

### Проверка состояния:
```bash
# Статистика платежей
curl -X GET http://localhost:8080/api/data-fix/payment-statistics

# Ответ:
{
  "success": true,
  "statistics": {
    "totalPayments": 28,
    "processedPayments": 18,
    "pendingPayments": 10,
    "paymentsWithNotes": 18
  }
}
```

### Логи приложения:
```bash
# Просмотр логов backend
docker logs autorent-backend-1 --tail 50

# Фильтрация по ошибкам
docker logs autorent-backend-1 | grep -i "error\|exception"
```

## ⚠️ Важные замечания

### Лимиты AWS RDS:
- **3600 запросов в час** на пользователя
- **Сброс лимита**: через 1 час после превышения
- **Рекомендация**: использовать локальную базу для тестирования

### Профили приложения:
- **local**: Локальная база данных (localhost:3306)
- **dev**: Облачная база данных (AWS RDS)
- **Переключение**: через переменную `spring.profiles.active`

### Кодировка:
- **Исправлено**: использование UTF-8 вместо utf8mb4
- **Настройки**: в application-local.properties и application-dev.properties
- **HikariCP**: SET NAMES utf8 COLLATE utf8_unicode_ci

## 🔄 Процесс исправления

1. **Запуск приложения** с профилем `local` или `dev`
2. **Проверка состояния** через `/api/data-fix/health-check`
3. **Исправление данных** через `/api/data-fix/fix-payment-notes`
4. **Проверка результата** через `/api/data-fix/payment-statistics`
5. **Мониторинг логов** на предмет ошибок

## 📞 Поддержка

При возникновении проблем:
1. Проверьте логи приложения
2. Убедитесь в доступности базы данных
3. Проверьте настройки кодировки
4. При превышении лимитов - подождите 1 час или используйте локальную базу

---

**Автор**: AutoRent Development Team  
**Дата**: 29.06.2025  
**Версия**: 1.0 