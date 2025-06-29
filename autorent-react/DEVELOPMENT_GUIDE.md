# 🚗 AutoRent React Development Guide

Подробное руководство по разработке фронтенда системы аренды автомобилей AutoRent с использованием React и Storybook.

## 📋 Содержание

1. [Архитектура проекта](#архитектура-проекта)
2. [Рабочий процесс разработки](#рабочий-процесс-разработки)
3. [Создание компонентов](#создание-компонентов)
4. [Storybook и визуальное тестирование](#storybook-и-визуальное-тестирование)
5. [Стилизация и дизайн](#стилизация-и-дизайн)
6. [Интеграция с API](#интеграция-с-api)
7. [Тестирование](#тестирование)
8. [Производительность](#производительность)
9. [Развертывание](#развертывание)

## 🏗️ Архитектура проекта

### Структура папок
```
autorent-react/
├── public/                    # Статические ресурсы
│   ├── images/               # Изображения
│   ├── icons/                # Иконки
│   └── index.html            # Главный HTML файл
├── src/
│   ├── components/           # Переиспользуемые компоненты
│   │   ├── ui/              # Базовые UI компоненты
│   │   │   ├── Button.jsx
│   │   │   ├── Input.jsx
│   │   │   └── Modal.jsx
│   │   ├── layout/          # Компоненты макета
│   │   │   ├── Navbar.jsx
│   │   │   ├── Footer.jsx
│   │   │   └── Sidebar.jsx
│   │   └── features/        # Компоненты функций
│   │       ├── CarCard.jsx
│   │       ├── SearchForm.jsx
│   │       └── BookingForm.jsx
│   ├── pages/               # Страницы приложения
│   │   ├── Home.jsx
│   │   ├── CarList.jsx
│   │   ├── CarDetail.jsx
│   │   └── Profile.jsx
│   ├── hooks/               # Кастомные React хуки
│   │   ├── useCars.js
│   │   ├── useAuth.js
│   │   └── useLocalStorage.js
│   ├── services/            # API сервисы
│   │   ├── api.js
│   │   ├── carService.js
│   │   └── authService.js
│   ├── utils/               # Утилиты и хелперы
│   │   ├── formatters.js
│   │   ├── validators.js
│   │   └── constants.js
│   ├── styles/              # Стили
│   │   ├── globals.css
│   │   ├── components.css
│   │   └── variables.css
│   ├── stories/             # Storybook сторисы
│   │   ├── ui/
│   │   ├── layout/
│   │   └── features/
│   └── App.js               # Главный компонент
├── .storybook/              # Конфигурация Storybook
├── tests/                   # Тесты
└── docs/                    # Документация
```

### Принципы архитектуры

1. **Компонентный подход**: Каждый компонент должен быть независимым и переиспользуемым
2. **Разделение ответственности**: UI компоненты отделены от бизнес-логики
3. **Единообразие**: Все компоненты следуют единым паттернам
4. **Тестируемость**: Каждый компонент должен быть легко тестируемым

## 🔄 Рабочий процесс разработки

### 1. Создание нового компонента

```bash
# 1. Создайте файл компонента
touch src/components/features/NewComponent.jsx

# 2. Создайте сторис для Storybook
touch src/stories/features/NewComponent.stories.jsx

# 3. Создайте тесты
touch tests/components/NewComponent.test.jsx

# 4. Добавьте стили (если нужно)
touch src/styles/components/NewComponent.css
```

### 2. Разработка компонента

1. **Начните с интерфейса** - определите пропсы компонента
2. **Создайте базовую структуру** - JSX разметка
3. **Добавьте стили** - CSS/SCSS
4. **Создайте сторис** - для визуального тестирования
5. **Напишите тесты** - unit тесты
6. **Интегрируйте** - подключите к приложению

### 3. Code Review процесс

1. **Создайте ветку** для новой функции
2. **Разработайте компонент** с тестами и сторисами
3. **Протестируйте в Storybook** - убедитесь, что все варианты работают
4. **Запустите тесты** - `npm test`
5. **Создайте Pull Request** с описанием изменений

## 🧩 Создание компонентов

### Шаблон компонента

```jsx
import React from 'react';
import PropTypes from 'prop-types';
import './ComponentName.css';

/**
 * Описание компонента
 * @param {Object} props - Пропсы компонента
 * @param {string} props.title - Заголовок
 * @param {function} props.onClick - Обработчик клика
 * @returns {JSX.Element} Компонент
 */
export default function ComponentName({ title, onClick, children }) {
  return (
    <div className="component-name">
      <h2 className="component-name__title">{title}</h2>
      <div className="component-name__content">
        {children}
      </div>
      <button 
        className="component-name__button"
        onClick={onClick}
      >
        Действие
      </button>
    </div>
  );
}

// PropTypes для валидации пропсов
ComponentName.propTypes = {
  title: PropTypes.string.isRequired,
  onClick: PropTypes.func,
  children: PropTypes.node
};

// Значения по умолчанию
ComponentName.defaultProps = {
  onClick: () => {},
  children: null
};
```

### Пример: CarCard компонент

```jsx
import React from 'react';
import PropTypes from 'prop-types';
import './CarCard.css';

export default function CarCard({ 
  id, 
  brand, 
  model, 
  year, 
  price, 
  image, 
  available, 
  onBook, 
  onView 
}) {
  const handleBook = () => {
    if (available && onBook) {
      onBook(id);
    }
  };

  const handleView = () => {
    if (onView) {
      onView(id);
    }
  };

  return (
    <div className={`car-card ${!available ? 'car-card--unavailable' : ''}`}>
      <div className="car-card__image">
        <img src={image} alt={`${brand} ${model}`} />
        {!available && (
          <div className="car-card__badge">Недоступен</div>
        )}
      </div>
      
      <div className="car-card__content">
        <h3 className="car-card__title">{brand} {model}</h3>
        <p className="car-card__year">{year} год</p>
        <p className="car-card__price">{price.toLocaleString()} ₽/сутки</p>
      </div>
      
      <div className="car-card__actions">
        <button 
          className="car-card__button car-card__button--view"
          onClick={handleView}
        >
          Подробнее
        </button>
        <button 
          className="car-card__button car-card__button--book"
          onClick={handleBook}
          disabled={!available}
        >
          Забронировать
        </button>
      </div>
    </div>
  );
}

CarCard.propTypes = {
  id: PropTypes.string.isRequired,
  brand: PropTypes.string.isRequired,
  model: PropTypes.string.isRequired,
  year: PropTypes.number.isRequired,
  price: PropTypes.number.isRequired,
  image: PropTypes.string.isRequired,
  available: PropTypes.bool,
  onBook: PropTypes.func,
  onView: PropTypes.func
};

CarCard.defaultProps = {
  available: true,
  onBook: null,
  onView: null
};
```

## 📚 Storybook и визуальное тестирование

### Структура сторисов

```jsx
import React from 'react';
import ComponentName from '../components/ComponentName';

// Метаданные компонента
export default {
  title: 'AutoRent/Features/ComponentName',
  component: ComponentName,
  parameters: {
    docs: {
      description: {
        component: 'Подробное описание компонента и его назначения'
      }
    },
    design: {
      type: 'figma',
      url: 'https://figma.com/file/...'
    }
  },
  argTypes: {
    // Настройка Controls панели
    title: {
      control: 'text',
      description: 'Заголовок компонента'
    },
    onClick: {
      action: 'clicked',
      description: 'Обработчик клика'
    }
  }
};

// Основная история
export const Default = {
  args: {
    title: 'Заголовок по умолчанию',
    children: 'Содержимое компонента'
  }
};

// Вариант с длинным заголовком
export const LongTitle = {
  args: {
    title: 'Очень длинный заголовок, который может не поместиться в одну строку',
    children: 'Содержимое компонента'
  }
};

// Вариант без содержимого
export const Empty = {
  args: {
    title: 'Пустой компонент',
    children: null
  }
};

// Интерактивная история
export const Interactive = {
  args: {
    title: 'Интерактивный компонент',
    children: 'Кликните на кнопку'
  },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const button = canvas.getByRole('button');
    await userEvent.click(button);
  }
};
```

### Пример: CarCard сторис

```jsx
import React from 'react';
import CarCard from '../components/features/CarCard';

export default {
  title: 'AutoRent/Features/CarCard',
  component: CarCard,
  parameters: {
    docs: {
      description: {
        component: 'Карточка автомобиля для отображения в каталоге. Показывает основную информацию об автомобиле и позволяет забронировать его.'
      }
    }
  },
  argTypes: {
    brand: {
      control: 'text',
      description: 'Марка автомобиля'
    },
    model: {
      control: 'text',
      description: 'Модель автомобиля'
    },
    price: {
      control: { type: 'number', min: 0 },
      description: 'Цена за сутки в рублях'
    },
    available: {
      control: 'boolean',
      description: 'Доступность автомобиля'
    },
    onBook: {
      action: 'booked',
      description: 'Обработчик бронирования'
    },
    onView: {
      action: 'viewed',
      description: 'Обработчик просмотра деталей'
    }
  }
};

export const Default = {
  args: {
    id: '1',
    brand: 'Toyota',
    model: 'Camry',
    year: 2022,
    price: 2500,
    image: '/images/cars/camry.jpg',
    available: true
  }
};

export const Expensive = {
  args: {
    id: '2',
    brand: 'BMW',
    model: 'X5',
    year: 2023,
    price: 8000,
    image: '/images/cars/x5.jpg',
    available: true
  }
};

export const Unavailable = {
  args: {
    id: '3',
    brand: 'Mercedes',
    model: 'C-Class',
    year: 2021,
    price: 3500,
    image: '/images/cars/c-class.jpg',
    available: false
  }
};

export const Loading = {
  args: {
    id: '4',
    brand: 'Loading...',
    model: 'Loading...',
    year: 0,
    price: 0,
    image: '/images/placeholder.jpg',
    available: false
  }
};
```

## 🎨 Стилизация и дизайн

### CSS методология

Используем BEM (Block Element Modifier) для организации CSS:

```css
/* Блок */
.car-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 16px;
  background: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

/* Элемент */
.car-card__image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 4px;
}

.car-card__title {
  font-size: 18px;
  font-weight: 600;
  margin: 8px 0;
  color: #333;
}

.car-card__price {
  font-size: 16px;
  color: #007bff;
  font-weight: 500;
}

/* Модификатор */
.car-card--unavailable {
  opacity: 0.6;
  pointer-events: none;
}

.car-card--featured {
  border-color: #007bff;
  box-shadow: 0 4px 8px rgba(0,123,255,0.2);
}
```

### CSS переменные

```css
/* src/styles/variables.css */
:root {
  /* Цвета */
  --color-primary: #007bff;
  --color-secondary: #6c757d;
  --color-success: #28a745;
  --color-danger: #dc3545;
  --color-warning: #ffc107;
  --color-info: #17a2b8;
  
  /* Текст */
  --color-text-primary: #333;
  --color-text-secondary: #666;
  --color-text-muted: #999;
  
  /* Фоны */
  --color-bg-primary: #fff;
  --color-bg-secondary: #f8f9fa;
  --color-bg-dark: #343a40;
  
  /* Границы */
  --color-border: #e0e0e0;
  --color-border-light: #f0f0f0;
  
  /* Тени */
  --shadow-sm: 0 2px 4px rgba(0,0,0,0.1);
  --shadow-md: 0 4px 8px rgba(0,0,0,0.15);
  --shadow-lg: 0 8px 16px rgba(0,0,0,0.2);
  
  /* Типографика */
  --font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  --font-size-xs: 12px;
  --font-size-sm: 14px;
  --font-size-base: 16px;
  --font-size-lg: 18px;
  --font-size-xl: 20px;
  --font-size-2xl: 24px;
  
  /* Отступы */
  --spacing-xs: 4px;
  --spacing-sm: 8px;
  --spacing-md: 16px;
  --spacing-lg: 24px;
  --spacing-xl: 32px;
  
  /* Скругления */
  --border-radius-sm: 4px;
  --border-radius-md: 8px;
  --border-radius-lg: 12px;
  
  /* Анимации */
  --transition-fast: 0.15s ease;
  --transition-normal: 0.3s ease;
  --transition-slow: 0.5s ease;
}
```

### Адаптивный дизайн

```css
/* Мобильные устройства */
@media (max-width: 768px) {
  .car-card {
    padding: 12px;
  }
  
  .car-card__title {
    font-size: 16px;
  }
  
  .car-card__actions {
    flex-direction: column;
    gap: 8px;
  }
}

/* Планшеты */
@media (min-width: 769px) and (max-width: 1024px) {
  .car-card {
    padding: 14px;
  }
}

/* Десктоп */
@media (min-width: 1025px) {
  .car-card:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-lg);
  }
}
```

## 🔗 Интеграция с API

### Настройка API клиента

```javascript
// src/services/api.js
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

class ApiClient {
  constructor() {
    this.baseURL = API_BASE_URL;
  }

  async request(endpoint, options = {}) {
    const url = `${this.baseURL}${endpoint}`;
    const config = {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers
      },
      ...options
    };

    // Добавляем токен авторизации
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    try {
      const response = await fetch(url, config);
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      return await response.json();
    } catch (error) {
      console.error('API request failed:', error);
      throw error;
    }
  }

  // GET запрос
  async get(endpoint) {
    return this.request(endpoint, { method: 'GET' });
  }

  // POST запрос
  async post(endpoint, data) {
    return this.request(endpoint, {
      method: 'POST',
      body: JSON.stringify(data)
    });
  }

  // PUT запрос
  async put(endpoint, data) {
    return this.request(endpoint, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  }

  // DELETE запрос
  async delete(endpoint) {
    return this.request(endpoint, { method: 'DELETE' });
  }
}

export const apiClient = new ApiClient();
```

### Сервисы для работы с данными

```javascript
// src/services/carService.js
import { apiClient } from './api';

export const carService = {
  // Получить список автомобилей
  async getCars(filters = {}) {
    const params = new URLSearchParams(filters);
    return apiClient.get(`/cars?${params}`);
  },

  // Получить автомобиль по ID
  async getCar(id) {
    return apiClient.get(`/cars/${id}`);
  },

  // Создать бронирование
  async createBooking(bookingData) {
    return apiClient.post('/bookings', bookingData);
  },

  // Получить бронирования пользователя
  async getUserBookings() {
    return apiClient.get('/bookings/user');
  },

  // Отменить бронирование
  async cancelBooking(bookingId) {
    return apiClient.delete(`/bookings/${bookingId}`);
  }
};
```

### Кастомные хуки для работы с API

```javascript
// src/hooks/useCars.js
import { useState, useEffect } from 'react';
import { carService } from '../services/carService';

export function useCars(filters = {}) {
  const [cars, setCars] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchCars = async () => {
      try {
        setLoading(true);
        setError(null);
        const data = await carService.getCars(filters);
        setCars(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchCars();
  }, [filters]);

  const refetch = () => {
    setLoading(true);
    carService.getCars(filters)
      .then(setCars)
      .catch(setError)
      .finally(() => setLoading(false));
  };

  return { cars, loading, error, refetch };
}

// src/hooks/useCar.js
import { useState, useEffect } from 'react';
import { carService } from '../services/carService';

export function useCar(id) {
  const [car, setCar] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!id) return;

    const fetchCar = async () => {
      try {
        setLoading(true);
        setError(null);
        const data = await carService.getCar(id);
        setCar(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchCar();
  }, [id]);

  return { car, loading, error };
}
```

### Пример использования в компоненте

```jsx
// src/pages/CarList.jsx
import React, { useState } from 'react';
import { useCars } from '../hooks/useCars';
import CarCard from '../components/features/CarCard';
import SearchForm from '../components/features/SearchForm';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import ErrorMessage from '../components/ui/ErrorMessage';

export default function CarList() {
  const [filters, setFilters] = useState({});
  const { cars, loading, error, refetch } = useCars(filters);

  const handleSearch = (searchFilters) => {
    setFilters(searchFilters);
  };

  const handleBook = async (carId) => {
    try {
      await carService.createBooking({ carId });
      // Показать уведомление об успехе
      refetch(); // Обновить список
    } catch (error) {
      // Показать ошибку
      console.error('Booking failed:', error);
    }
  };

  const handleView = (carId) => {
    // Навигация к деталям автомобиля
    window.location.href = `/cars/${carId}`;
  };

  if (loading) {
    return <LoadingSpinner />;
  }

  if (error) {
    return <ErrorMessage message={error} onRetry={refetch} />;
  }

  return (
    <div className="car-list">
      <SearchForm onSearch={handleSearch} />
      
      <div className="car-grid">
        {cars.map(car => (
          <CarCard
            key={car.id}
            {...car}
            onBook={handleBook}
            onView={handleView}
          />
        ))}
      </div>
      
      {cars.length === 0 && (
        <div className="empty-state">
          <p>Автомобили не найдены</p>
        </div>
      )}
    </div>
  );
}
```

## 🧪 Тестирование

### Unit тесты с Jest и React Testing Library

```javascript
// tests/components/CarCard.test.jsx
import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import CarCard from '../../src/components/features/CarCard';

const mockCar = {
  id: '1',
  brand: 'Toyota',
  model: 'Camry',
  year: 2022,
  price: 2500,
  image: '/test-image.jpg',
  available: true
};

describe('CarCard', () => {
  it('отображает информацию об автомобиле', () => {
    render(<CarCard {...mockCar} />);
    
    expect(screen.getByText('Toyota Camry')).toBeInTheDocument();
    expect(screen.getByText('2022 год')).toBeInTheDocument();
    expect(screen.getByText('2 500 ₽/сутки')).toBeInTheDocument();
  });

  it('вызывает onBook при клике на кнопку бронирования', () => {
    const mockOnBook = jest.fn();
    render(<CarCard {...mockCar} onBook={mockOnBook} />);
    
    const bookButton = screen.getByText('Забронировать');
    fireEvent.click(bookButton);
    
    expect(mockOnBook).toHaveBeenCalledWith('1');
  });

  it('вызывает onView при клике на кнопку просмотра', () => {
    const mockOnView = jest.fn();
    render(<CarCard {...mockCar} onView={mockOnView} />);
    
    const viewButton = screen.getByText('Подробнее');
    fireEvent.click(viewButton);
    
    expect(mockOnView).toHaveBeenCalledWith('1');
  });

  it('отображает недоступность когда available=false', () => {
    render(<CarCard {...mockCar} available={false} />);
    
    expect(screen.getByText('Недоступен')).toBeInTheDocument();
    expect(screen.getByText('Забронировать')).toBeDisabled();
  });

  it('применяет правильные CSS классы', () => {
    const { container } = render(<CarCard {...mockCar} available={false} />);
    
    expect(container.firstChild).toHaveClass('car-card--unavailable');
  });
});
```

### Интеграционные тесты

```javascript
// tests/integration/CarList.test.jsx
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import CarList from '../../src/pages/CarList';
import { carService } from '../../src/services/carService';

// Мокаем сервис
jest.mock('../../src/services/carService');

const mockCars = [
  {
    id: '1',
    brand: 'Toyota',
    model: 'Camry',
    year: 2022,
    price: 2500,
    image: '/test1.jpg',
    available: true
  },
  {
    id: '2',
    brand: 'BMW',
    model: 'X5',
    year: 2023,
    price: 8000,
    image: '/test2.jpg',
    available: false
  }
];

describe('CarList Integration', () => {
  beforeEach(() => {
    carService.getCars.mockResolvedValue(mockCars);
  });

  it('загружает и отображает список автомобилей', async () => {
    render(
      <BrowserRouter>
        <CarList />
      </BrowserRouter>
    );

    // Показывается загрузка
    expect(screen.getByText(/загрузка/i)).toBeInTheDocument();

    // Ждем загрузки данных
    await waitFor(() => {
      expect(screen.getByText('Toyota Camry')).toBeInTheDocument();
      expect(screen.getByText('BMW X5')).toBeInTheDocument();
    });

    // Проверяем, что сервис был вызван
    expect(carService.getCars).toHaveBeenCalledWith({});
  });

  it('обрабатывает ошибки загрузки', async () => {
    carService.getCars.mockRejectedValue(new Error('Network error'));

    render(
      <BrowserRouter>
        <CarList />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/ошибка/i)).toBeInTheDocument();
    });
  });
});
```

### E2E тесты с Cypress

```javascript
// cypress/e2e/car-list.cy.js
describe('Car List Page', () => {
  beforeEach(() => {
    // Мокаем API ответы
    cy.intercept('GET', '/api/cars', { fixture: 'cars.json' }).as('getCars');
    cy.visit('/cars');
  });

  it('отображает список автомобилей', () => {
    cy.wait('@getCars');
    
    cy.get('[data-testid="car-card"]').should('have.length', 2);
    cy.get('[data-testid="car-card"]').first().should('contain', 'Toyota Camry');
  });

  it('позволяет забронировать доступный автомобиль', () => {
    cy.intercept('POST', '/api/bookings', { statusCode: 201 }).as('createBooking');
    
    cy.wait('@getCars');
    cy.get('[data-testid="car-card"]').first().find('[data-testid="book-button"]').click();
    
    cy.wait('@createBooking');
    cy.get('[data-testid="success-message"]').should('be.visible');
  });

  it('показывает недоступность для забронированных автомобилей', () => {
    cy.wait('@getCars');
    cy.get('[data-testid="car-card"]').last().should('contain', 'Недоступен');
    cy.get('[data-testid="car-card"]').last().find('[data-testid="book-button"]').should('be.disabled');
  });
});
```

## ⚡ Производительность

### Оптимизация компонентов

```jsx
import React, { memo, useCallback, useMemo } from 'react';

// Мемоизация компонента
const CarCard = memo(function CarCard({ car, onBook, onView }) {
  // Мемоизация обработчиков
  const handleBook = useCallback(() => {
    onBook(car.id);
  }, [car.id, onBook]);

  const handleView = useCallback(() => {
    onView(car.id);
  }, [car.id, onView]);

  // Мемоизация вычисляемых значений
  const formattedPrice = useMemo(() => {
    return car.price.toLocaleString() + ' ₽/сутки';
  }, [car.price]);

  return (
    <div className="car-card">
      <img src={car.image} alt={`${car.brand} ${car.model}`} />
      <h3>{car.brand} {car.model}</h3>
      <p>{formattedPrice}</p>
      <button onClick={handleBook}>Забронировать</button>
      <button onClick={handleView}>Подробнее</button>
    </div>
  );
});

export default CarCard;
```

### Ленивая загрузка

```jsx
import React, { Suspense, lazy } from 'react';

// Ленивая загрузка компонентов
const CarDetail = lazy(() => import('./CarDetail'));
const UserProfile = lazy(() => import('./UserProfile'));

function App() {
  return (
    <div>
      <Suspense fallback={<div>Загрузка...</div>}>
        <CarDetail />
      </Suspense>
      
      <Suspense fallback={<div>Загрузка профиля...</div>}>
        <UserProfile />
      </Suspense>
    </div>
  );
}
```

### Виртуализация списков

```jsx
import React from 'react';
import { FixedSizeList as List } from 'react-window';
import CarCard from './CarCard';

function VirtualizedCarList({ cars }) {
  const Row = ({ index, style }) => (
    <div style={style}>
      <CarCard car={cars[index]} />
    </div>
  );

  return (
    <List
      height={600}
      itemCount={cars.length}
      itemSize={200}
      width="100%"
    >
      {Row}
    </List>
  );
}
```

## 🚀 Развертывание

### Сборка для продакшена

```bash
# Создание продакшен сборки
npm run build

# Проверка сборки локально
npx serve -s build

# Анализ размера бандла
npm run analyze
```

### Docker

```dockerfile
# Dockerfile
FROM node:18-alpine as build

WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production

COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### CI/CD Pipeline

```yaml
# .github/workflows/deploy.yml
name: Deploy to Production

on:
  push:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: '18'
      - run: npm ci
      - run: npm test
      - run: npm run build

  deploy:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: '18'
      - run: npm ci
      - run: npm run build
      - name: Deploy to server
        run: |
          # Команды деплоя
```

## 📝 Чек-лист разработки

### Перед созданием компонента:
- [ ] Определить интерфейс (пропсы)
- [ ] Создать макет в Figma/дизайн-системе
- [ ] Планировать тестируемость

### При разработке компонента:
- [ ] Создать базовую структуру JSX
- [ ] Добавить PropTypes
- [ ] Написать CSS с BEM методологией
- [ ] Создать сторис в Storybook
- [ ] Написать unit тесты
- [ ] Проверить доступность (a11y)
- [ ] Протестировать на разных размерах экрана

### Перед коммитом:
- [ ] Запустить линтер: `npm run lint`
- [ ] Запустить тесты: `npm test`
- [ ] Проверить в Storybook
- [ ] Проверить производительность
- [ ] Обновить документацию

### При интеграции:
- [ ] Подключить к API
- [ ] Добавить обработку ошибок
- [ ] Добавить состояния загрузки
- [ ] Протестировать интеграцию
- [ ] Обновить E2E тесты

---

**Этот гайд поможет вам создавать качественные, тестируемые и производительные React-компоненты для AutoRent! 🚗✨** 