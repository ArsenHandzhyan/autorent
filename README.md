# 🚗 AutoRent - Система аренды автомобилей

**Единая система аренды автомобилей** с полным стеком технологий: Spring Boot backend, React frontend, облачная база данных AWS RDS и современные инструменты разработки.

## 📋 Содержание

- [🎯 Обзор проекта](#обзор-проекта)
- [🏗️ Архитектура](#архитектура)
- [🛠️ Технологический стек](#технологический-стек)
- [🚀 Быстрый старт](#быстрый-старт)
- [🗄️ База данных](#база-данных)
- [📁 Структура проекта](#структура-проекта)
- [🔧 Разработка](#разработка)
- [🧪 Тестирование](#тестирование)
- [📱 Мобильная адаптивность](#мобильная-адаптивность)
- [🚀 Развертывание](#развертывание)
- [📚 Документация](#документация)

## 🎯 Обзор проекта

AutoRent - это **полнофункциональная система аренды автомобилей**, объединяющая:

- **🎯 Backend**: Spring Boot приложение с REST API
- **🎨 Frontend**: React приложение с современным UI
- **📱 Мобильная адаптивность**: Mobile-First дизайн
- **☁️ Облачная БД**: AWS RDS MySQL с реальными данными
- **📖 Документация**: Подробные гайды и API документация
- **🧪 Тестирование**: Unit, Integration и Visual тесты

### ✨ Основные функции

#### 👤 Для пользователей:
- 🔐 Регистрация и авторизация с верификацией
- 🔍 Поиск и фильтрация автомобилей
- 📅 Бронирование с выбором дат
- 💳 Онлайн оплата и управление счетами
- 📊 Личный кабинет с историей аренд
- ⭐ Система отзывов и рейтингов
- 📱 **Полная мобильная поддержка**

#### 👨‍💼 Для администраторов:
- 🎛️ Адаптивная панель управления
- 🚗 Управление автопарком (CRUD)
- 👥 Управление пользователями и правами
- ✅ Подтверждение/отмена аренд
- 📈 Финансовая аналитика и отчеты
- 🔔 Система уведомлений
- 📱 **Оптимизация для планшетов**

## 🏗️ Архитектура

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   React App     │    │  Spring Boot    │    │   AWS RDS       │
│   (Frontend)    │◄──►│   (Backend)     │◄──►│   (Database)    │
│                 │    │                 │    │                 │
│ • Components    │    │ • Controllers   │    │ • Users         │
│ • Pages         │    │ • Services      │    │ • Cars          │
│ • Storybook     │    │ • Repositories  │    │ • Bookings      │
│ • Mobile UI     │    │ • Security      │    │ • Payments      │
│ • Responsive    │    │ • Validation    │    │ • Analytics     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🛠️ Технологический стек

### 🎯 Backend (Spring Boot)
- **Framework**: Spring Boot 3.x
- **Security**: Spring Security + JWT
- **Database**: Spring Data JPA + MySQL (AWS RDS)
- **Validation**: Bean Validation
- **Documentation**: Swagger/OpenAPI
- **Build Tool**: Maven
- **Template Engine**: Thymeleaf

### 🎨 Frontend (React)
- **Framework**: React 18
- **Build Tool**: Create React App
- **Styling**: CSS Modules + BEM
- **Testing**: Jest + React Testing Library
- **Visual Testing**: Storybook
- **Package Manager**: npm
- **Mobile**: Responsive Design

### 🗄️ База данных
- **Primary**: AWS RDS MySQL 8.0
- **Backup**: Local MySQL (для разработки)
- **Migration**: Flyway
- **Connection Pool**: HikariCP

### 🧪 Тестирование
- **Unit Tests**: JUnit 5 + Mockito
- **Integration Tests**: TestContainers
- **Frontend Tests**: Jest + React Testing Library
- **Visual Tests**: Storybook
- **API Tests**: Postman Collections

### 🚀 DevOps
- **Containerization**: Docker + Docker Compose
- **CI/CD**: GitHub Actions
- **Monitoring**: Spring Boot Actuator
- **Logging**: Logback

## 🚀 Быстрый старт

### 📋 Предварительные требования

- **Java**: 17+
- **Node.js**: 18+
- **Maven**: 3.6+
- **Git**: Последняя версия
- **Docker**: (опционально)

### 🎯 1. Клонирование репозитория

```bash
git clone <repository-url>
cd autorent
```

### 🎯 2. Запуск Backend (с облачной базой данных)

```bash
# Перейдите в корневую папку проекта
cd autorent

# Запустите Spring Boot приложение с профилем dev (облачная БД)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Или запустите через JAR файл
java -jar target/autorent-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

**✅ Backend будет доступен на http://localhost:8080**

### 🔐 Данные для входа в систему:

| Роль | Email | Пароль |
|------|-------|--------|
| **👨‍💼 Администратор** | `admin@autorent.com` | `admin123` |
| **👤 Пользователь** | `user@autorent.com` | `user123` |

### 🐳 3. Запуск через Docker (опционально)

```bash
# Запуск с облачной базой данных (профиль dev)
docker-compose up --build

# Остановка
docker-compose down
```

### 🎨 4. Запуск Frontend

```bash
# Перейдите в папку React приложения
cd autorent-react

# Установите зависимости
npm install

# Запустите React приложение
npm start
```

**✅ Frontend будет доступен на http://localhost:3000**

### 📖 5. Запуск Storybook

```bash
# В папке autorent-react
npm run storybook
```

**✅ Storybook будет доступен на http://localhost:6006**

## 🗄️ База данных

### ☁️ Облачная база данных (основная)

**AWS RDS MySQL:**
- **🌐 Хост**: `uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com`
- **🔌 Порт**: 3306
- **🗃️ База данных**: `cmwz7gjxubq6sk64`
- **👤 Пользователь**: `wm02va8ppexvexe1`
- **🔒 SSL**: Включен
- **🌍 Регион**: eu-west-1

### 📊 Статистика данных

- **💰 Платежи**: 17 записей (13 PENDING, 4 PROCESSED)
- **🚗 Автомобили**: Полный автопарк
- **👥 Пользователи**: Зарегистрированные пользователи
- **📅 Аренды**: Активные и завершенные
- **💳 Счета**: Финансовые операции

### 🔧 Доступные профили

| Профиль | Сервер | База данных | Порт | Назначение | Статус |
|---------|--------|-------------|------|------------|--------|
| **dev** | AWS RDS MySQL | `cmwz7gjxubq6sk64` | 8080 | **🎯 Разработка (основной)** | ✅ Активен |
| **local** | `localhost:3306` | `autorent` | 8080 | Локальная разработка | 🔧 Резервный |
| **test** | `test-mysql-server:3306` | `autorent_test` | 8081 | Тестирование | 🔧 Резервный |
| **staging** | `staging-mysql-server:3306` | `autorent_staging` | 8080 | Предпродакшн | 🔧 Резервный |
| **prod** | AWS RDS MySQL | `cmwz7gjxubq6sk64` | 8080 | Продакшн | 🔧 Резервный |

## 📁 Структура проекта

```
autorent/
├── 📁 src/main/java/ru/anapa/autorent/
│   ├── 📁 config/          # Конфигурации Spring
│   ├── 📁 controller/      # REST контроллеры
│   ├── 📁 dto/            # Data Transfer Objects
│   ├── 📁 entity/         # JPA сущности
│   ├── 📁 repository/     # Репозитории данных
│   ├── 📁 service/        # Бизнес-логика
│   └── 📁 util/           # Утилиты
├── 📁 src/main/resources/
│   ├── 📁 static/
│   │   ├── 📁 css/
│   │   │   └── main.css        # 🎨 ЕДИНСТВЕННЫЙ CSS файл
│   │   ├── 📁 js/
│   │   │   └── main.js         # 📜 ЕДИНСТВЕННЫЙ JS файл
│   │   └── 📁 images/
│   ├── 📁 templates/      # Thymeleaf шаблоны
│   └── application-*.properties
├── 📁 autorent-react/     # React frontend
├── 📁 autorent-nextjs/    # Next.js frontend
├── 📁 onlook/            # Дополнительные инструменты
├── 📁 database/          # Миграции и скрипты БД
├── 📁 scripts/           # Скрипты запуска
├── 📁 docs/              # Документация
├── 📁 logs/              # Логи приложения
├── 📁 uploads/           # Загруженные файлы
├── 📄 README.md          # 📚 ЕДИНСТВЕННАЯ документация
├── 📄 PRODUCTIVE_PROMPT.md # 🎯 ЕДИНСТВЕННЫЙ промт
├── 📄 pom.xml            # Maven конфигурация
├── 📄 docker-compose.yml # Docker конфигурация
└── 📄 Dockerfile         # Docker образ
```

## 🔧 Разработка

### 🎯 Основные принципы

1. **📝 Обновление README.md** - ПРИ КАЖДОМ изменении
2. **🎨 Единый CSS файл** - `src/main/resources/static/css/main.css`
3. **📜 Единый JS файл** - `src/main/resources/static/js/main.js`
4. **🗄️ Все операции через БД** - Полная история и логирование
5. **📱 Mobile-First дизайн** - Адаптивность для всех устройств

### 🏗️ Архитектурные принципы

#### Backend (Spring Boot):
- **Слоистая архитектура**: Controller → Service → Repository → Entity
- **DTO паттерн** для передачи данных между слоями
- **Валидация** на всех уровнях (Entity, DTO, Controller)
- **Обработка исключений** с кастомными error responses
- **Логирование** всех бизнес-операций
- **Транзакционность** для критических операций

#### Frontend (Thymeleaf + Bootstrap):
- **Семантическая разметка** HTML5
- **Bootstrap 5** для responsive дизайна
- **Thymeleaf фрагменты** для переиспользования компонентов
- **Формы с валидацией** на клиенте и сервере
- **AJAX запросы** для динамического обновления
- **Mobile-First подход** в дизайне

### 📝 Правила именования

#### Java:
- **Классы**: PascalCase (UserService, CarController)
- **Методы**: camelCase (findUserById, saveCar)
- **Переменные**: camelCase (userName, carBrand)
- **Константы**: UPPER_SNAKE_CASE (MAX_RENTAL_DAYS)

#### CSS:
- **Классы**: kebab-case (user-profile, car-card)
- **ID**: camelCase (userProfile, carCard)
- **Переменные**: kebab-case (--primary-color, --font-size)

#### JavaScript:
- **Функции**: camelCase (updateCarStatus, validateForm)
- **Переменные**: camelCase (carData, userInfo)
- **Константы**: UPPER_SNAKE_CASE (API_BASE_URL)

## 🧪 Тестирование

### 🎯 Типы тестов

1. **Unit Tests** (JUnit 5 + Mockito)
2. **Integration Tests** (TestContainers)
3. **Frontend Tests** (Jest + React Testing Library)
4. **Visual Tests** (Storybook)
5. **API Tests** (Postman Collections)

### 🚀 Запуск тестов

```bash
# Backend тесты
mvn test

# Frontend тесты
cd autorent-react
npm test

# Storybook
npm run storybook
```

## 📱 Мобильная адаптивность

### 🎯 Mobile-First принципы

- **📱 Mobile-First дизайн** - сначала для мобильных устройств
- **📐 Адаптивная сетка** - Bootstrap Grid System
- **👆 Оптимизация для сенсорного управления**:
  - Минимальный размер кнопок: 44x44px
  - Достаточные отступы между элементами
  - Увеличенные области нажатия
- **🖼️ Оптимизация изображений**:
  - Responsive images с `srcset` и `sizes`
  - Ленивая загрузка изображений
  - Оптимизация размера файлов
- **📝 Типографика**:
  - Минимальный размер шрифта: 16px
  - Адаптивные размеры заголовков
  - Достаточный контраст текста

### 📱 Поддерживаемые устройства

- **📱 Мобильные телефоны** (320px - 768px)
- **📱 Планшеты** (768px - 1024px)
- **💻 Десктопы** (1024px+)

## 🚀 Развертывание

### 🐳 Docker развертывание

```bash
# Сборка и запуск
docker-compose up --build

# Только backend
docker build -t autorent-backend .
docker run -p 8080:8080 autorent-backend
```

### ☁️ Облачное развертывание

- **AWS**: EC2 + RDS + S3
- **Heroku**: Container deployment
- **Google Cloud**: Cloud Run + Cloud SQL
- **Azure**: App Service + Azure SQL

### 📊 Мониторинг

- **Spring Boot Actuator** для метрик
- **Logback** для логирования
- **Health checks** для проверки состояния
- **Performance monitoring** для производительности

## 📚 Документация

### 📖 Основные документы

- **📄 README.md** - Единая документация проекта
- **🎯 PRODUCTIVE_PROMPT.md** - Единый промт для разработки
- **📋 API Documentation** - Swagger/OpenAPI
- **📱 Mobile Guidelines** - Руководство по мобильной адаптивности

### 🔗 Полезные ссылки

- **🌐 Backend API**: http://localhost:8080
- **🎨 Frontend**: http://localhost:3000
- **📖 Storybook**: http://localhost:6006
- **📋 Swagger**: http://localhost:8080/swagger-ui.html

### 📞 Поддержка

- **🐛 Issues**: GitHub Issues
- **📧 Email**: support@autorent.com
- **💬 Chat**: Discord/Slack

---

## 🎯 Статус проекта

**✅ Активно разрабатывается**
**✅ Облачная база данных подключена**
**✅ Мобильная адаптивность реализована**
**✅ Полный функционал системы**

---

**🚀 AutoRent - Современная система аренды автомобилей с полной мобильной поддержкой!**
