# AutoRent React Frontend

Современный React-фронтенд для системы аренды автомобилей AutoRent с интегрированным Storybook для визуального тестирования компонентов.

## 🚀 Быстрый старт

### Предварительные требования
- Node.js 18+ и npm
- Git

### Установка и запуск

1. **Клонируйте репозиторий:**
   ```bash
   git clone <repository-url>
   cd autorent-react
   ```

2. **Установите зависимости:**
   ```bash
   npm install
   ```

3. **Запустите React-приложение:**
   ```bash
   npm start
   ```
   Приложение откроется на http://localhost:3000

4. **Запустите Storybook для визуального тестирования:**
   ```bash
   npm run storybook
   ```
   Storybook откроется на http://localhost:6006

## 📁 Структура проекта

```
autorent-react/
├── public/                 # Статические файлы
├── src/
│   ├── components/         # React-компоненты
│   │   ├── CarCard.jsx    # Карточка автомобиля
│   │   ├── Navbar.jsx     # Навигация
│   │   ├── SearchForm.jsx # Форма поиска
│   │   └── ...
│   ├── stories/           # Storybook сторисы
│   │   ├── CarCard.stories.jsx
│   │   └── ...
│   ├── pages/             # Страницы приложения
│   ├── hooks/             # Кастомные React-хуки
│   ├── utils/             # Утилиты и хелперы
│   ├── styles/            # CSS/SCSS файлы
│   └── App.js             # Главный компонент
├── .storybook/            # Конфигурация Storybook
├── package.json
└── README.md
```

## 🎨 Разработка компонентов

### Создание нового компонента

1. **Создайте файл компонента:**
   ```jsx
   // src/components/MyComponent.jsx
   import React from 'react';
   
   export default function MyComponent({ prop1, prop2 }) {
     return (
       <div className="my-component">
         <h2>{prop1}</h2>
         <p>{prop2}</p>
       </div>
     );
   }
   ```

2. **Создайте сторис для Storybook:**
   ```jsx
   // src/stories/MyComponent.stories.jsx
   import MyComponent from '../components/MyComponent';
   
   export default {
     title: 'AutoRent/MyComponent',
     component: MyComponent,
     parameters: {
       docs: {
         description: {
           component: 'Описание компонента'
         }
       }
     }
   };
   
   export const Default = {
     args: {
       prop1: 'Значение 1',
       prop2: 'Значение 2'
     }
   };
   
   export const Variant = {
     args: {
       prop1: 'Другое значение',
       prop2: 'Еще значение'
     }
   };
   ```

### Визуальное тестирование в Storybook

- **Просмотр компонентов:** Откройте http://localhost:6006
- **Изменение пропсов:** Используйте Controls панель
- **Тестирование состояний:** Создавайте разные варианты в сторисах
- **Адаптивность:** Проверяйте на разных размерах экрана

## 🔧 Доступные скрипты

```bash
# Запуск React-приложения
npm start

# Запуск Storybook
npm run storybook

# Сборка для продакшена
npm run build

# Запуск тестов
npm test

# Линтинг кода
npm run lint
```

## 📋 Компоненты AutoRent

### CarCard
Карточка автомобиля с основной информацией.

**Пропсы:**
- `brand` (string) - Марка автомобиля
- `model` (string) - Модель автомобиля
- `price` (number) - Цена за сутки
- `image` (string) - URL изображения
- `available` (boolean) - Доступность

**Пример использования:**
```jsx
<CarCard 
  brand="Toyota" 
  model="Camry" 
  price={2500}
  image="/images/camry.jpg"
  available={true}
/>
```

### Navbar
Навигационная панель приложения.

### SearchForm
Форма поиска автомобилей с фильтрами.

### CarGrid
Сетка для отображения списка автомобилей.

## 🎯 Принципы разработки

### 1. Компонентный подход
- Каждый компонент должен быть переиспользуемым
- Используйте пропсы для настройки поведения
- Разделяйте логику и представление

### 2. Визуальное тестирование
- Каждый компонент должен иметь сторис в Storybook
- Тестируйте разные состояния и варианты
- Документируйте назначение компонента

### 3. Стилизация
- Используйте CSS-модули или styled-components
- Следуйте дизайн-системе AutoRent
- Обеспечивайте адаптивность

### 4. Производительность
- Используйте React.memo для оптимизации
- Избегайте лишних ре-рендеров
- Оптимизируйте изображения

## 🔗 Интеграция с Backend

### API Endpoints
```javascript
// Базовый URL API
const API_BASE_URL = 'http://localhost:8080/api';

// Получение списка автомобилей
GET /api/cars

// Получение автомобиля по ID
GET /api/cars/{id}

// Создание бронирования
POST /api/bookings

// Получение пользователя
GET /api/users/profile
```

### Пример использования API
```jsx
import { useState, useEffect } from 'react';

function CarList() {
  const [cars, setCars] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch('/api/cars')
      .then(response => response.json())
      .then(data => {
        setCars(data);
        setLoading(false);
      });
  }, []);

  if (loading) return <div>Загрузка...</div>;

  return (
    <div className="car-grid">
      {cars.map(car => (
        <CarCard key={car.id} {...car} />
      ))}
    </div>
  );
}
```

## 🧪 Тестирование

### Unit тесты
```bash
npm test
```

### Визуальные тесты (Storybook)
```bash
npm run storybook
```

### E2E тесты
```bash
npm run test:e2e
```

## 📦 Развертывание

### Локальная сборка
```bash
npm run build
```

### Продакшен сборка
```bash
npm run build:prod
```

### Docker
```bash
docker build -t autorent-react .
docker run -p 3000:3000 autorent-react
```

## 🤝 Вклад в проект

1. Форкните репозиторий
2. Создайте ветку для новой функции
3. Создайте компонент и его сторис
4. Протестируйте в Storybook
5. Создайте Pull Request

## 📚 Полезные ссылки

- [React Documentation](https://react.dev/)
- [Storybook Documentation](https://storybook.js.org/)
- [AutoRent Backend API](http://localhost:8080/swagger-ui.html)
- [Design System](link-to-design-system)

## 🐛 Отладка

### Частые проблемы

1. **Storybook не запускается:**
   ```bash
   npm run storybook -- --port 6007
   ```

2. **Компонент не отображается в Storybook:**
   - Проверьте импорты в сторисе
   - Убедитесь, что файл сохранен
   - Перезапустите Storybook

3. **API не отвечает:**
   - Проверьте, что backend запущен на порту 8080
   - Проверьте CORS настройки

## 📞 Поддержка

При возникновении проблем:
1. Проверьте консоль браузера
2. Проверьте логи Storybook
3. Создайте Issue в репозитории

---

**Happy coding! 🚗✨** 