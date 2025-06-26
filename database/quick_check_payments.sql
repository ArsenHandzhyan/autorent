-- Быстрая проверка тестовых платежей
-- Выполните этот запрос для проверки корректности данных

SELECT 
    '=== БЫСТРАЯ ПРОВЕРКА ТЕСТОВЫХ ПЛАТЕЖЕЙ ===' as info;

-- Количество платежей
SELECT 
    COUNT(*) as total_payments,
    CASE WHEN COUNT(*) = 9 THEN '✅ КОРРЕКТНО' ELSE '❌ ОШИБКА' END as count_status
FROM daily_payments;

-- Статистика по статусам
SELECT 
    status,
    COUNT(*) as count,
    CASE 
        WHEN status = 'PROCESSED' AND COUNT(*) >= 6 THEN '✅ КОРРЕКТНО'
        WHEN status = 'PENDING' AND COUNT(*) = 2 THEN '✅ КОРРЕКТНО'
        WHEN status = 'FAILED' AND COUNT(*) = 1 THEN '✅ КОРРЕКТНО'
        ELSE '❌ НЕКОРРЕКТНО'
    END as status_check
FROM daily_payments 
GROUP BY status
ORDER BY status;

-- Проверка сумм
SELECT 
    amount,
    COUNT(*) as count,
    CASE WHEN amount = 1500.00 THEN '✅ КОРРЕКТНО' ELSE '❌ ОШИБКА' END as amount_status
FROM daily_payments 
GROUP BY amount;

-- Последние платежи
SELECT 
    '=== ПОСЛЕДНИЕ ПЛАТЕЖИ ===' as info;

SELECT 
    id,
    DATE_FORMAT(payment_date, '%d.%m.%Y') as payment_date,
    amount,
    status,
    CASE 
        WHEN notes IS NULL THEN 'Нет примечаний'
        ELSE LEFT(notes, 30)
    END as notes_preview
FROM daily_payments 
ORDER BY payment_date DESC, id DESC
LIMIT 10; 