-- Проверка платежей в базе данных AutoRent

-- 1. Общая статистика платежей
SELECT 
    COUNT(*) as total_payments,
    SUM(amount) as total_amount,
    status,
    COUNT(*) as count_by_status
FROM daily_payments 
GROUP BY status;

-- 2. Платежи для аренды 54
SELECT 
    id,
    rental_id,
    payment_date,
    amount,
    status,
    notes,
    created_at,
    processed_at
FROM daily_payments 
WHERE rental_id = 54 
ORDER BY payment_date;

-- 3. Платежи с поврежденной кодировкой
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

-- 4. Проверка кодировки для аренды 54
SELECT 
    id,
    notes,
    HEX(notes) as notes_hex
FROM daily_payments 
WHERE rental_id = 54; 