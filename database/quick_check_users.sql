-- =====================================================
-- БЫСТРАЯ ПРОВЕРКА ПОЛЬЗОВАТЕЛЕЙ В ОБЛАЧНОЙ БД
-- =====================================================

-- 1. Общая статистика
SELECT 
    'ОБЩАЯ СТАТИСТИКА' as info,
    COUNT(*) as total_users,
    COUNT(CASE WHEN enabled = 1 THEN 1 END) as active_users,
    COUNT(CASE WHEN enabled = 0 THEN 1 END) as disabled_users
FROM users;

-- 2. Последние 10 зарегистрированных пользователей
SELECT 
    'ПОСЛЕДНИЕ ПОЛЬЗОВАТЕЛИ' as info,
    id,
    email,
    first_name,
    last_name,
    phone,
    enabled,
    registration_date
FROM users 
ORDER BY registration_date DESC 
LIMIT 10;

-- 3. Администраторы
SELECT 
    'АДМИНИСТРАТОРЫ' as info,
    u.id,
    u.email,
    u.first_name,
    u.last_name,
    u.phone,
    u.enabled
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE r.name = 'ROLE_ADMIN'
ORDER BY u.registration_date;

-- 4. Пользователи без аккаунтов
SELECT 
    'ПОЛЬЗОВАТЕЛИ БЕЗ АККАУНТОВ' as info,
    u.id,
    u.email,
    u.first_name,
    u.last_name
FROM users u
LEFT JOIN accounts a ON u.id = a.user_id
WHERE a.id IS NULL;

-- 5. Проблемные записи
SELECT 
    'ПРОБЛЕМНЫЕ ЗАПИСИ' as info,
    id,
    email,
    first_name,
    last_name,
    phone,
    CASE 
        WHEN email NOT LIKE '%@%' OR email = '' THEN 'Некорректный email'
        WHEN first_name IS NULL OR first_name = '' THEN 'Отсутствует имя'
        WHEN last_name IS NULL OR last_name = '' THEN 'Отсутствует фамилия'
        WHEN phone IS NULL OR phone = '' THEN 'Отсутствует телефон'
        ELSE 'OK'
    END as problem
FROM users
WHERE email NOT LIKE '%@%' OR email = '' 
    OR first_name IS NULL OR first_name = '' 
    OR last_name IS NULL OR last_name = ''
    OR phone IS NULL OR phone = ''; 