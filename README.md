# 🚗 AutoRent - Система аренды автомобилей

Современная система аренды автомобилей с Spring Boot backend и React frontend, включающая визуальное тестирование компонентов через Storybook.

## 📋 Содержание

- [Обзор проекта](#обзор-проекта)
- [Архитектура](#архитектура)
- [Технологический стек](#технологический-стек)
- [Быстрый старт](#быстрый-старт)
- [Предварительный просмотр шаблонов](#предварительный-просмотр-шаблонов)
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

## 👁️ Предварительный просмотр шаблонов

Система AutoRent включает встроенный сервер предварительного просмотра Thymeleaf шаблонов, который позволяет просматривать и тестировать HTML шаблоны без запуска основного Spring Boot приложения.

### 🎨 Подключение общих стилей и скриптов

Сервер предварительного просмотра **точно воспроизводит реальный фронтенд** за счет подключения общих унифицированных файлов:

#### CSS файлы
- **Bootstrap 5**: `https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css`
- **FontAwesome**: `https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css`
- **Единый файл стилей**: `/css/main.css` (81KB, 3676 строк)

#### JavaScript файлы
- **Bootstrap 5 JS**: `https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js`
- **Единый файл скриптов**: `/js/main.js` (96KB, 2617 строк)

#### Тестирование подключения
Для проверки корректности загрузки стилей и скриптов доступен специальный тест:
- **URL**: http://localhost:3001/test-styles
- **Функции**: Проверка Bootstrap, FontAwesome, кастомных стилей и JavaScript

#### Как это работает
1. **Статические файлы**: Сервер обслуживает папку `src/main/resources/static/` как статические ресурсы
2. **Thymeleaf обработка**: Выражения `th:href="@{/css/main.css}"` преобразуются в `href="/css/main.css"`
3. **Единые стили**: Все шаблоны используют один файл `main.css` с полным набором стилей
4. **Единые скрипты**: Все шаблоны используют один файл `main.js` с полным функционалом
5. **Точное воспроизведение**: Предварительный просмотр идентичен реальному приложению

#### Преимущества
- ✅ **Единообразие**: Все шаблоны используют одинаковые стили и скрипты
- ✅ **Актуальность**: Изменения в `main.css` и `main.js` сразу видны в превью
- ✅ **Производительность**: Кэширование общих файлов браузером
- ✅ **Поддержка**: Легко поддерживать единый стиль во всем приложении
- ✅ **Тестирование**: Можно тестировать новые стили без запуска Spring Boot

### 🚀 Быстрый запуск

#### Windows
```bash
# Запуск предварительного просмотра
start-preview.bat
```

#### Linux/Mac
```bash
# Сделать скрипт исполняемым (только при первом запуске)
chmod +x start-preview.sh

# Запуск предварительного просмотра
./start-preview.sh
```

#### Ручной запуск
```bash
# Установка зависимостей (только при первом запуске)
npm install

# Запуск сервера
npm run preview
```

### 📍 Доступ к предварительному просмотру

После запуска сервер будет доступен по адресу: **http://localhost:3001**

### 🎯 Возможности

#### 1. Автоматическое сканирование шаблонов
- Автоматическое обнаружение всех `.html` файлов в `src/main/resources/templates/`
- Поддержка вложенных папок (admin/, cars/, auth/, etc.)
- Отображение структуры файлов

#### 2. Мок-данные для тестирования
- Предустановленные мок-данные для каждого типа шаблона
- Реалистичные данные пользователей, автомобилей, аренд
- Возможность просмотра используемых данных

#### 3. Обработка Thymeleaf выражений
- Автоматическая замена `${...}` выражений на мок-данные
- Удаление Thymeleaf атрибутов (`th:*`)
- Обработка ссылок `@{...}` с сохранением путей к статическим файлам

#### 4. Интерактивный интерфейс
- Веб-интерфейс для выбора шаблонов
- Панель с мок-данными
- Кнопки управления (обновить, скрыть/показать данные)
- Тест подключения стилей и скриптов

#### 5. Точное воспроизведение фронтенда
- Подключение всех общих CSS и JS файлов
- Использование единых стилей `main.css`
- Использование единых скриптов `main.js`
- Полная совместимость с реальным приложением

### 📊 Доступные мок-данные

| Шаблон | Данные | Описание |
|--------|--------|----------|
| `main` | `user`, `rentals` | Личный кабинет пользователя |
| `home` | `featuredCars`, `stats` | Главная страница |
| `cars` | `cars`, `filters` | Каталог автомобилей |
| `auth` | `error`, `success` | Страницы авторизации |
| `admin` | `stats`, `recentRentals` | Админ панель |

### 🔧 Настройка мок-данных

Мок-данные можно настроить в файле `preview-server.js`:

```javascript
const mockData = {
    main: {
        user: {
            firstName: "Иван",
            lastName: "Петров",
            email: "ivan.petrov@example.com",
            phone: "+7 (999) 123-45-67",
            registrationDate: "15.01.2025"
        },
        // ... другие данные
    },
    // ... другие шаблоны
};
```

### 📁 Структура файлов предварительного просмотра

```
autorent/
├── preview-server.js          # Node.js сервер предварительного просмотра
├── preview-templates.html     # Альтернативный HTML-превью
├── package.json              # Node.js зависимости
├── start-preview.bat         # Скрипт запуска для Windows
├── start-preview.sh          # Скрипт запуска для Linux/Mac
└── src/main/resources/
    ├── static/               # Статические файлы (CSS, JS, изображения)
    └── templates/            # Thymeleaf шаблоны
        ├── main.html         # Личный кабинет
        ├── home.html         # Главная страница
        ├── cars/             # Шаблоны автомобилей
        ├── auth/             # Шаблоны авторизации
        └── admin/            # Админ шаблоны
```

### 🎨 Особенности интерфейса

#### Главная страница сервера
- Список всех доступных шаблонов
- Информация о сервере (порт, время запуска)
- Список доступных мок-данных
- Карточки с быстрым доступом к каждому шаблону

#### Страница предварительного просмотра
- Заголовок с информацией о шаблоне
- Панель с мок-данными (скрываемая)
- Кнопки управления
- Полноценный рендеринг шаблона с CSS и JS

### 🔍 Примеры использования

#### Просмотр личного кабинета
1. Запустите сервер: `./start-preview.sh`
2. Откройте: http://localhost:3001
3. Выберите "main" (Личный кабинет)
4. Просмотрите рендеринг с мок-данными пользователя

#### Тестирование каталога автомобилей
1. Перейдите к: http://localhost:3001/preview/cars
2. Просмотрите список автомобилей
3. Проверьте работу фильтров
4. Изучите используемые мок-данные

#### Разработка новых шаблонов
1. Создайте новый `.html` файл в `templates/`
2. Добавьте мок-данные в `preview-server.js`
3. Обновите страницу сервера
4. Протестируйте новый шаблон

### 🛠️ Разработка и отладка

#### Режим разработки с автоперезагрузкой
```bash
# Установка nodemon для автоперезагрузки
npm install -g nodemon

# Запуск в режиме разработки
npm run preview:dev
```

#### API endpoints
```bash
# Список всех шаблонов
GET /api/templates

# Мок-данные для конкретного шаблона
GET /api/mock-data/{template}

# Предварительный просмотр шаблона
GET /preview/{template}
```

#### Логирование
Сервер выводит подробную информацию в консоль:
- Список найденных шаблонов
- Доступные мок-данные
- Ошибки загрузки файлов
- Статистика запросов

### 🔒 Безопасность

- Сервер работает только на localhost
- Нет доступа к реальной базе данных
- Используются только мок-данные
- Предназначен только для разработки

### 📝 Примечания

- Сервер предварительного просмотра не заменяет полноценное тестирование
- Некоторые сложные Thymeleaf выражения могут не обрабатываться корректно
- Для полного тестирования используйте основное Spring Boot приложение
- Мок-данные следует обновлять при изменении структуры данных

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

#### 📋 Полная диагностика базы данных

Для выявления и исправления всех возможных ошибок в арендах и платежах создан подробный отчет диагностики:

**📄 Файл**: `DATABASE_DIAGNOSTIC_REPORT.md`

**🔍 Что проверяется:**
- Общая статистика по всем таблицам
- Некорректные даты аренд (start_date >= end_date)
- Некорректные суммы (total_cost <= 0)
- Некорректные статусы
- Несоответствие количества дней аренды и платежей
- Несоответствие сумм аренды и платежей
- Проблемы с кодировкой (кракозябры)
- Целостность данных (orphaned записи)

**🛠️ Что исправляется:**
- Кодировка в текстовых полях
- Создание недостающих платежей
- Исправление некорректных дат
- Исправление некорректных сумм
- Восстановление связей между таблицами

**📊 Ожидаемые результаты после исправления:**
- ✅ Все аренды имеют корректные даты (start_date < end_date)
- ✅ Все аренды имеют корректную стоимость (total_cost > 0)
- ✅ Все аренды имеют корректный статус
- ✅ Количество платежей = количество дней аренды
- ✅ Сумма платежей = стоимость аренды
- ✅ Все примечания отображаются корректно на русском языке
- ✅ Нет orphaned записей (все связи целы)

**🚀 Инструкции по выполнению:**
1. Открыть файл `DATABASE_DIAGNOSTIC_REPORT.md`
2. Выполнить диагностические SQL-запросы
3. Проанализировать результаты
4. Выполнить исправляющие SQL-запросы
5. Повторить диагностику для проверки

**⚠️ Важно:** Выполнять запросы по одному, делать backup перед массовыми изменениями и проверять результаты после каждого исправления.

#### 📄 Готовые SQL файлы для диагностики

Для удобства выполнения диагностики созданы готовые SQL файлы:

**🔍 `database_queries.sql`** - Полная диагностика (18 запросов)
- Общая статистика по всем таблицам
- Проверка аренд на некорректные данные
- Проверка платежей на некорректные данные
- Проверка соответствия аренд и платежей
- Проверка целостности данных
- Проверка кодировки
- Статистика по статусам
- Итоговая сводка проблем

**⚡ `quick_check.sql`** - Быстрая диагностика (5 запросов)
- Быстрая статистика
- Критические ошибки аренд
- Критические ошибки платежей
- Несоответствие дней и платежей
- Кракозябры в примечаниях

**🛠️ `fix_queries.sql`** - Готовые исправления
- Исправление кодировки в примечаниях
- Создание недостающих платежей
- Исправление некорректных дат
- Исправление некорректных сумм
- Исправление некорректных статусов
- Удаление orphaned записей
- Проверка результатов исправлений

#### 🎯 Рекомендуемый порядок выполнения:

1. **Быстрая проверка**: Выполнить `quick_check.sql`
2. **Полная диагностика**: Выполнить `database_queries.sql`
3. **Анализ результатов**: Определить конкретные проблемы
4. **Исправление**: Выполнить соответствующие запросы из `fix_queries.sql`
5. **Проверка**: Повторить диагностику для подтверждения исправлений

#### 🔧 Параметры подключения к облачной БД
- **Хост**: `uf63wl4z2aq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com`
- **Порт**: `3306`
- **База данных**: `cmwz7gjxubq6sk64`
- **Пользователь**: `wm02va8ppexvexe1`
- **Пароль**: `srj7xmugajaa2ww3`
- **Кодировка**: `utf8mb4` (обязательно!)

#### 🚨 РЕЗУЛЬТАТЫ ДИАГНОСТИКИ С ОБНОВЛЕННЫМИ ПАРАМЕТРАМИ

**Статус**: ❌ ПРОБЛЕМА ПОДКЛЮЧЕНИЯ СОХРАНЯЕТСЯ

**Результаты проверки подключения:**
- ❌ Ping тест: `Ping request could not find host`
- ❌ DNS разрешение (Google DNS): `Non-existent domain`
- ❌ Docker подключение: `Unknown MySQL server host (-2)`
- ❌ PowerShell скрипт: `ParserError: Unexpected token`

**Анализ проблемы:**
Возможные причины недоступности облачной БД:
1. **RDS инстанс удален или остановлен** - инстанс `cmwz7gjxubq6sk64` может быть удален из AWS
2. **Проблемы с Security Groups** - доступ к порту 3306 может быть заблокирован
3. **Изменение параметров подключения** - пароль или пользователь могли быть изменены
4. **Сетевые проблемы** - проблемы с DNS серверами или файрволом

**Проверенные конфигурационные файлы:**
- ✅ `application-dev.properties` - параметры подключения актуальны
- ✅ `application-prod.properties` - параметры подключения актуальны
- **Созданные инструменты для диагностики:**
- ✅ `quick_check.sql` - быстрая диагностика (5 запросов)
- ✅ `database_queries.sql` - полная диагностика (18 запросов)
- ✅ `fix_queries.sql` - готовые исправления
- ✅ `MANUAL_DIAGNOSTIC_INSTRUCTIONS.md` - подробные инструкции
- ✅ `CLOUD_DB_CONNECTION_ISSUE.md` - анализ проблемы подключения
- ✅ `DIAGNOSTIC_RESULTS.md` - итоговый отчет
- ✅ `UPDATED_DIAGNOSTIC_RESULTS.md` - отчет с обновленными параметрами

**Рекомендуемые действия:**
1. **Немедленно**: Проверить AWS Console и статус RDS инстанса
2. **Проверить статус RDS**: Убедиться, что инстанс запущен и доступен
3. **Получить актуальные параметры**: Скопировать новый endpoint и проверить учетные данные
4. **Альтернативная диагностика**: Использовать локальную БД для тестирования

**Подробная информация:**
- 📄 `UPDATED_DIAGNOSTIC_RESULTS.md` - полный отчет с обновленными параметрами
- 📄 `CLOUD_DB_CONNECTION_ISSUE.md` - анализ проблемы подключения
- 📄 `MANUAL_DIAGNOSTIC_INSTRUCTIONS.md` - инструкции по ручному выполнению

---

**AutoRent - Современная система аренды автомобилей** 🚗✨

*Разработано с ❤️ для удобства аренды автомобилей*

### 👥 Проверка пользователей в облачной базе данных

#### Быстрая проверка через Docker

**Статистика пользователей:**
```bash
# Общая статистика
docker run --rm mysql:8.0 mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 cmwz7gjxubq6sk64 -e "SELECT COUNT(*) as total_users, COUNT(CASE WHEN enabled = 1 THEN 1 END) as active_users FROM users;"

# Список всех пользователей
docker run --rm mysql:8.0 mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 cmwz7gjxubq6sk64 -e "SELECT id, email, first_name, last_name, enabled, registration_date FROM users ORDER BY registration_date DESC;"

# Администраторы системы
docker run --rm mysql:8.0 mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 cmwz7gjxubq6sk64 -e "SELECT u.id, u.email, u.first_name, u.last_name, GROUP_CONCAT(r.name) as roles FROM users u JOIN user_roles ur ON u.id = ur.user_id JOIN roles r ON ur.role_id = r.id WHERE r.name = 'ROLE_ADMIN' GROUP BY u.id, u.email, u.first_name, u.last_name;"
```

#### Готовые скрипты проверки

**Windows:**
```bash
# Перейти в папку скриптов
cd scripts

# Запустить проверку пользователей
check_cloud_users.bat
```

**Linux/Mac:**
```bash
# Перейти в папку скриптов
cd scripts

# Сделать скрипт исполняемым
chmod +x check_cloud_users.sh

# Запустить проверку пользователей
./check_cloud_users.sh
```

#### SQL скрипты для детальной проверки

**Быстрая проверка** (`database/quick_check_users.sql`):
```sql
-- Общая статистика пользователей
SELECT COUNT(*) as total_users, COUNT(CASE WHEN enabled = 1 THEN 1 END) as active_users FROM users;

-- Последние зарегистрированные пользователи
SELECT id, email, first_name, last_name, registration_date FROM users ORDER BY registration_date DESC LIMIT 10;

-- Администраторы системы
SELECT u.id, u.email, u.first_name, u.last_name FROM users u JOIN user_roles ur ON u.id = ur.user_id JOIN roles r ON ur.role_id = r.id WHERE r.name = 'ROLE_ADMIN';
```

**Полная проверка** (`database/check_users_cloud.sql`):
- Статистика по ролям
- Анализ активности пользователей
- Проверка данных пользователей
- Связи с другими таблицами
- Статистика по датам регистрации
- Проверка дубликатов

#### Полезные запросы для анализа пользователей

```sql
-- Пользователи с высоким балансом
SELECT u.id, u.email, u.first_name, u.last_name, a.balance 
FROM users u LEFT JOIN accounts a ON u.id = a.user_id 
WHERE a.balance > 100000 ORDER BY a.balance DESC;

-- Пользователи с арендами
SELECT u.id, u.email, u.first_name, u.last_name, COUNT(r.id) as rental_count 
FROM users u LEFT JOIN rentals r ON u.id = r.user_id 
GROUP BY u.id, u.email, u.first_name, u.last_name 
HAVING rental_count > 0 ORDER BY rental_count DESC;

-- Пользователи без входов более 30 дней
SELECT u.id, u.email, u.first_name, u.last_name, u.last_login_date 
FROM users u WHERE u.last_login_date < DATE_SUB(NOW(), INTERVAL 30 DAY) AND u.enabled = 1;

-- Проблемные записи пользователей
SELECT id, email, first_name, last_name, phone 
FROM users WHERE email NOT LIKE '%@%' OR first_name = '' OR last_name = '' OR phone = '';
```

#### Отчеты о проверке

**📄 Полный отчет**: `CLOUD_USERS_CHECK_REPORT.md`
- Общая статистика пользователей
- Список всех пользователей с деталями
- Анализ ролей и прав доступа
- Финансовая информация по аккаунтам
- Активность аренд
- Выявленные проблемы и рекомендации

**📊 Последние результаты проверки (29.12.2024):**
- **Всего пользователей**: 13
- **Активных пользователей**: 13
- **Администраторов**: 4
- **Пользователей с арендами**: 2
- **Пользователей с высоким балансом**: 5

📖 **Подробная документация**: [MSP_SERVERS.md](./docs/MSP_SERVERS.md)
