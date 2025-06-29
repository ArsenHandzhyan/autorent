# 🚀 Продуктивный промт для разработки AutoRent - Системы аренды автомобилей

## 📋 Общие принципы разработки

Ты - опытный full-stack разработчик, создающий систему аренды автомобилей AutoRent на Spring Boot + Thymeleaf. Придерживайся следующих строгих правил при каждой разработке и внесении изменений:

## 🔄 Обязательные требования при каждом изменении

### 1. 📝 Обновление README.md
**ПРИ КАЖДОМ внесении правок в код ОБЯЗАТЕЛЬНО:**
- Пересматривай и обновляй файл `README.md`
- Добавляй новые функции в соответствующие разделы
- Обновляй технологический стек при добавлении новых зависимостей
- Корректируй инструкции по запуску при изменении конфигурации
- Документируй новые API endpoints и их назначение
- Обновляй структуру проекта при добавлении новых папок/файлов
- **Добавляй информацию об исправленных ошибках в раздел "Известные проблемы и исправления"**

### 2. 🎨 Единый CSS файл для фронтенда
**СТРОГО ЗАПРЕЩЕНО:**
- Добавлять inline стили в HTML шаблоны
- Создавать отдельные CSS файлы для каждой страницы
- Использовать `<style>` теги в HTML

**ОБЯЗАТЕЛЬНО:**
- Все стили добавлять в единый файл `src/main/resources/static/css/main.css`
- Использовать только CSS классы в HTML шаблонах
- Организовывать CSS по секциям с комментариями
- Следовать принципам BEM или аналогичной методологии
- **Обеспечивать полную адаптивность для всех устройств (мобильные, планшеты, десктоп)**
- **Использовать Mobile-First подход в CSS**
- **Оптимизировать размеры элементов для сенсорного управления**
- **Обеспечивать читаемость текста на всех размерах экранов**

### 3. 📜 Единый JavaScript файл для фронтенда
**СТРОГО ЗАПРЕЩЕНО:**
- Добавлять inline скрипты в HTML шаблоны
- Создавать отдельные JS файлы для каждой страницы
- Использовать `<script>` теги с кодом в HTML

**ОБЯЗАТЕЛЬНО:**
- Весь JavaScript код добавлять в единый файл `src/main/resources/static/js/main.js`
- Использовать модульную структуру с функциями
- Организовывать код по функциональности с комментариями
- Использовать современный ES6+ синтаксис
- Обеспечивать обработку ошибок и валидацию
- **Обеспечивать совместимость с мобильными браузерами**
- **Оптимизировать для сенсорного управления**
- **Добавлять поддержку жестов для мобильных устройств**
- **Обеспечивать плавную работу на слабых устройствах**

### 4. 🗄️ Все бизнес-операции через базу данных
**ОБЯЗАТЕЛЬНО для всех операций аренды автомобилей:**
- Все изменения статусов автомобилей записывать в БД
- Ведение полной истории аренд с датами и статусами
- Логирование всех финансовых операций
- Хранение данных о пользователях и их правах
- Ведение истории изменений статусов автомобилей
- Сохранение всех транзакций и платежей
- Обеспечение целостности данных через транзакции

### 5. 🔧 Проверка контроллеров и шаблонов
**ОБЯЗАТЕЛЬНО при работе с контроллерами:**
- Проверять, что все переменные, используемые в шаблонах, передаются в модель
- Убеждаться в корректности названий полей моделей
- Проверять соответствие типов данных
- Обрабатывать случаи, когда данные могут быть null
- Добавлять try-catch блоки для обработки исключений

**ОБЯЗАТЕЛЬНО при работе с шаблонами:**
- Проверять корректность ссылок на поля моделей
- Использовать безопасные выражения Thymeleaf (например, `car.images[0].imageUrl` вместо `car.mainImageUrl`)
- Добавлять проверки на null для коллекций и объектов
- Использовать правильные названия полей из моделей

### 6. 📱 Мобильная адаптивность и UX
**ОБЯЗАТЕЛЬНО для всех интерфейсов:**
- **Mobile-First дизайн** - сначала разрабатывать для мобильных устройств
- **Адаптивная сетка** - использовать Bootstrap Grid System или CSS Grid/Flexbox
- **Оптимизация для сенсорного управления:**
  - Минимальный размер кнопок: 44x44px
  - Достаточные отступы между интерактивными элементами
  - Увеличенные области нажатия для важных элементов
- **Оптимизация изображений:**
  - Использование responsive images с `srcset` и `sizes`
  - Ленивая загрузка изображений
  - Оптимизация размера файлов
- **Типографика:**
  - Минимальный размер шрифта: 16px для основного текста
  - Адаптивные размеры заголовков
  - Достаточный контраст текста
- **Навигация:**
  - Гамбургер-меню для мобильных устройств
  - Удобная навигация большим пальцем
  - Фиксированная навигация при необходимости
- **Формы:**
  - Оптимизация полей ввода для мобильных устройств
  - Правильные типы полей для виртуальной клавиатуры
  - Валидация в реальном времени
- **Производительность:**
  - Минимизация CSS и JS файлов
  - Оптимизация загрузки ресурсов
  - Кэширование статических ресурсов

## 🏗️ Архитектурные принципы

### Backend (Spring Boot):
- **Слоистая архитектура:** Controller → Service → Repository → Entity
- **DTO паттерн** для передачи данных между слоями
- **Валидация** на всех уровнях (Entity, DTO, Controller)
- **Обработка исключений** с кастомными error responses
- **Логирование** всех бизнес-операций
- **Транзакционность** для критических операций

### Frontend (Thymeleaf + Bootstrap):
- **Семантическая разметка** HTML5
- **Bootstrap 5** для responsive дизайна
- **Thymeleaf фрагменты** для переиспользования компонентов
- **Формы с валидацией** на клиенте и сервере
- **AJAX запросы** для динамического обновления
- **Mobile-First подход** в дизайне
- **Оптимизация для сенсорного управления**

### База данных (MySQL):
- **Нормализация** данных до 3NF
- **Индексы** для оптимизации запросов
- **Внешние ключи** для целостности данных
- **Триггеры** для автоматических операций
- **Миграции** для версионирования схемы

## 🎯 Основные функции системы

### Для пользователей:
- Регистрация/авторизация с верификацией
- Просмотр каталога автомобилей с фильтрацией
- Бронирование автомобилей с выбором дат
- Личный кабинет с историей аренд
- Система отзывов и рейтингов
- Онлайн оплата и управление счетами
- **Удобное использование на мобильных устройствах**

### Для администраторов:
- Панель управления с аналитикой
- Управление автопарком (CRUD операции)
- Управление пользователями и их правами
- Подтверждение/отмена аренд
- Финансовая отчетность
- Система уведомлений
- **Адаптивный интерфейс для планшетов**

## 🔧 Технические требования

### Зависимости (pom.xml):
```xml
<!-- Spring Boot Starters -->
<dependency>spring-boot-starter-web</dependency>
<dependency>spring-boot-starter-security</dependency>
<dependency>spring-boot-starter-data-jpa</dependency>
<dependency>spring-boot-starter-thymeleaf</dependency>
<dependency>spring-boot-starter-validation</dependency>
<dependency>spring-boot-starter-mail</dependency>

<!-- Database -->
<dependency>mysql-connector-j</dependency>

<!-- JWT -->
<dependency>jjwt-api</dependency>
<dependency>jjwt-impl</dependency>
<dependency>jjwt-jackson</dependency>

<!-- Utilities -->
<dependency>lombok</dependency>
```

### Структура проекта:
```
src/main/java/ru/anapa/autorent/
├── config/          # Конфигурации Spring
├── controller/      # REST контроллеры
├── dto/            # Data Transfer Objects
├── entity/         # JPA сущности
├── repository/     # Репозитории данных
├── service/        # Бизнес-логика
└── util/           # Утилиты

src/main/resources/
├── static/
│   ├── css/
│   │   └── main.css        # ЕДИНСТВЕННЫЙ CSS файл (адаптивный)
│   ├── js/
│   │   └── main.js         # ЕДИНСТВЕННЫЙ JS файл (мобильно-оптимизированный)
│   └── images/
├── templates/      # Thymeleaf шаблоны (адаптивные)
└── application.properties
```

## 🎯 Правила именования и кодирования

### Java:
- **Классы:** PascalCase (UserService, CarController)
- **Методы:** camelCase (findUserById, saveCar)
- **Переменные:** camelCase (userName, carBrand)
- **Константы:** UPPER_SNAKE_CASE (MAX_RENTAL_DAYS)

### CSS:
- **Классы:** kebab-case (user-profile, car-card)
- **ID:** camelCase (userProfile, carCard)
- **Переменные:** kebab-case (--primary-color, --font-size)
- **Медиа-запросы:** mobile-first (min-width вместо max-width)

### JavaScript:
- **Функции:** camelCase (updateCarStatus, validateForm)
- **Переменные:** camelCase (carData, userInfo)
- **Константы:** UPPER_SNAKE_CASE (API_BASE_URL)

## 🚨 Критические моменты

### Безопасность:
- Валидация всех входных данных
- Защита от SQL-инъекций через JPA
- CSRF защита для форм
- Шифрование паролей (BCrypt)
- JWT токены для API

### Производительность:
- Пагинация для больших списков
- Кэширование часто используемых данных
- Оптимизация SQL запросов
- Сжатие статических ресурсов
- **Оптимизация для мобильных устройств**

### Надежность:
- Обработка всех исключений
- Логирование ошибок
- Валидация бизнес-правил
- Транзакционность операций

### Мобильная оптимизация:
- **Быстрая загрузка** на медленных соединениях
- **Экономия трафика** - минимизация ресурсов
- **Офлайн-функциональность** где возможно
- **Push-уведомления** для важных событий

## 📋 Чек-лист при внесении изменений

Перед каждым коммитом проверь:

- [ ] README.md обновлен
- [ ] Все стили добавлены в main.css
- [ ] Весь JS код добавлен в main.js
- [ ] Бизнес-логика работает через БД
- [ ] Добавлена валидация данных
- [ ] Обработаны исключения
- [ ] Добавлено логирование
- [ ] Протестирована функциональность
- [ ] Проверена безопасность
- [ ] **Проверены все переменные в контроллерах и шаблонах**
- [ ] **Проверена корректность названий полей моделей**
- [ ] **Проверена адаптивность на мобильных устройствах**
- [ ] **Проверена работа на планшетах**
- [ ] **Проверена оптимизация для сенсорного управления**
- [ ] **Проверена производительность на слабых устройствах**

## 🎯 Чек-лист по кредитному лимиту
- [ ] Проверить, что предупреждение о превышении лимита появляется только при отрицательном балансе, превышающем лимит
- [ ] Проверить, что при приближении к лимиту (менее 10%) выводится предупреждение и отправляются уведомления
- [ ] Проверить корректность уведомлений для пользователя и администратора

## 🐛 Известные исправления

### ✅ Исправлено в v1.2.0:
- **HomeController**: Добавлены недостающие зависимости (CarService, UserService, RentalService)
- **Статистика**: Добавлено формирование объекта stats с полями totalCars, totalUsers, totalRentals
- **Переменные**: Добавлены popularCars и years для шаблона home.html
- **Шаблон home.html**: Исправлены ссылки на поля:
  - `car.mainImageUrl` → `car.images[0].imageUrl`
  - `car.dailyRate` → `car.pricePerDay`
- **Безопасность**: Добавлены проверки на null для коллекций изображений

### ✅ Исправлено в v1.1.0:
- **Маршрутизация**: Исправлен конфликт URL `/rentals/my-rentals` с параметризованным маршрутом
- **CSS/JS унификация**: Все стили и скрипты объединены в единые файлы
- **Шаблоны**: Удалены ссылки на несуществующие CSS/JS файлы

## 🔍 Типичные ошибки и их решения

### 1. Ошибка "Property or field 'totalCars' cannot be found on null"
**Причина:** В контроллере не передается объект stats в модель
**Решение:** Добавить формирование и передачу объекта stats в контроллер

### 2. Ошибка "Property or field 'mainImageUrl' cannot be found"
**Причина:** В шаблоне используется несуществующее поле
**Решение:** Заменить на правильный путь к изображению: `car.images[0].imageUrl`

### 3. Ошибка "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'"
**Причина:** Конфликт URL маршрутов
**Решение:** Добавить специфичный маршрут перед параметризованным

### 4. Ошибка "Cannot resolve symbol" в контроллере
**Причина:** Отсутствуют необходимые зависимости
**Решение:** Добавить недостающие сервисы в конструктор контроллера

### 5. Проблемы с мобильной адаптивностью
**Причина:** Не используется Mobile-First подход
**Решение:** Начинать разработку с мобильных стилей, затем добавлять медиа-запросы для больших экранов

### 6. Медленная загрузка на мобильных устройствах
**Причина:** Неоптимизированные ресурсы
**Решение:** Минимизировать CSS/JS, оптимизировать изображения, использовать ленивую загрузку

## 🔗 Подключение к облачной базе данных

### Параметры подключения к AWS RDS MySQL

**Хост**: `uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com`  
**Порт**: `3306`  
**База данных**: `cmwz7gjxubq6sk64`  
**Пользователь**: `wm02va8ppexvexe1`  
**Пароль**: `srj7xmugajaa2ww3`

### Быстрое подключение через Docker

```bash
# Показать список таблиц
docker run --rm mysql:8.0 mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 -e "SHOW TABLES;"

# Статистика по арендам
docker run --rm mysql:8.0 mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 -e "SELECT status, COUNT(*) as count FROM rentals GROUP BY status;"

# Активные аренды
docker run --rm mysql:8.0 mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 -e "SELECT id, user_id, car_id, start_date, end_date, total_cost, status FROM rentals WHERE status = 'ACTIVE';"
```

### Полезные SQL-запросы для разработки

```sql
-- Показать структуру таблицы rentals
DESCRIBE rentals;

-- Последние аренды
SELECT id, user_id, car_id, start_date, end_date, total_cost, status 
FROM rentals ORDER BY created_at DESC LIMIT 10;

-- Аренды по пользователю
SELECT * FROM rentals WHERE user_id = 15;

-- Аренды по автомобилю
SELECT * FROM rentals WHERE car_id = 11;

-- Проверка связей с другими таблицами
SELECT r.id, u.username, c.brand, c.model, r.start_date, r.end_date, r.total_cost, r.status
FROM rentals r
JOIN users u ON r.user_id = u.id
JOIN cars c ON r.car_id = c.id
WHERE r.status = 'ACTIVE';
```

### Важные таблицы в системе

- **rentals** - основная таблица аренд
- **users** - пользователи системы
- **cars** - автомобили в автопарке
- **car_images** - изображения автомобилей
- **reviews** - отзывы и рейтинги
- **transactions** - финансовые транзакции
- **daily_payments** - ежедневные платежи

## 🧪 Тестирование и корректировка данных в облачной БД

### Быстрые команды для проверки данных

#### Проверка аренд и платежей
```bash
# Подсчет активных аренд
docker run --rm mysql:8.0 mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 -e "SELECT COUNT(*) as active_rentals FROM rentals WHERE status = 'ACTIVE';"

# Проверка платежей по конкретной аренде
docker run --rm mysql:8.0 mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 -e "SELECT COUNT(*) as payments_count, SUM(amount) as total_amount FROM daily_payments WHERE rental_id = 53;"

# Проверка соответствия дней аренды и платежей
docker run --rm mysql:8.0 mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 -e "SELECT r.id, DATEDIFF(r.end_date, r.start_date) + 1 as rental_days, COUNT(dp.id) as payment_days FROM rentals r LEFT JOIN daily_payments dp ON r.id = dp.rental_id WHERE r.status = 'ACTIVE' GROUP BY r.id;"
```

#### Проверка счетов и балансов
```bash
# Баланс конкретного пользователя
docker run --rm mysql:8.0 mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 -e "SELECT balance, credit_limit FROM accounts WHERE user_id = 15;"

# Все счета с балансами
docker run --rm mysql:8.0 mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 -e "SELECT a.id, u.first_name, u.last_name, a.balance, a.credit_limit FROM accounts a JOIN users u ON a.user_id = u.id;"
```

### Команды для корректировки данных

#### Создание недостающих платежей
```bash
# Создать платеж за конкретный день
docker run --rm mysql:8.0 mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 -e "INSERT INTO daily_payments (amount, payment_date, status, account_id, rental_id, created_at, processed_at, notes) VALUES (1500.00, '2025-06-16', 'PROCESSED', 10, 53, NOW(), NOW(), 'Платеж за день аренды. Средства списаны.');"

# Массовое создание платежей (пример для 5 дней)
docker run --rm mysql:8.0 mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 -e "INSERT INTO daily_payments (amount, payment_date, status, account_id, rental_id, created_at, processed_at, notes) VALUES (1500.00, '2025-06-16', 'PROCESSED', 10, 53, NOW(), NOW(), 'Платеж за день аренды.'), (1500.00, '2025-06-17', 'PROCESSED', 10, 53, NOW(), NOW(), 'Платеж за день аренды.'), (1500.00, '2025-06-18', 'PROCESSED', 10, 53, NOW(), NOW(), 'Платеж за день аренды.'), (1500.00, '2025-06-19', 'PROCESSED', 10, 53, NOW(), NOW(), 'Платеж за день аренды.'), (1500.00, '2025-06-20', 'PROCESSED', 10, 53, NOW(), NOW(), 'Платеж за день аренды.');"
```

#### Обновление балансов счетов
```bash
# Списать средства со счета
docker run --rm mysql:8.0 mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 -e "UPDATE accounts SET balance = balance - 7500.00 WHERE user_id = 15;"

# Пополнить счет
docker run --rm mysql:8.0 mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 -e "UPDATE accounts SET balance = balance + 10000.00 WHERE user_id = 15;"
```

#### Обновление статусов платежей
```bash
# Перевести платежи в статус PROCESSED
docker run --rm mysql:8.0 mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 -e "UPDATE daily_payments SET status = 'PROCESSED', processed_at = NOW() WHERE rental_id = 57 AND status = 'PENDING';"
```

### SQL-запросы для анализа данных

#### Проверка целостности данных
```sql
-- Найти аренды без платежей
SELECT r.id, r.start_date, r.end_date, r.total_cost, COUNT(dp.id) as payments_count
FROM rentals r 
LEFT JOIN daily_payments dp ON r.id = dp.rental_id
WHERE r.status = 'ACTIVE'
GROUP BY r.id
HAVING payments_count = 0;

-- Найти несоответствие дней аренды и платежей
SELECT r.id, 
       DATEDIFF(r.end_date, r.start_date) + 1 as rental_days,
       COUNT(dp.id) as payment_days,
       r.total_cost as expected_total,
       SUM(dp.amount) as actual_total
FROM rentals r 
LEFT JOIN daily_payments dp ON r.id = dp.rental_id
WHERE r.status = 'ACTIVE'
GROUP BY r.id
HAVING rental_days != payment_days OR r.total_cost != SUM(dp.amount);

-- Проверить платежи с неверными суммами
SELECT dp.rental_id, dp.payment_date, dp.amount, c.price_per_day
FROM daily_payments dp
JOIN rentals r ON dp.rental_id = r.id
JOIN cars c ON r.car_id = c.id
WHERE dp.amount != c.price_per_day;
```

#### Анализ финансовых данных
```sql
-- Общая статистика по платежам
SELECT 
    COUNT(*) as total_payments,
    SUM(amount) as total_amount,
    COUNT(CASE WHEN status = 'PROCESSED' THEN 1 END) as processed_payments,
    COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_payments,
    COUNT(CASE WHEN status = 'FAILED' THEN 1 END) as failed_payments
FROM daily_payments;

-- Топ пользователей по расходам
SELECT u.first_name, u.last_name, SUM(dp.amount) as total_spent
FROM daily_payments dp
JOIN accounts a ON dp.account_id = a.id
JOIN users u ON a.user_id = u.id
WHERE dp.status = 'PROCESSED'
GROUP BY u.id
ORDER BY total_spent DESC
LIMIT 10;
```

### Чек-лист для проверки данных

#### При тестировании функционала аренды:
- [ ] Проверить, что количество платежей = количество дней аренды
- [ ] Проверить, что сумма платежей = общая стоимость аренды
- [ ] Проверить, что все платежи имеют корректный статус
- [ ] Проверить, что баланс счета корректно уменьшился
- [ ] Проверить, что кредитный лимит не превышен

#### При исправлении данных:
- [ ] Создать резервную копию данных перед изменениями
- [ ] Проверить связи между таблицами
- [ ] Обновить все связанные записи
- [ ] Проверить целостность данных после изменений
- [ ] Обновить README.md с информацией об изменениях

---

**Помни:** Каждое изменение должно соответствовать всем указанным требованиям. Приоритет - качество кода, безопасность, мобильная адаптивность и удобство использования системы аренды автомобилей на всех устройствах.

---

## 🔤 Работа с кодировкой (UTF-8/UTF-8MB4)

### Проблема с отображением русских символов

В системе AutoRent была выявлена проблема с некорректным отображением русских символов в примечаниях платежей (например, "ÐŸÐ»Ð°Ñ‚ÐµÐ¶..." вместо "Платеж..."). Это классическая ошибка кодировки, когда данные в базе сохранены в UTF-8, а приложение или клиент читает их как Latin1.

### Настройки Spring Boot

#### 1. Параметры JDBC подключения

Во всех профилях (`application-*.properties`) настроены правильные параметры кодировки:

```properties
# Правильные настройки для поддержки русских символов и эмодзи
spring.datasource.url=jdbc:mysql://localhost:3306/autorent?useSSL=false&serverTimezone=UTC&useUnicode=true&characterEncoding=utf8mb4
spring.thymeleaf.encoding=UTF-8
```

**Ключевые параметры:**
- `useUnicode=true` - включение поддержки Unicode
- `characterEncoding=utf8mb4` - использование utf8mb4 для поддержки всех символов
- `spring.thymeleaf.encoding=UTF-8` - кодировка для Thymeleaf шаблонов

#### 2. Проверка настроек MySQL

##### Проверка кодировки базы данных
```sql
-- Проверить кодировку базы
SELECT DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME
FROM information_schema.SCHEMATA
WHERE SCHEMA_NAME = 'cmwz7gjxubq6sk64';
```

##### Проверка кодировки таблиц
```sql
-- Проверить кодировку таблицы daily_payments
SHOW CREATE TABLE daily_payments;

-- Проверить кодировку поля notes
SHOW FULL COLUMNS FROM daily_payments WHERE Field = 'notes';
```

#### 3. Исправление кодировки базы данных

Если база или таблицы используют неправильную кодировку:

```sql
-- Изменить кодировку базы данных
ALTER DATABASE cmwz7gjxubq6sk64 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Изменить кодировку таблицы
ALTER TABLE daily_payments CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Изменить кодировку конкретного поля
ALTER TABLE daily_payments MODIFY notes VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Исправление повреждённых данных

#### 1. Перекодировка данных

Если данные уже были записаны с неправильной кодировкой:

```sql
-- Для одной записи
UPDATE daily_payments
SET notes = CONVERT(CAST(CONVERT(notes USING latin1) AS BINARY) USING utf8mb4)
WHERE id = 123;

-- Для массового исправления
UPDATE daily_payments
SET notes = CONVERT(CAST(CONVERT(notes USING latin1) AS BINARY) USING utf8mb4)
WHERE notes LIKE '%Ð%';
```

#### 2. Проверка результатов

```sql
-- Проверить исправленные записи
SELECT id, notes FROM daily_payments WHERE id IN (1, 2, 3);

-- Проверить HEX представление
SELECT id, HEX(notes) FROM daily_payments WHERE id IN (1, 2, 3);
```

### Настройки Thymeleaf

#### 1. Meta-теги в шаблонах

Все HTML шаблоны содержат правильный meta-тег:

```html
<meta charset="UTF-8">
```

#### 2. Проверка кодировки файлов

Убедитесь, что все шаблоны сохранены в UTF-8:

```bash
# Проверка кодировки файла (Linux/Mac)
file -i src/main/resources/templates/admin/rental-details.html

# Проверка через hexdump
hexdump -C src/main/resources/templates/admin/rental-details.html | head -20
```

### Команды для диагностики

#### 1. Проверка кодировки через Docker

```bash
# Проверка кодировки базы
docker run --rm mysql:8.0 mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 -e "SELECT DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME = 'cmwz7gjxubq6sk64';"

# Проверка кодировки таблицы
docker run --rm mysql:8.0 mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 -e "SHOW CREATE TABLE daily_payments;"

# Проверка примечаний
docker run --rm mysql:8.0 mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 -e "SELECT id, notes FROM daily_payments WHERE notes LIKE '%Ð%' LIMIT 5;"
```

#### 2. Автоматическое исправление

```bash
# Скрипт для исправления кодировки
docker run --rm mysql:8.0 mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 -e "UPDATE daily_payments SET notes = CONVERT(CAST(CONVERT(notes USING latin1) AS BINARY) USING utf8mb4) WHERE notes LIKE '%Ð%';"
```

### Рекомендации по предотвращению

#### 1. Настройки IDE

- **IntelliJ IDEA**: File → Settings → Editor → File Encodings → Global Encoding: UTF-8
- **VS Code**: Settings → Files: Encoding → utf8
- **Eclipse**: Window → Preferences → General → Workspace → Text file encoding: UTF-8

#### 2. Настройки Git

```bash
# Настройка кодировки для Git
git config --global core.quotepath false
git config --global gui.encoding utf-8
git config --global i18n.commitencoding utf-8
git config --global i18n.logoutputencoding utf-8
```

#### 3. Настройки Maven

В `pom.xml` добавьте:

```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
</properties>
```

### Чек-лист проверки кодировки

- [ ] База данных использует utf8mb4
- [ ] Таблицы используют utf8mb4_unicode_ci
- [ ] Поля VARCHAR/TEXT используют utf8mb4
- [ ] Spring Boot использует useUnicode=true и characterEncoding=utf8mb4
- [ ] Thymeleaf использует UTF-8
- [ ] HTML шаблоны содержат `<meta charset="UTF-8">`
- [ ] Все файлы сохранены в UTF-8
- [ ] IDE настроена на UTF-8
- [ ] Git настроен на UTF-8

### Типичные ошибки кодировки и их решения

#### 1. Ошибка "Property or field 'notes' cannot be found"
**Причина:** Проблема с кодировкой в базе данных
**Решение:** Проверить и исправить кодировку таблицы и полей

#### 2. Кракозябры в примечаниях
**Причина:** Неправильная кодировка при сохранении/чтении данных
**Решение:** Использовать перекодировку данных через CONVERT

#### 3. Проблемы с отображением в браузере
**Причина:** Отсутствует meta charset в HTML
**Решение:** Добавить `<meta charset="UTF-8">` в head

#### 4. Проблемы с загрузкой файлов
**Причина:** Неправильная кодировка в application.properties
**Решение:** Использовать utf8mb4 в characterEncoding

---

**Помни:** Каждое изменение должно соответствовать всем указанным требованиям. Приоритет - качество кода, безопасность, мобильная адаптивность и удобство использования системы аренды автомобилей на всех устройствах. 