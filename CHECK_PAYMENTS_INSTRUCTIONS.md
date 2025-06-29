# Инструкция по проверке платежей в базе данных

## 🔍 Способы проверки платежей

### Способ 1: Через MySQL Workbench (рекомендуется)

1. **Подключитесь к базе данных:**
   - Host: `uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com`
   - Port: `3306`
   - Database: `cmwz7gjxubq6sk64`
   - Username: `wm02va8ppexvexe1`
   - Password: `srj7xmugajaa2ww3`

2. **Выполните запросы из файла `check_payments_manual.sql`**

### Способ 2: Через командную строку

```bash
# Подключение к базе данных
docker run --rm mysql:8.0 mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64

# Затем выполните запросы:
SELECT id, rental_id, payment_date, amount, status, notes FROM daily_payments WHERE rental_id = 54 ORDER BY payment_date;
```

### Способ 3: Через веб-интерфейс

1. **Дождитесь запуска приложения** (2-3 минуты после `docker-compose up -d`)
2. **Откройте браузер:** http://localhost:8080
3. **Войдите как администратор:**
   - Email: `admin61@example.com`
   - Пароль: `admin123`
4. **Перейдите:** Панель управления → Ежедневные платежи → Платежи по аренде #54

## 📊 Что проверять

### 1. Текущее состояние платежей аренды 54
```sql
SELECT id, rental_id, payment_date, amount, status, notes 
FROM daily_payments 
WHERE rental_id = 54 
ORDER BY payment_date;
```

### 2. Платежи с поврежденной кодировкой
```sql
SELECT id, rental_id, payment_date, amount, status, notes 
FROM daily_payments 
WHERE notes LIKE '%ÐŸÐ»Ð°Ñ‚ÐµÐ¶%'
ORDER BY rental_id, payment_date;
```

### 3. Общая статистика
```sql
SELECT 
    COUNT(*) as total_payments,
    SUM(amount) as total_amount,
    status,
    COUNT(*) as count_by_status
FROM daily_payments 
GROUP BY status;
```

## 🔧 Исправление данных (если нужно)

### Исправление платежей аренды 54
```sql
UPDATE daily_payments 
SET notes = 'Платеж за день аренды. Средства списаны.' 
WHERE rental_id = 54 AND notes LIKE '%ÐŸÐ»Ð°Ñ‚ÐµÐ¶%';
```

### Исправление всех поврежденных платежей
```sql
UPDATE daily_payments 
SET notes = 'Платеж за день аренды. Средства списаны.' 
WHERE notes LIKE '%ÐŸÐ»Ð°Ñ‚ÐµÐ¶%';
```

## 🎯 Ожидаемые результаты

### До исправления:
- Примечания: `ÐŸÐ»Ð°Ñ‚ÐµÐ¶ Ð·Ð° Ð´ÐµÐ½ÑŒ Ð°Ñ€ÐµÐ½Ð´Ñ‹. Ð¡Ñ€ÐµÐ´ÑÑ‚Ð²Ð° ÑÐ¿Ð¸ÑÐ°Ð½Ñ‹.`

### После исправления:
- Примечания: `Платеж за день аренды. Средства списаны.`

## ⚠️ Важные замечания

1. **Лимит запросов:** База данных имеет лимит 3600 запросов в час
2. **Ожидание:** Если получаете ошибку лимита, подождите 1 час
3. **Кэш браузера:** После исправления данных очистите кэш браузера (Ctrl+Shift+R)

## 📞 Если нужна помощь

1. Проверьте логи приложения: `docker logs autorent-backend-1`
2. Убедитесь, что приложение запущено: `docker-compose ps`
3. Перезапустите приложение: `docker-compose down && docker-compose up -d` 