USE cmwz7gjxubq6sk64;

-- Проверка всех примечаний в таблице daily_payments
SELECT 
    id, 
    rental_id, 
    amount, 
    status, 
    notes, 
    created_at,
    LENGTH(notes) as notes_length,
    HEX(notes) as notes_hex
FROM daily_payments 
WHERE notes IS NOT NULL AND notes != '' 
ORDER BY id;

-- Статистика по примечаниям
SELECT 
    COUNT(*) as total_payments,
    COUNT(notes) as payments_with_notes,
    COUNT(CASE WHEN notes = '' THEN 1 END) as empty_notes,
    COUNT(CASE WHEN notes IS NULL THEN 1 END) as null_notes
FROM daily_payments;

-- Проверка на наличие кракозябр (некорректных символов)
SELECT 
    id, 
    rental_id, 
    notes,
    HEX(notes) as notes_hex
FROM daily_payments 
WHERE notes IS NOT NULL 
  AND notes != '' 
  AND (notes LIKE '%%' OR notes LIKE '%?%' OR notes LIKE '%%')
ORDER BY id; 