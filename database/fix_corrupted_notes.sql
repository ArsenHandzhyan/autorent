USE cmwz7gjxubq6sk64;

-- Исправление кракозябр в примечаниях для записей с ID 1 и 2
UPDATE daily_payments 
SET notes = 'Платеж за день аренды. Средства списаны.' 
WHERE id IN (1, 2);

-- Проверка результата
SELECT id, rental_id, notes 
FROM daily_payments 
WHERE id IN (1, 2); 