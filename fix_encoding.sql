-- Скрипт для исправления кодировки примечаний платежей
-- Выполняется в облачной базе данных AutoRent

-- 1. Проверяем текущие данные
SELECT id, notes FROM daily_payments WHERE rental_id = 54;

-- 2. Исправляем кодировку для аренды 54
UPDATE daily_payments 
SET notes = 'Платеж за день аренды. Средства списаны.' 
WHERE rental_id = 54 AND notes LIKE '%ÐŸÐ»Ð°Ñ‚ÐµÐ¶%';

-- 3. Исправляем кодировку для всех поврежденных записей
UPDATE daily_payments 
SET notes = 'Платеж за день аренды. Средства списаны.' 
WHERE notes LIKE '%ÐŸÐ»Ð°Ñ‚ÐµÐ¶%';

-- 4. Проверяем результат
SELECT id, notes FROM daily_payments WHERE rental_id = 54;

-- 5. Проверяем общее количество исправленных записей
SELECT COUNT(*) as fixed_records FROM daily_payments 
WHERE notes = 'Платеж за день аренды. Средства списаны.'; 