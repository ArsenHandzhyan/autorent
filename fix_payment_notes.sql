-- Исправление поврежденных примечаний платежей
-- Заменяем кракозябры на корректные русские тексты

UPDATE daily_payments 
SET notes = 'Платеж за день аренды. Средства списаны.'
WHERE notes LIKE '%ÐŸÐ»Ð°Ñ‚ÐµÐ¶%' 
   OR notes LIKE '%Ð·Ð°%' 
   OR notes LIKE '%Ð°Ñ€ÐµÐ½Ð´Ñ‹%'
   OR notes LIKE '%Ð¡Ñ€ÐµÐ´ÑÑ‚Ð²Ð°%'
   OR notes LIKE '%ÑÐ¿Ð¸ÑÐ°Ð½Ñ‹%';

-- Проверяем результат
SELECT 
    id,
    rental_id,
    amount,
    payment_date,
    status,
    notes,
    created_at,
    processed_at
FROM daily_payments 
WHERE notes LIKE '%Платеж за день аренды%'
ORDER BY payment_date DESC;

-- Общая статистика
SELECT 
    COUNT(*) as total_payments,
    COUNT(CASE WHEN notes LIKE '%Платеж за день аренды%' THEN 1 END) as fixed_payments,
    COUNT(CASE WHEN notes LIKE '%ÐŸÐ»Ð°Ñ‚ÐµÐ¶%' THEN 1 END) as corrupted_payments
FROM daily_payments; 