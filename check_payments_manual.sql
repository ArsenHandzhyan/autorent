-- Ручная проверка платежей в базе данных AutoRent
-- Выполнить в MySQL Workbench или через командную строку

-- 1. Проверка платежей для аренды 54
SELECT 
    id,
    rental_id,
    payment_date,
    amount,
    status,
    notes
FROM daily_payments 
WHERE rental_id = 54 
ORDER BY payment_date;

-- 2. Проверка всех платежей с поврежденной кодировкой
SELECT 
    id,
    rental_id,
    payment_date,
    amount,
    status,
    notes
FROM daily_payments 
WHERE notes LIKE '%ÐŸÐ»Ð°Ñ‚ÐµÐ¶%'
ORDER BY rental_id, payment_date;

-- 3. Исправление платежей для аренды 54 (если нужно)
UPDATE daily_payments 
SET notes = 'Платеж за день аренды. Средства списаны.' 
WHERE rental_id = 54 AND notes LIKE '%ÐŸÐ»Ð°Ñ‚ÐµÐ¶%';

-- 4. Исправление всех поврежденных платежей (если нужно)
UPDATE daily_payments 
SET notes = 'Платеж за день аренды. Средства списаны.' 
WHERE notes LIKE '%ÐŸÐ»Ð°Ñ‚ÐµÐ¶%';

-- 5. Проверка результата
SELECT 
    id,
    rental_id,
    payment_date,
    amount,
    status,
    notes
FROM daily_payments 
WHERE rental_id = 54 
ORDER BY payment_date; 