USE cmwz7gjxubq6sk64;

-- Проверка всех примечаний
SELECT id, rental_id, LEFT(notes, 40) as notes_preview 
FROM daily_payments 
WHERE notes IS NOT NULL 
ORDER BY id; 