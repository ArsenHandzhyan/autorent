# 🚗 AutoRent - Система аренды автомобилей

Современная система аренды автомобилей с Spring Boot backend и React frontend, включающая визуальное тестирование компонентов через Storybook.

## 📋 Содержание

- [Обзор проекта](#обзор-проекта)
- [Архитектура](#архитектура)
- [Технологический стек](#технологический-стек)
- [Быстрый старт](#быстрый-старт)
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

---

**AutoRent - Современная система аренды автомобилей** 🚗✨

*Разработано с ❤️ для удобства аренды автомобилей*
