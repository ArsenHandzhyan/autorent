-- Скрипт для очистки и создания новых тестовых данных платежей
-- Выполняется для исправления некорректных дат и создания правильных тестовых данных

-- 1. Очистка всех существующих платежей
DELETE FROM daily_payments;

-- 2. Сброс автоинкремента
ALTER TABLE daily_payments AUTO_INCREMENT = 1;

-- 3. Получение информации о существующих арендах и счетах
-- (этот скрипт предполагает, что у нас есть активные аренды и счета)

-- 4. Создание новых тестовых платежей с правильными датами
-- Платежи за последние 7 дней для тестирования

-- Платеж 1: Вчера (обработанный)
INSERT INTO daily_payments (rental_id, account_id, payment_date, amount, status, created_at, processed_at, notes) 
SELECT 
    r.id as rental_id,
    a.id as account_id,
    DATE_SUB(CURDATE(), INTERVAL 1 DAY) as payment_date,
    1500.00 as amount,
    'PROCESSED' as status,
    DATE_SUB(NOW(), INTERVAL 1 DAY) as created_at,
    DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 1 HOUR as processed_at,
    'Платеж успешно обработан' as notes
FROM rentals r
JOIN accounts a ON a.user_id = r.user_id
WHERE r.status = 'ACTIVE' 
LIMIT 1;

-- Платеж 2: Сегодня (обработанный)
INSERT INTO daily_payments (rental_id, account_id, payment_date, amount, status, created_at, processed_at, notes) 
SELECT 
    r.id as rental_id,
    a.id as account_id,
    CURDATE() as payment_date,
    1500.00 as amount,
    'PROCESSED' as status,
    NOW() - INTERVAL 2 HOUR as created_at,
    NOW() - INTERVAL 1 HOUR as processed_at,
    'Платеж успешно обработан' as notes
FROM rentals r
JOIN accounts a ON a.user_id = r.user_id
WHERE r.status = 'ACTIVE' 
LIMIT 1;

-- Платеж 3: Завтра (ожидающий обработки)
INSERT INTO daily_payments (rental_id, account_id, payment_date, amount, status, created_at, processed_at, notes) 
SELECT 
    r.id as rental_id,
    a.id as account_id,
    DATE_ADD(CURDATE(), INTERVAL 1 DAY) as payment_date,
    1500.00 as amount,
    'PENDING' as status,
    NOW() as created_at,
    NULL as processed_at,
    NULL as notes
FROM rentals r
JOIN accounts a ON a.user_id = r.user_id
WHERE r.status = 'ACTIVE' 
LIMIT 1;

-- Платеж 4: Послезавтра (ожидающий обработки)
INSERT INTO daily_payments (rental_id, account_id, payment_date, amount, status, created_at, processed_at, notes) 
SELECT 
    r.id as rental_id,
    a.id as account_id,
    DATE_ADD(CURDATE(), INTERVAL 2 DAY) as payment_date,
    1500.00 as amount,
    'PENDING' as status,
    NOW() as created_at,
    NULL as processed_at,
    NULL as notes
FROM rentals r
JOIN accounts a ON a.user_id = r.user_id
WHERE r.status = 'ACTIVE' 
LIMIT 1;

-- Платеж 5: 3 дня назад (обработанный с предупреждением о превышении лимита)
INSERT INTO daily_payments (rental_id, account_id, payment_date, amount, status, created_at, processed_at, notes) 
SELECT 
    r.id as rental_id,
    a.id as account_id,
    DATE_SUB(CURDATE(), INTERVAL 3 DAY) as payment_date,
    1500.00 as amount,
    'PROCESSED' as status,
    DATE_SUB(NOW(), INTERVAL 3 DAY) as created_at,
    DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 30 MINUTE as processed_at,
    'Платеж обработан с предупреждением: превышен кредитный лимит' as notes
FROM rentals r
JOIN accounts a ON a.user_id = r.user_id
WHERE r.status = 'ACTIVE' 
LIMIT 1;

-- Платеж 6: 4 дня назад (обработанный с предупреждением о недостатке средств)
INSERT INTO daily_payments (rental_id, account_id, payment_date, amount, status, created_at, processed_at, notes) 
SELECT 
    r.id as rental_id,
    a.id as account_id,
    DATE_SUB(CURDATE(), INTERVAL 4 DAY) as payment_date,
    1500.00 as amount,
    'PROCESSED' as status,
    DATE_SUB(NOW(), INTERVAL 4 DAY) as created_at,
    DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 45 MINUTE as processed_at,
    'Платеж обработан с предупреждением: недостаточно средств на счете' as notes
FROM rentals r
JOIN accounts a ON a.user_id = r.user_id
WHERE r.status = 'ACTIVE' 
LIMIT 1;

-- Платеж 7: 5 дней назад (техническая ошибка - FAILED)
INSERT INTO daily_payments (rental_id, account_id, payment_date, amount, status, created_at, processed_at, notes) 
SELECT 
    r.id as rental_id,
    a.id as account_id,
    DATE_SUB(CURDATE(), INTERVAL 5 DAY) as payment_date,
    1500.00 as amount,
    'FAILED' as status,
    DATE_SUB(NOW(), INTERVAL 5 DAY) as created_at,
    DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 1 HOUR as processed_at,
    'Техническая ошибка при списании: временная недоступность системы' as notes
FROM rentals r
JOIN accounts a ON a.user_id = r.user_id
WHERE r.status = 'ACTIVE' 
LIMIT 1;

-- Платеж 8: 6 дней назад (обработанный)
INSERT INTO daily_payments (rental_id, account_id, payment_date, amount, status, created_at, processed_at, notes) 
SELECT 
    r.id as rental_id,
    a.id as account_id,
    DATE_SUB(CURDATE(), INTERVAL 6 DAY) as payment_date,
    1500.00 as amount,
    'PROCESSED' as status,
    DATE_SUB(NOW(), INTERVAL 6 DAY) as created_at,
    DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 15 MINUTE as processed_at,
    'Платеж успешно обработан' as notes
FROM rentals r
JOIN accounts a ON a.user_id = r.user_id
WHERE r.status = 'ACTIVE' 
LIMIT 1;

-- Платеж 9: 7 дней назад (обработанный)
INSERT INTO daily_payments (rental_id, account_id, payment_date, amount, status, created_at, processed_at, notes) 
SELECT 
    r.id as rental_id,
    a.id as account_id,
    DATE_SUB(CURDATE(), INTERVAL 7 DAY) as payment_date,
    1500.00 as amount,
    'PROCESSED' as status,
    DATE_SUB(NOW(), INTERVAL 7 DAY) as created_at,
    DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 20 MINUTE as processed_at,
    'Платеж успешно обработан' as notes
FROM rentals r
JOIN accounts a ON a.user_id = r.user_id
WHERE r.status = 'ACTIVE' 
LIMIT 1;

-- 5. Проверка созданных данных
SELECT 
    'Статистика платежей после очистки и создания новых данных:' as info;

SELECT 
    status,
    COUNT(*) as count,
    SUM(amount) as total_amount
FROM daily_payments 
GROUP BY status;

SELECT 
    'Детальная информация о платежах:' as info;

SELECT 
    id,
    payment_date,
    amount,
    status,
    created_at,
    processed_at,
    LEFT(notes, 50) as notes_preview
FROM daily_payments 
ORDER BY payment_date DESC, id DESC; 