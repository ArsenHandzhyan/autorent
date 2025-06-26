-- Проверка тестовых платежей в базе данных
-- Выполните эти запросы в MySQL для проверки корректности тестовых данных

-- 1. Общая статистика платежей
SELECT 
    'ОБЩАЯ СТАТИСТИКА ПЛАТЕЖЕЙ' as info;

SELECT 
    status,
    COUNT(*) as count,
    SUM(amount) as total_amount,
    MIN(payment_date) as min_date,
    MAX(payment_date) as max_date
FROM daily_payments 
GROUP BY status
ORDER BY status;

-- 2. Проверка количества платежей (должно быть 9)
SELECT 
    'ПРОВЕРКА КОЛИЧЕСТВА ПЛАТЕЖЕЙ' as info;

SELECT 
    COUNT(*) as total_payments,
    CASE 
        WHEN COUNT(*) = 9 THEN '✅ КОРРЕКТНО - 9 платежей'
        ELSE CONCAT('❌ ОШИБКА - ', COUNT(*), ' платежей вместо 9')
    END as status
FROM daily_payments;

-- 3. Детальная информация о всех платежах
SELECT 
    'ДЕТАЛЬНАЯ ИНФОРМАЦИЯ О ПЛАТЕЖАХ' as info;

SELECT 
    id,
    payment_date,
    amount,
    status,
    created_at,
    processed_at,
    CASE 
        WHEN notes IS NULL THEN 'Нет примечаний'
        ELSE LEFT(notes, 50)
    END as notes_preview
FROM daily_payments 
ORDER BY payment_date DESC, id DESC;

-- 4. Проверка дат платежей (должны быть за последние 7 дней)
SELECT 
    'ПРОВЕРКА ДАТ ПЛАТЕЖЕЙ' as info;

SELECT 
    payment_date,
    COUNT(*) as count,
    GROUP_CONCAT(status) as statuses,
    CASE 
        WHEN payment_date >= CURDATE() - INTERVAL 7 DAY AND payment_date <= CURDATE() + INTERVAL 2 DAY 
        THEN '✅ КОРРЕКТНАЯ ДАТА'
        ELSE '❌ НЕКОРРЕКТНАЯ ДАТА'
    END as date_status
FROM daily_payments 
GROUP BY payment_date
ORDER BY payment_date DESC;

-- 5. Проверка статусов платежей
SELECT 
    'ПРОВЕРКА СТАТУСОВ ПЛАТЕЖЕЙ' as info;

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

-- 6. Проверка сумм платежей (должны быть 1500.00)
SELECT 
    'ПРОВЕРКА СУММ ПЛАТЕЖЕЙ' as info;

SELECT 
    amount,
    COUNT(*) as count,
    CASE 
        WHEN amount = 1500.00 THEN '✅ КОРРЕКТНАЯ СУММА'
        ELSE '❌ НЕКОРРЕКТНАЯ СУММА'
    END as amount_status
FROM daily_payments 
GROUP BY amount
ORDER BY amount;

-- 7. Проверка примечаний к платежам
SELECT 
    'ПРОВЕРКА ПРИМЕЧАНИЙ К ПЛАТЕЖАМ' as info;

SELECT 
    id,
    payment_date,
    status,
    CASE 
        WHEN notes IS NULL AND status = 'PENDING' THEN '✅ КОРРЕКТНО - PENDING без примечаний'
        WHEN notes IS NOT NULL AND status = 'PROCESSED' THEN '✅ КОРРЕКТНО - PROCESSED с примечаниями'
        WHEN notes IS NOT NULL AND status = 'FAILED' THEN '✅ КОРРЕКТНО - FAILED с примечаниями'
        WHEN notes IS NULL AND status IN ('PROCESSED', 'FAILED') THEN '❌ ОШИБКА - должен быть notes'
        ELSE '⚠️ ПРОВЕРИТЬ ВРУЧНУЮ'
    END as notes_status,
    notes
FROM daily_payments 
ORDER BY payment_date DESC;

-- 8. Проверка временных меток
SELECT 
    'ПРОВЕРКА ВРЕМЕННЫХ МЕТОК' as info;

SELECT 
    id,
    payment_date,
    status,
    created_at,
    processed_at,
    CASE 
        WHEN created_at IS NOT NULL THEN '✅ created_at заполнено'
        ELSE '❌ created_at пустое'
    END as created_at_status,
    CASE 
        WHEN processed_at IS NOT NULL AND status IN ('PROCESSED', 'FAILED') THEN '✅ processed_at корректно'
        WHEN processed_at IS NULL AND status = 'PENDING' THEN '✅ processed_at корректно пустое'
        ELSE '❌ processed_at некорректно'
    END as processed_at_status
FROM daily_payments 
ORDER BY payment_date DESC;

-- 9. Итоговая проверка
SELECT 
    'ИТОГОВАЯ ПРОВЕРКА ТЕСТОВЫХ ДАННЫХ' as info;

SELECT 
    CASE 
        WHEN COUNT(*) = 9 THEN '✅ Количество платежей корректно'
        ELSE CONCAT('❌ Количество платежей: ', COUNT(*), ' (должно быть 9)')
    END as count_check,
    CASE 
        WHEN COUNT(CASE WHEN status = 'PROCESSED' THEN 1 END) >= 6 THEN '✅ PROCESSED платежей достаточно'
        ELSE '❌ Недостаточно PROCESSED платежей'
    END as processed_check,
    CASE 
        WHEN COUNT(CASE WHEN status = 'PENDING' THEN 1 END) = 2 THEN '✅ PENDING платежей корректно'
        ELSE '❌ Неверное количество PENDING платежей'
    END as pending_check,
    CASE 
        WHEN COUNT(CASE WHEN status = 'FAILED' THEN 1 END) = 1 THEN '✅ FAILED платежей корректно'
        ELSE '❌ Неверное количество FAILED платежей'
    END as failed_check,
    CASE 
        WHEN COUNT(CASE WHEN amount = 1500.00 THEN 1 END) = 9 THEN '✅ Все суммы корректны'
        ELSE '❌ Не все суммы равны 1500.00'
    END as amount_check
FROM daily_payments; 