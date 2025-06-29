# 🚗 AutoRent - Система аренды автомобилей

Современная система аренды автомобилей с Spring Boot backend и React frontend, включающая визуальное тестирование компонентов через Storybook.

## 📋 Содержание

- [Обзор проекта](#обзор-проекта)
- [Архитектура](#архитектура)
- [Технологический стек](#технологический-стек)
- [Быстрый старт](#быстрый-старт)
- [MSP Серверы MySQL](#msp-серверы-mysql)
- [Структура проекта](#структура-проекта)
- [API документация](#api-документация)
- [Разработка](#разработка)
- [Тестирование](#тестирование)
- [Развертывание](#развертывание)

## 🎯 Обзор проекта

AutoRent - это полнофункциональная система аренды автомобилей, состоящая из:

- **Backend**: Spring Boot приложение с REST API
- **Frontend**: React приложение с современным UI
- **Визуальное тестирование**: Storybook для разработки компонентов
- **База данных**: MySQL для хранения данных
- **Документация**: Подробные гайды по разработке

### Основные функции

#### Для пользователей:
- 📱 Регистрация и авторизация
- 🔍 Поиск и фильтрация автомобилей
- 📅 Бронирование с выбором дат
- 💳 Онлайн оплата
- 📊 Личный кабинет с историей
- ⭐ Система отзывов и рейтингов

#### Для администраторов:
- 🎛️ Панель управления
- 🚗 Управление автопарком
- 👥 Управление пользователями
- 📈 Аналитика и отчеты
- 🔔 Система уведомлений

## 🏗️ Архитектура

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   React App     │    │  Spring Boot    │    │     MySQL       │
│   (Frontend)    │◄──►│   (Backend)     │◄──►│   (Database)    │
│                 │    │                 │    │                 │
│ • Components    │    │ • Controllers   │    │ • Users         │
│ • Pages         │    │ • Services      │    │ • Cars          │
│ • Storybook     │    │ • Repositories  │    │ • Bookings      │
│ • API Client    │    │ • Security      │    │ • Payments      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🛠️ Технологический стек

### Backend (Spring Boot)
- **Framework**: Spring Boot 3.x
- **Security**: Spring Security + JWT
- **Database**: Spring Data JPA + MySQL
- **Validation**: Bean Validation
- **Documentation**: Swagger/OpenAPI
- **Build Tool**: Maven

### Frontend (React)
- **Framework**: React 18
- **Build Tool**: Create React App
- **Styling**: CSS Modules + BEM
- **Testing**: Jest + React Testing Library
- **Visual Testing**: Storybook
- **Package Manager**: npm

### Инструменты разработки
- **IDE**: IntelliJ IDEA / VS Code
- **Version Control**: Git
- **API Testing**: Postman / Insomnia
- **Database**: MySQL Workbench
- **Containerization**: Docker

## 🚀 Быстрый старт

### Предварительные требования

- Java 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.6+
- Git

### 1. Клонирование репозитория

```bash
git clone <repository-url>
cd autorent
```

### 2. Настройка базы данных

```bash
# Создайте базу данных
mysql -u root -p
CREATE DATABASE autorent;
CREATE USER 'autorent'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON autorent.* TO 'autorent'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Настройка Backend

```bash
# Перейдите в корневую папку проекта
cd autorent

# Настройте application.properties
cp src/main/resources/application-dev.properties.example src/main/resources/application-dev.properties

# Запустите Spring Boot приложение
mvn spring-boot:run
```

Backend будет доступен на http://localhost:8080

### 4. Настройка Frontend

```bash
# Перейдите в папку React приложения
cd autorent-react

# Установите зависимости
npm install

# Запустите React приложение
npm start
```

Frontend будет доступен на http://localhost:3000

### 5. Запуск Storybook

```bash
# В папке autorent-react
npm run storybook
```

Storybook будет доступен на http://localhost:6006

## 🗄️ MSP Серверы MySQL

Система AutoRent поддерживает Multiple Server Profile (MSP) конфигурацию для подключения к различным MySQL серверам в зависимости от окружения.

### Доступные профили

| Профиль | Сервер | База данных | Порт | Назначение |
|---------|--------|-------------|------|------------|
| **local** | `localhost:3306` | `autorent` | 8080 | Локальная разработка |
| **dev** | AWS RDS MySQL | `cmwz7gjxubq6sk64` | 8080 | Разработка |
| **test** | `test-mysql-server:3306` | `autorent_test` | 8081 | Тестирование |
| **staging** | `staging-mysql-server:3306` | `autorent_staging` | 8080 | Предпродакшн |
| **prod** | AWS RDS MySQL | `cmwz7gjxubq6sk64` | 8080 | Продакшн |
| **backup** | `backup-mysql-server:3306` | `autorent_backup` | 8082 | Резервный сервер |

### Быстрый запуск с профилями

#### Windows
```bash
# Локальная разработка
scripts\start-local.bat

# Разработка
scripts\start-dev.bat

# Тестирование
scripts\start-test.bat

# Предпродакшн
scripts\start-staging.bat

# Продакшн
scripts\start-prod.bat

# Резервный сервер
scripts\start-backup.bat
```

#### Linux/Mac
```bash
# Сделать скрипты исполняемыми
chmod +x scripts/*.sh

# Локальная разработка
./scripts/start-local.sh

# Разработка
./scripts/start-dev.sh

# Тестирование
./scripts/start-test.sh

# Предпродакшн
./scripts/start-staging.sh

# Продакшн
./scripts/start-prod.sh

# Резервный сервер
./scripts/start-backup.sh
```

#### Maven команды
```bash
# Локальная разработка
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Разработка
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Тестирование
mvn spring-boot:run -Dspring-boot.run.profiles=test

# Предпродакшн
mvn spring-boot:run -Dspring-boot.run.profiles=staging

# Продакшн
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Резервный сервер
mvn spring-boot:run -Dspring-boot.run.profiles=backup
```

### Мониторинг MSP серверов

#### API endpoints для мониторинга
```bash
# Проверка состояния подключения
GET /api/database/health

# Проверка производительности
GET /api/database/performance

# Проверка доступности таблиц
GET /api/database/tables

# Полная диагностика
GET /api/database/diagnostic

# Информация о конфигурации
GET /api/database/info

# Тестирование подключения
POST /api/database/test-connection
```

#### Пример использования
```bash
# Проверка здоровья базы данных
curl -X GET http://localhost:8080/api/database/health \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Полная диагностика
curl -X GET http://localhost:8080/api/database/diagnostic \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Конфигурация

- **Файлы конфигурации**: `application-{profile}.properties`
- **Пул соединений**: HikariCP с оптимизированными настройками
- **Безопасность**: SSL соединения, шифрование паролей
- **Мониторинг**: Встроенные health checks и метрики

### 🔗 Подключение к облачной базе данных

#### Параметры подключения к AWS RDS MySQL

**Хост**: `uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com`  
**Порт**: `3306`  
**База данных**: `cmwz7gjxubq6sk64`  
**Пользователь**: `wm02va8ppexvexe1`  
**Пароль**: `srj7xmugajaa2ww3`

#### Подключение через MySQL-клиент

**Установка MySQL-клиента:**
```bash
# Windows (Chocolatey)
choco install mysql -y

# Linux (Ubuntu/Debian)
sudo apt-get install mysql-client

# macOS (Homebrew)
brew install mysql-client
```

**Команда подключения:**
```bash
mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -p cmwz7gjxubq6sk64
```

#### Подключение через Docker

**Быстрое подключение без установки клиента:**
```bash
# Показать список таблиц
docker run --rm mysql:8.0 mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 -e "SHOW TABLES;"

# Подсчитать количество аренд
docker run --rm mysql:8.0 mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 -e "SELECT COUNT(*) FROM rentals;"

# Показать активные аренды
docker run --rm mysql:8.0 mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 -e "SELECT * FROM rentals WHERE status = 'ACTIVE';"
```

#### Подключение через GUI-клиенты

**DBeaver:**
1. Создать новое соединение → MySQL
2. Хост: `uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com`
3. Порт: `3306`
4. База данных: `cmwz7gjxubq6sk64`
5. Пользователь: `wm02va8ppexvexe1`
6. Пароль: `srj7xmugajaa2ww3`

**MySQL Workbench:**
1. Создать новое соединение
2. Connection Name: `AutoRent Cloud DB`
3. Hostname: `uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com`
4. Port: `3306`
5. Username: `wm02va8ppexvexe1`
6. Password: `srj7xmugajaa2ww3`
7. Default Schema: `cmwz7gjxubq6sk64`

#### Полезные SQL-запросы

```sql
-- Показать все таблицы
SHOW TABLES;

-- Статистика по арендам
SELECT status, COUNT(*) as count FROM rentals GROUP BY status;

-- Активные аренды
SELECT id, user_id, car_id, start_date, end_date, total_cost, status 
FROM rentals WHERE status = 'ACTIVE';

-- Последние аренды
SELECT id, user_id, car_id, start_date, end_date, total_cost, status 
FROM rentals ORDER BY created_at DESC LIMIT 10;

-- Аренды по пользователю
SELECT * FROM rentals WHERE user_id = 15;

-- Аренды по автомобилю
SELECT * FROM rentals WHERE car_id = 11;
```

📖 **Подробная документация**: [MSP_SERVERS.md](./docs/MSP_SERVERS.md)

## 📁 Структура проекта

```
autorent/
├── src/                          # Spring Boot backend
│   ├── main/
│   │   ├── java/ru/anapa/autorent/
│   │   │   ├── config/           # Конфигурации
│   │   │   ├── controller/       # REST контроллеры
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entity/          # JPA сущности
│   │   │   ├── repository/      # Репозитории
│   │   │   ├── service/         # Бизнес-логика
│   │   │   └── util/            # Утилиты
│   │   └── resources/
│   │       ├── static/          # Статические файлы
│   │       ├── templates/       # Thymeleaf шаблоны
│   │       └── application.properties
│   └── test/                    # Тесты backend
├── autorent-react/              # React frontend
│   ├── src/
│   │   ├── components/          # React компоненты
│   │   ├── pages/              # Страницы
│   │   ├── hooks/              # Кастомные хуки
│   │   ├── services/           # API сервисы
│   │   ├── stories/            # Storybook сторисы
│   │   └── styles/             # CSS файлы
│   ├── .storybook/             # Конфигурация Storybook
│   └── tests/                  # Тесты frontend
├── database/                   # SQL скрипты
├── docs/                       # Документация
├── uploads/                    # Загруженные файлы
└── logs/                       # Логи приложения
```

## 📚 API документация

### Основные endpoints

#### Автомобили
```
GET    /api/cars              # Список автомобилей
GET    /api/cars/{id}         # Детали автомобиля
POST   /api/cars              # Создание автомобиля (admin)
PUT    /api/cars/{id}         # Обновление автомобиля (admin)
DELETE /api/cars/{id}         # Удаление автомобиля (admin)
```

#### Бронирования
```
GET    /api/bookings          # Список бронирований
GET    /api/bookings/{id}     # Детали бронирования
POST   /api/bookings          # Создание бронирования
PUT    /api/bookings/{id}     # Обновление бронирования
DELETE /api/bookings/{id}     # Отмена бронирования
```

#### Пользователи
```
GET    /api/users/profile     # Профиль пользователя
PUT    /api/users/profile     # Обновление профиля
POST   /api/auth/register     # Регистрация
POST   /api/auth/login        # Авторизация
POST   /api/auth/logout       # Выход
```

### Swagger UI

API документация доступна по адресу: http://localhost:8080/swagger-ui.html

## 👨‍💻 Разработка

### Backend разработка

#### Создание нового контроллера

```java
@RestController
@RequestMapping("/api/example")
@Validated
public class ExampleController {
    
    private final ExampleService exampleService;
    
    public ExampleController(ExampleService exampleService) {
        this.exampleService = exampleService;
    }
    
    @GetMapping
    public ResponseEntity<List<ExampleDto>> getAll() {
        return ResponseEntity.ok(exampleService.findAll());
    }
    
    @PostMapping
    public ResponseEntity<ExampleDto> create(@Valid @RequestBody ExampleDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                           .body(exampleService.create(dto));
    }
}
```

#### Создание сервиса

```java
@Service
@Transactional
public class ExampleService {
    
    private final ExampleRepository repository;
    
    public ExampleService(ExampleRepository repository) {
        this.repository = repository;
    }
    
    public List<ExampleDto> findAll() {
        return repository.findAll()
                        .stream()
                        .map(this::toDto)
                        .collect(Collectors.toList());
    }
    
    public ExampleDto create(ExampleDto dto) {
        Example entity = toEntity(dto);
        Example saved = repository.save(entity);
        return toDto(saved);
    }
}
```

### Frontend разработка

#### Создание компонента

```jsx
// src/components/features/ExampleComponent.jsx
import React from 'react';
import PropTypes from 'prop-types';
import './ExampleComponent.css';

export default function ExampleComponent({ title, onAction }) {
  return (
    <div className="example-component">
      <h2 className="example-component__title">{title}</h2>
      <button 
        className="example-component__button"
        onClick={onAction}
      >
        Действие
      </button>
    </div>
  );
}

ExampleComponent.propTypes = {
  title: PropTypes.string.isRequired,
  onAction: PropTypes.func
};
```

#### Создание сториса

```jsx
// src/stories/features/ExampleComponent.stories.jsx
import ExampleComponent from '../components/features/ExampleComponent';

export default {
  title: 'AutoRent/Features/ExampleComponent',
  component: ExampleComponent
};

export const Default = {
  args: {
    title: 'Пример компонента',
    onAction: () => console.log('Действие выполнено')
  }
};
```

### Рабочий процесс

1. **Создайте ветку** для новой функции
2. **Разработайте backend** (контроллер, сервис, репозиторий)
3. **Создайте frontend компоненты** с сторисами
4. **Напишите тесты** для backend и frontend
5. **Протестируйте в Storybook** визуально
6. **Создайте Pull Request**

## 🧪 Тестирование

### Backend тестирование

```bash
# Запуск всех тестов
mvn test

# Запуск тестов с покрытием
mvn test jacoco:report

# Интеграционные тесты
mvn verify
```

### Frontend тестирование

```bash
# Unit тесты
npm test

# Визуальные тесты (Storybook)
npm run storybook

# E2E тесты
npm run test:e2e
```

### API тестирование

```bash
# Запуск Postman коллекции
newman run AutoRent.postman_collection.json

# Тестирование через curl
curl -X GET http://localhost:8080/api/cars
```

## 📦 Развертывание

### Локальная сборка

```bash
# Backend
mvn clean package
java -jar target/autorent-0.0.1-SNAPSHOT.jar

# Frontend
npm run build
npx serve -s build
```

### Docker развертывание

```bash
# Сборка образов
docker build -t autorent-backend .
docker build -t autorent-frontend ./autorent-react

# Запуск с docker-compose
docker-compose up -d
```

### Продакшен развертывание

```bash
# Настройка переменных окружения
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:mysql://prod-db:3306/autorent
export DB_USERNAME=autorent
export DB_PASSWORD=secure_password

# Запуск приложения
mvn spring-boot:run
```

## 📖 Документация

- [Руководство по разработке React](./autorent-react/DEVELOPMENT_GUIDE.md)
- [API документация](./docs/api.md)
- [Руководство по развертыванию](./docs/deployment.md)
- [Руководство по тестированию](./docs/testing.md)

## 🤝 Вклад в проект

1. Форкните репозиторий
2. Создайте ветку для новой функции (`git checkout -b feature/amazing-feature`)
3. Закоммитьте изменения (`git commit -m 'Add amazing feature'`)
4. Запушьте в ветку (`git push origin feature/amazing-feature`)
5. Откройте Pull Request

## 📞 Поддержка

- **Issues**: Создавайте issues для багов и предложений
- **Discussions**: Обсуждайте идеи в Discussions
- **Wiki**: Дополнительная документация в Wiki

## 📄 Лицензия

Этот проект лицензирован под MIT License - см. файл [LICENSE](LICENSE) для деталей.

## 🔧 Актуализация данных в облачной БД

### Проделанная работа по корректировке данных

#### Проблема 1: Несоответствие данных между арендами и платежами
В системе AutoRent была выявлена несоответствие данных между арендами и ежедневными платежами в облачной базе данных AWS RDS MySQL.

#### Анализ проблемы
- **Аренда ID 53**: Период 16.06.2025 - 28.06.2025 (13 дней)
- **Должно быть платежей**: 13 (по одному на каждый день)
- **Фактически было**: 2 платежа (25.06 и 28.06)
- **Отсутствовало**: 11 платежей за дни 16.06-24.06 и 26.06-27.06
- **Недостающая сумма**: 16,500.00 ₽

#### Выполненные действия

##### 1. Создание недостающих платежей
```sql
-- Создано 11 новых платежей за недостающие дни
INSERT INTO daily_payments (amount, payment_date, status, account_id, rental_id, created_at, processed_at, notes) 
VALUES 
(1500.00, '2025-06-16', 'PROCESSED', 10, 53, NOW(), NOW(), 'Платеж за день аренды. Средства списаны.'),
(1500.00, '2025-06-17', 'PROCESSED', 10, 53, NOW(), NOW(), 'Платеж за день аренды. Средства списаны.'),
-- ... и так далее для всех 11 дней
```

##### 2. Списание средств со счета
```sql
-- Обновлен баланс счета пользователя
UPDATE accounts SET balance = balance - 16500.00 WHERE user_id = 15;
```

##### 3. Результаты актуализации
- **Всего платежей**: 13 (было 2)
- **Общая сумма**: 19,500.00 ₽ (13 × 1,500.00)
- **Статус**: Все платежи PROCESSED (списаны)
- **Баланс счета**: Корректно уменьшен на 16,500.00 ₽

#### Проблема 2: Некорректная кодировка примечаний (РЕШЕНА ✅)
В поле `notes` таблицы `daily_payments` отображались кракозябры вместо русского текста.

#### Анализ проблемы кодировки
- **Причина**: Данные были вставлены с неправильной кодировкой (Windows-1251 вместо UTF-8)
- **Проявление**: Вместо "Платеж за день аренды. Средства списаны." отображалось "ÐŸÐ»Ð°Ñ‚ÐµÐ¶ Ð·Ð° Ð´ÐµÐ½ÑŒ Ð°Ñ€ÐµÐ½Ð´Ñ‹. Ð¡Ñ€ÐµÐ´ÑÑ‚Ð²Ð° ÑÐ¿Ð¸ÑÐ°Ð½Ñ‹."
- **Затронутые записи**: Платежи для аренд ID 53, 54, 57, 58

#### Исправление кодировки (ВЫПОЛНЕНО ✅)
```sql
-- Временно отключить safe update mode
SET SQL_SAFE_UPDATES = 0;

-- Исправление примечаний для PENDING платежей
UPDATE daily_payments 
SET notes = 'Платеж ожидает обработки.'
WHERE notes LIKE '%ÐŸÐ»Ð°Ñ‚ÐµÐ¶ Ð¾Ð¶Ð¸Ð´Ð°ÐµÑ‚ Ð¾Ð±Ñ€Ð°Ð±Ð¾Ñ‚ÐºÐ¸%';
-- Результат: 13 row(s) affected

-- Исправление примечаний для PROCESSED платежей
UPDATE daily_payments 
SET notes = 'Платеж за день аренды. Средства списаны.'
WHERE notes LIKE '%ÐŸÐ»Ð°Ñ‚ÐµÐ¶ Ð·Ð° Ð´ÐµÐ½ÑŒ Ð°Ñ€ÐµÐ½Ð´Ñ‹. Ð¡Ñ€ÐµÐ´ÑÑ‚Ð²Ð° ÑÐ¿Ð¸ÑÐ°Ð½Ñ‹%';
-- Результат: 15 row(s) affected

-- Вернуть safe update mode
SET SQL_SAFE_UPDATES = 1;
```

#### Результаты исправления кодировки ✅
- **Исправлено PENDING платежей**: 13 из 13
- **Исправлено PROCESSED платежей**: 15 из 15
- **Всего исправлено записей**: 28
- **Записей с кракозябрами**: 0
- **Статус**: Все примечания корректно отображаются на русском языке

#### Команды для проверки данных

##### Подключение к облачной БД
```bash
# Быстрое подключение через Docker
docker run --rm mysql:8.0 mysql -h uf63wl4z2aq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 -e "SHOW TABLES;"
```

##### Проверка платежей по аренде
```bash
# Подсчет платежей
docker run --rm mysql:8.0 mysql -h uf63wl4z2aq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 -e "SELECT COUNT(*) FROM daily_payments WHERE rental_id = 53;"

# Сумма платежей
docker run --rm mysql:8.0 mysql -h uf63wl4z2aq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 -e "SELECT SUM(amount) FROM daily_payments WHERE rental_id = 53;"

# Детали платежей с примечаниями
docker run --rm mysql:8.0 mysql -h uf63wl4z2aq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 -e "SELECT payment_date, amount, status, notes FROM daily_payments WHERE rental_id = 53 ORDER BY payment_date;"
```

##### Проверка баланса счета
```bash
# Баланс пользователя
docker run --rm mysql:8.0 mysql -h uf63wl4z2aq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 -e "SELECT balance, credit_limit FROM accounts WHERE user_id = 15;"
```

#### Уроки и рекомендации

1. **Регулярная проверка данных**: Необходимо периодически проверять соответствие количества платежей количеству дней аренды
2. **Автоматизация**: Следует реализовать автоматическое создание платежей при создании аренды
3. **Валидация**: Добавить проверки целостности данных между таблицами rentals и daily_payments
4. **Мониторинг**: Настроить алерты при несоответствии данных
5. **Кодировка**: Всегда использовать UTF-8 при работе с текстовыми данными
6. **Проверка кодировки**: Регулярно проверять корректность отображения русского текста в базе данных

#### 🚨 КРИТИЧЕСКИ ВАЖНО: Предотвращение проблем с кодировкой

##### Причины возникновения кракозябр:
- **Ручной ввод данных** в MySQL Workbench с неправильной кодировкой
- **Копирование текста** из Windows-приложений (Windows-1251)
- **Неправильные настройки** клиента MySQL
- **Отсутствие явного указания** кодировки при подключении

##### Правильные SQL-запросы для исправления кодировки:
```sql
-- Временно отключить safe update mode
SET SQL_SAFE_UPDATES = 0;

-- Исправление PENDING платежей
UPDATE daily_payments 
SET notes = 'Платеж ожидает обработки.'
WHERE notes LIKE '%ÐŸÐ»Ð°Ñ‚ÐµÐ¶ Ð¾Ð¶Ð¸Ð´Ð°ÐµÑ‚ Ð¾Ð±Ñ€Ð°Ð±Ð¾Ñ‚ÐºÐ¸%';

-- Исправление PROCESSED платежей  
UPDATE daily_payments 
SET notes = 'Платеж за день аренды. Средства списаны.'
WHERE notes LIKE '%ÐŸÐ»Ð°Ñ‚ÐµÐ¶ Ð·Ð° Ð´ÐµÐ½ÑŒ Ð°Ñ€ÐµÐ½Ð´Ñ‹. Ð¡Ñ€ÐµÐ´ÑÑ‚Ð²Ð° ÑÐ¿Ð¸ÑÐ°Ð½Ñ‹%';

-- Вернуть safe update mode
SET SQL_SAFE_UPDATES = 1;
```

##### Рекомендации для предотвращения:
1. **Всегда использовать UTF-8** при подключении к БД
2. **Проверять кодировку** перед массовым вводом данных
3. **Использовать параметризованные запросы** вместо прямого ввода
4. **Настроить MySQL Workbench** на UTF-8 по умолчанию
5. **Тестировать отображение** русского текста после ввода

#### Параметры подключения к облачной БД
- **Хост**: `uf63wl4z2aq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com`
- **Порт**: `3306`
- **База данных**: `cmwz7gjxubq6sk64`
- **Пользователь**: `wm02va8ppexvexe1`
- **Пароль**: `srj7xmugajaa2ww3`
- **Кодировка**: `utf8mb4` (обязательно!)

---

**AutoRent - Современная система аренды автомобилей** 🚗✨

*Разработано с ❤️ для удобства аренды автомобилей*
