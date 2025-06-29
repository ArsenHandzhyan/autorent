-- =====================================================
-- СКРИПТ ДЛЯ ПРОВЕРКИ ПОЛЬЗОВАТЕЛЕЙ В ОБЛАЧНОЙ БД AUTORENT
-- =====================================================

-- Подключение к облачной базе данных:
-- Host: uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com
-- Port: 3306
-- Database: cmwz7gjxubq6sk64
-- Username: wm02va8ppexvexe1
-- Password: srj7xmugajaa2ww3

-- =====================================================
-- 1. ОБЩАЯ СТАТИСТИКА ПОЛЬЗОВАТЕЛЕЙ
-- =====================================================

-- Общее количество пользователей
SELECT 
    COUNT(*) as total_users,
    COUNT(CASE WHEN enabled = 1 THEN 1 END) as active_users,
    COUNT(CASE WHEN enabled = 0 THEN 1 END) as disabled_users
FROM users;

-- =====================================================
-- 2. СПИСОК ВСЕХ ПОЛЬЗОВАТЕЛЕЙ С ОСНОВНОЙ ИНФОРМАЦИЕЙ
-- =====================================================

SELECT 
    u.id,
    u.email,
    u.first_name,
    u.last_name,
    u.phone,
    u.enabled,
    u.registration_date,
    u.last_login_date,
    GROUP_CONCAT(r.name) as roles
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
GROUP BY u.id, u.email, u.first_name, u.last_name, u.phone, u.enabled, u.registration_date, u.last_login_date
ORDER BY u.registration_date DESC;

-- =====================================================
-- 3. ПОЛЬЗОВАТЕЛИ ПО РОЛЯМ
-- =====================================================

-- Пользователи с ролью ADMIN
SELECT 
    u.id,
    u.email,
    u.first_name,
    u.last_name,
    u.phone,
    u.enabled,
    u.registration_date
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE r.name = 'ROLE_ADMIN'
ORDER BY u.registration_date;

-- Пользователи с ролью USER
SELECT 
    u.id,
    u.email,
    u.first_name,
    u.last_name,
    u.phone,
    u.enabled,
    u.registration_date
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE r.name = 'ROLE_USER'
ORDER BY u.registration_date;

-- =====================================================
-- 4. АНАЛИЗ АКТИВНОСТИ ПОЛЬЗОВАТЕЛЕЙ
-- =====================================================

-- Пользователи, которые не заходили более 30 дней
SELECT 
    u.id,
    u.email,
    u.first_name,
    u.last_name,
    u.last_login_date,
    DATEDIFF(NOW(), u.last_login_date) as days_since_last_login
FROM users u
WHERE u.last_login_date IS NOT NULL 
    AND u.last_login_date < DATE_SUB(NOW(), INTERVAL 30 DAY)
    AND u.enabled = 1
ORDER BY u.last_login_date;

-- Пользователи, зарегистрированные за последние 30 дней
SELECT 
    u.id,
    u.email,
    u.first_name,
    u.last_name,
    u.registration_date,
    DATEDIFF(NOW(), u.registration_date) as days_since_registration
FROM users u
WHERE u.registration_date >= DATE_SUB(NOW(), INTERVAL 30 DAY)
ORDER BY u.registration_date DESC;

-- =====================================================
-- 5. ПРОВЕРКА ДАННЫХ ПОЛЬЗОВАТЕЛЕЙ
-- =====================================================

-- Пользователи с некорректными email (без @)
SELECT 
    id,
    email,
    first_name,
    last_name
FROM users
WHERE email NOT LIKE '%@%' OR email = '';

-- Пользователи без имени или фамилии
SELECT 
    id,
    email,
    first_name,
    last_name
FROM users
WHERE first_name IS NULL OR first_name = '' 
    OR last_name IS NULL OR last_name = '';

-- Пользователи без телефона
SELECT 
    id,
    email,
    first_name,
    last_name,
    phone
FROM users
WHERE phone IS NULL OR phone = '';

-- =====================================================
-- 6. ПРОВЕРКА СВЯЗЕЙ С ДРУГИМИ ТАБЛИЦАМИ
-- =====================================================

-- Пользователи с аккаунтами
SELECT 
    u.id,
    u.email,
    u.first_name,
    u.last_name,
    a.balance,
    a.credit_limit,
    a.allow_negative_balance
FROM users u
LEFT JOIN accounts a ON u.id = a.user_id
ORDER BY u.id;

-- Пользователи с арендами
SELECT 
    u.id,
    u.email,
    u.first_name,
    u.last_name,
    COUNT(r.id) as rental_count
FROM users u
LEFT JOIN rentals r ON u.id = r.user_id
GROUP BY u.id, u.email, u.first_name, u.last_name
ORDER BY rental_count DESC;

-- =====================================================
-- 7. СТАТИСТИКА ПО ДАТАМ РЕГИСТРАЦИИ
-- =====================================================

-- Регистрации по месяцам
SELECT 
    DATE_FORMAT(registration_date, '%Y-%m') as month,
    COUNT(*) as new_users
FROM users
WHERE registration_date IS NOT NULL
GROUP BY DATE_FORMAT(registration_date, '%Y-%m')
ORDER BY month DESC;

-- =====================================================
-- 8. ПРОВЕРКА ДУБЛИКАТОВ
-- =====================================================

-- Дубликаты email (не должно быть)
SELECT 
    email,
    COUNT(*) as count
FROM users
GROUP BY email
HAVING COUNT(*) > 1;

-- Дубликаты телефонов (не должно быть)
SELECT 
    phone,
    COUNT(*) as count
FROM users
WHERE phone IS NOT NULL AND phone != ''
GROUP BY phone
HAVING COUNT(*) > 1;

-- =====================================================
-- 9. ПРОВЕРКА СТРУКТУРЫ ТАБЛИЦ
-- =====================================================

-- Структура таблицы users
DESCRIBE users;

-- Структура таблицы roles
DESCRIBE roles;

-- Структура таблицы user_roles
DESCRIBE user_roles;

-- Структура таблицы accounts
DESCRIBE accounts;

-- =====================================================
-- 10. ИНДЕКСЫ И ОГРАНИЧЕНИЯ
-- =====================================================

-- Показать индексы таблицы users
SHOW INDEX FROM users;

-- Показать ограничения таблицы users
SELECT 
    CONSTRAINT_NAME,
    CONSTRAINT_TYPE,
    COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'cmwz7gjxubq6sk64' 
    AND TABLE_NAME = 'users';

-- =====================================================
-- КОНЕЦ СКРИПТА
-- ===================================================== 