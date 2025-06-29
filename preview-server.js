const express = require('express');
const fs = require('fs').promises;
const path = require('path');
const app = express();
const port = 3001;

// Middleware
app.use(express.json());
app.use(express.static('src/main/resources/static'));
app.use(express.static('.'));

// Мок-данные для разных шаблонов
const mockData = {
    main: {
        user: {
            id: 1,
            firstName: "Иван",
            lastName: "Петров",
            email: "ivan.petrov@example.com",
            phone: "+7 (999) 123-45-67",
            registrationDate: "15.01.2025",
            avatar: "/images/avatars/user1.jpg",
            licenseNumber: "77АА123456",
            licenseExpiry: "2028-12-31",
            rating: 4.8,
            totalRentals: 15,
            totalSpent: 125000,
            isVerified: true,
            preferences: {
                favoriteBrands: ["Toyota", "BMW", "Mercedes"],
                preferredTransmission: "Автомат",
                preferredFuelType: "Бензин"
            }
        },
        rentals: [
            { 
                id: 1, 
                car: "Toyota Camry", 
                carId: 5,
                startDate: "20.01.2025", 
                endDate: "25.01.2025", 
                status: "Активна",
                totalCost: 12500,
                insurance: true,
                pickupLocation: "Москва, ул. Тверская, 1",
                returnLocation: "Москва, ул. Тверская, 1",
                carImage: "/images/cars/toyota-camry.jpg",
                rating: 5,
                review: "Отличный автомобиль, все работает идеально!"
            },
            { 
                id: 2, 
                car: "Honda Civic", 
                carId: 8,
                startDate: "10.01.2025", 
                endDate: "12.01.2025", 
                status: "Завершена",
                totalCost: 6000,
                insurance: false,
                pickupLocation: "Москва, ул. Арбат, 15",
                returnLocation: "Москва, ул. Арбат, 15",
                carImage: "/images/cars/honda-civic.jpg",
                rating: 4,
                review: "Хороший автомобиль, но немного шумный"
            },
            { 
                id: 3, 
                car: "BMW X5", 
                carId: 12,
                startDate: "05.01.2025", 
                endDate: "08.01.2025", 
                status: "Завершена",
                totalCost: 18000,
                insurance: true,
                pickupLocation: "Москва, ул. Новый Арбат, 8",
                returnLocation: "Москва, ул. Новый Арбат, 8",
                carImage: "/images/cars/bmw-x5.jpg",
                rating: 5,
                review: "Превосходный внедорожник, очень комфортный!"
            }
        ],
        upcomingRentals: [
            {
                id: 4,
                car: "Mercedes C-Class",
                startDate: "30.01.2025",
                endDate: "02.02.2025",
                totalCost: 16000,
                status: "Подтверждена"
            }
        ],
        notifications: [
            {
                id: 1,
                type: "info",
                message: "Ваша аренда Toyota Camry заканчивается через 2 дня",
                date: "2025-01-23T10:30:00",
                isRead: false
            },
            {
                id: 2,
                type: "success",
                message: "Оплата прошла успешно. Сумма: 12,500 ₽",
                date: "2025-01-20T14:15:00",
                isRead: true
            }
        ],
        statistics: {
            totalRentals: 15,
            totalSpent: 125000,
            averageRating: 4.8,
            favoriteBrand: "Toyota",
            mostRentedCar: "Toyota Camry"
        }
    },
    home: {
        featuredCars: [
            { 
                id: 1, 
                brand: "Toyota", 
                model: "Camry", 
                year: 2023, 
                price: 2500, 
                image: "/images/cars/toyota-camry.jpg",
                rating: 4.8,
                reviews: 45,
                available: true,
                transmission: "Автомат",
                fuelType: "Бензин",
                seats: 5,
                features: ["Кондиционер", "Навигация", "Кожаный салон"]
            },
            { 
                id: 2, 
                brand: "Honda", 
                model: "Civic", 
                year: 2022, 
                price: 2200, 
                image: "/images/cars/honda-civic.jpg",
                rating: 4.6,
                reviews: 32,
                available: true,
                transmission: "Механика",
                fuelType: "Бензин",
                seats: 5,
                features: ["Кондиционер", "Bluetooth", "Круиз-контроль"]
            },
            { 
                id: 3, 
                brand: "BMW", 
                model: "X5", 
                year: 2024, 
                price: 4500, 
                image: "/images/cars/bmw-x5.jpg",
                rating: 4.9,
                reviews: 28,
                available: false,
                transmission: "Автомат",
                fuelType: "Дизель",
                seats: 7,
                features: ["Полный привод", "Парктроники", "Панорамная крыша"]
            },
            { 
                id: 4, 
                brand: "Mercedes", 
                model: "C-Class", 
                year: 2023, 
                price: 3800, 
                image: "/images/cars/mercedes-c-class.jpg",
                rating: 4.7,
                reviews: 38,
                available: true,
                transmission: "Автомат",
                fuelType: "Бензин",
                seats: 5,
                features: ["Кожаный салон", "Навигация", "Система безопасности"]
            }
        ],
        stats: {
            totalCars: 150,
            happyCustomers: 2500,
            cities: 12,
            averageRating: 4.7,
            totalRentals: 8500,
            satisfactionRate: 98
        },
        testimonials: [
            {
                id: 1,
                name: "Анна Сидорова",
                rating: 5,
                text: "Отличный сервис! Автомобиль был в идеальном состоянии, процесс аренды очень простой.",
                date: "2025-01-15",
                car: "Toyota Camry"
            },
            {
                id: 2,
                name: "Михаил Козлов",
                rating: 5,
                text: "Быстрое оформление, качественные автомобили. Рекомендую всем!",
                date: "2025-01-12",
                car: "BMW X5"
            },
            {
                id: 3,
                name: "Елена Петрова",
                rating: 4,
                text: "Удобно и быстро. Единственное - хотелось бы больше электромобилей.",
                date: "2025-01-10",
                car: "Honda Civic"
            }
        ],
        specialOffers: [
            {
                id: 1,
                title: "Скидка 20% на выходные",
                description: "Арендуйте автомобиль на выходные и получите скидку 20%",
                discount: 20,
                validUntil: "2025-02-01",
                code: "WEEKEND20"
            },
            {
                id: 2,
                title: "Бесплатная страховка",
                description: "При аренде от 3 дней - страховка в подарок",
                discount: 0,
                validUntil: "2025-01-31",
                code: "INSURANCE"
            }
        ]
    },
    cars: {
        cars: [
            { 
                id: 1, 
                brand: "Toyota", 
                model: "Camry", 
                year: 2023, 
                price: 2500, 
                available: true, 
                image: "/images/cars/toyota-camry.jpg",
                rating: 4.8,
                reviews: 45,
                transmission: "Автомат",
                fuelType: "Бензин",
                seats: 5,
                mileage: 15000,
                color: "Белый",
                features: ["Кондиционер", "Навигация", "Кожаный салон", "Bluetooth", "Круиз-контроль"],
                description: "Комфортный седан для поездок по городу и за городом"
            },
            { 
                id: 2, 
                brand: "Honda", 
                model: "Civic", 
                year: 2022, 
                price: 2200, 
                available: true, 
                image: "/images/cars/honda-civic.jpg",
                rating: 4.6,
                reviews: 32,
                transmission: "Механика",
                fuelType: "Бензин",
                seats: 5,
                mileage: 25000,
                color: "Серебристый",
                features: ["Кондиционер", "Bluetooth", "Круиз-контроль", "Парктроники"],
                description: "Экономичный и надежный автомобиль"
            },
            { 
                id: 3, 
                brand: "BMW", 
                model: "X5", 
                year: 2024, 
                price: 4500, 
                available: false, 
                image: "/images/cars/bmw-x5.jpg",
                rating: 4.9,
                reviews: 28,
                transmission: "Автомат",
                fuelType: "Дизель",
                seats: 7,
                mileage: 8000,
                color: "Черный",
                features: ["Полный привод", "Парктроники", "Панорамная крыша", "Навигация"],
                description: "Роскошный внедорожник для любых дорог"
            },
            { 
                id: 4, 
                brand: "Mercedes", 
                model: "C-Class", 
                year: 2023, 
                price: 3800, 
                available: true, 
                image: "/images/cars/mercedes-c-class.jpg",
                rating: 4.7,
                reviews: 38,
                transmission: "Автомат",
                fuelType: "Бензин",
                seats: 5,
                mileage: 12000,
                color: "Серый",
                features: ["Кожаный салон", "Навигация", "Система безопасности", "Матричные фары"],
                description: "Элегантный седан премиум-класса"
            },
            { 
                id: 5, 
                brand: "Audi", 
                model: "A4", 
                year: 2023, 
                price: 3200, 
                available: true, 
                image: "/images/cars/audi-a4.jpg",
                rating: 4.5,
                reviews: 25,
                transmission: "Автомат",
                fuelType: "Бензин",
                seats: 5,
                mileage: 18000,
                color: "Синий",
                features: ["Полный привод", "Навигация", "Кожаный салон", "LED фары"],
                description: "Спортивный седан с полным приводом"
            },
            { 
                id: 6, 
                brand: "Volkswagen", 
                model: "Passat", 
                year: 2022, 
                price: 2800, 
                available: true, 
                image: "/images/cars/vw-passat.jpg",
                rating: 4.4,
                reviews: 30,
                transmission: "Автомат",
                fuelType: "Дизель",
                seats: 5,
                mileage: 22000,
                color: "Белый",
                features: ["Кондиционер", "Навигация", "Круиз-контроль", "Парктроники"],
                description: "Надежный семейный автомобиль"
            }
        ],
        filters: {
            brands: ["Toyota", "Honda", "BMW", "Mercedes", "Audi", "Volkswagen", "Ford", "Hyundai", "Kia", "Nissan"],
            priceRange: { min: 1000, max: 10000 },
            years: [2020, 2021, 2022, 2023, 2024],
            transmissions: ["Автомат", "Механика", "Робот"],
            fuelTypes: ["Бензин", "Дизель", "Гибрид", "Электро"],
            seats: [2, 4, 5, 7, 8],
            features: ["Кондиционер", "Навигация", "Кожаный салон", "Полный привод", "Парктроники"]
        },
        pagination: {
            currentPage: 1,
            totalPages: 5,
            totalItems: 150,
            itemsPerPage: 6
        },
        sortOptions: [
            { value: "price_asc", label: "По цене (возрастание)" },
            { value: "price_desc", label: "По цене (убывание)" },
            { value: "rating", label: "По рейтингу" },
            { value: "year", label: "По году выпуска" },
            { value: "brand", label: "По марке" }
        ]
    },
    auth: {
        error: null,
        success: null,
        loginForm: {
            email: "",
            password: "",
            rememberMe: false
        },
        registerForm: {
            firstName: "",
            lastName: "",
            email: "",
            phone: "",
            password: "",
            confirmPassword: "",
            agreeToTerms: false
        },
        forgotPasswordForm: {
            email: ""
        },
        resetPasswordForm: {
            token: "",
            password: "",
            confirmPassword: ""
        }
    },
    admin: {
        stats: {
            totalUsers: 1250,
            totalCars: 150,
            activeRentals: 45,
            monthlyRevenue: 1250000,
            averageRating: 4.7,
            totalBookings: 8500,
            newUsersThisMonth: 45,
            revenueGrowth: 12.5
        },
        recentRentals: [
            { 
                id: 1, 
                user: "Иван Петров", 
                userEmail: "ivan.petrov@example.com",
                car: "Toyota Camry", 
                startDate: "20.01.2025", 
                endDate: "25.01.2025",
                status: "Активна",
                totalCost: 12500,
                paymentStatus: "Оплачено",
                createdAt: "2025-01-18T10:30:00"
            },
            { 
                id: 2, 
                user: "Мария Сидорова", 
                userEmail: "maria.sidorova@example.com",
                car: "Honda Civic", 
                startDate: "19.01.2025", 
                endDate: "22.01.2025",
                status: "Активна",
                totalCost: 6600,
                paymentStatus: "Оплачено",
                createdAt: "2025-01-17T14:15:00"
            },
            { 
                id: 3, 
                user: "Алексей Козлов", 
                userEmail: "alexey.kozlov@example.com",
                car: "BMW X5", 
                startDate: "18.01.2025", 
                endDate: "21.01.2025",
                status: "Завершена",
                totalCost: 13500,
                paymentStatus: "Оплачено",
                createdAt: "2025-01-16T09:45:00"
            },
            { 
                id: 4, 
                user: "Елена Петрова", 
                userEmail: "elena.petrova@example.com",
                car: "Mercedes C-Class", 
                startDate: "17.01.2025", 
                endDate: "20.01.2025",
                status: "Завершена",
                totalCost: 11400,
                paymentStatus: "Оплачено",
                createdAt: "2025-01-15T16:20:00"
            }
        ],
        recentUsers: [
            {
                id: 1,
                name: "Анна Иванова",
                email: "anna.ivanova@example.com",
                registrationDate: "2025-01-20",
                status: "Активен",
                totalRentals: 0,
                lastLogin: "2025-01-20T12:30:00"
            },
            {
                id: 2,
                name: "Дмитрий Смирнов",
                email: "dmitry.smirnov@example.com",
                registrationDate: "2025-01-19",
                status: "Активен",
                totalRentals: 1,
                lastLogin: "2025-01-19T18:45:00"
            }
        ],
        systemAlerts: [
            {
                id: 1,
                type: "warning",
                message: "Низкий уровень топлива у автомобиля BMW X5 (ID: 3)",
                date: "2025-01-20T15:30:00",
                isRead: false
            },
            {
                id: 2,
                type: "info",
                message: "Новый пользователь зарегистрирован: Анна Иванова",
                date: "2025-01-20T12:30:00",
                isRead: true
            }
        ],
        revenueChart: {
            labels: ["Янв", "Фев", "Мар", "Апр", "Май", "Июн"],
            data: [850000, 920000, 880000, 950000, 1100000, 1250000]
        }
    },
    new: {
        car: {
            id: 1,
            brand: "Toyota",
            model: "Camry",
            year: 2023,
            pricePerDay: 2500,
            image: "/images/cars/toyota-camry.jpg",
            description: "Комфортный седан для поездок по городу и за городом",
            features: ["Кондиционер", "Автоматическая коробка", "Кожаный салон", "Навигация"],
            available: true,
            transmission: "Автомат",
            fuelType: "Бензин",
            seats: 5,
            mileage: 15000,
            color: "Белый",
            rating: 4.8,
            reviews: 45
        },
        user: {
            id: 1,
            firstName: "Иван",
            lastName: "Петров",
            email: "ivan.petrov@example.com",
            phone: "+7 (999) 123-45-67",
            licenseNumber: "77АА123456",
            licenseExpiry: "2028-12-31",
            rating: 4.8,
            totalRentals: 15
        },
        rental: {
            startDate: "2025-01-20T10:00",
            endDate: "2025-01-25T18:00",
            totalDays: 5,
            dailyRate: 2500,
            totalCost: 12500,
            insurance: true,
            insuranceCost: 1000,
            finalCost: 13500,
            notes: "Нужен детский автокресло",
            pickupLocation: "Москва, ул. Тверская, 1",
            returnLocation: "Москва, ул. Тверская, 1",
            pickupTime: "10:00",
            returnTime: "18:00"
        },
        bookedPeriods: [
            {
                startDate: "2025-01-22T00:00:00",
                endDate: "2025-01-23T23:59:59"
            },
            {
                startDate: "2025-01-28T00:00:00",
                endDate: "2025-01-30T23:59:59"
            }
        ],
        availableDates: {
            minDate: "2025-01-20",
            maxDate: "2025-12-31",
            unavailableDates: ["2025-01-22", "2025-01-23", "2025-01-28", "2025-01-29", "2025-01-30"]
        },
        paymentMethods: [
            { id: "card", name: "Банковская карта", icon: "fas fa-credit-card", description: "Visa, MasterCard, МИР" },
            { id: "cash", name: "Наличные", icon: "fas fa-money-bill-wave", description: "При получении автомобиля" },
            { id: "transfer", name: "Банковский перевод", icon: "fas fa-university", description: "Счет в банке" },
            { id: "crypto", name: "Криптовалюта", icon: "fab fa-bitcoin", description: "Bitcoin, Ethereum" }
        ],
        insuranceOptions: [
            {
                id: "basic",
                name: "Базовая страховка",
                description: "Включена в стоимость аренды",
                price: 0,
                coverage: "ОСАГО"
            },
            {
                id: "full",
                name: "Полная страховка",
                description: "Покрывает все риски",
                price: 1000,
                coverage: "КАСКО + ОСАГО"
            },
            {
                id: "premium",
                name: "Премиум страховка",
                description: "Максимальная защита",
                price: 2000,
                coverage: "КАСКО + ОСАГО + Дополнительные риски"
            }
        ],
        additionalServices: [
            {
                id: "child_seat",
                name: "Детское автокресло",
                price: 500,
                description: "Для детей от 0 до 12 лет"
            },
            {
                id: "gps",
                name: "GPS навигатор",
                price: 300,
                description: "Портативный навигатор"
            },
            {
                id: "wifi",
                name: "WiFi роутер",
                price: 400,
                description: "Мобильный интернет в дороге"
            }
        ],
        error: null,
        success: "Бронирование успешно создано! Ожидайте подтверждения."
    }
};

// Функция обработки Thymeleaf выражений
function processThymeleafExpressions(html, data) {
    // Удаляем Thymeleaf атрибуты, но сохраняем содержимое
    html = html.replace(/th:href="@\{([^}]+)\}"/g, 'href="$1"');
    html = html.replace(/th:src="@\{([^}]+)\}"/g, 'src="$1"');
    html = html.replace(/th:[^=]*="[^"]*"/g, '');
    html = html.replace(/xmlns:th="[^"]*"/g, '');
    
    // Заменяем выражения ${...} на мок-данные
    html = html.replace(/\$\{([^}]+)\}/g, (match, expression) => {
        const parts = expression.split('.');
        let value = data;
        
        for (const part of parts) {
            if (value && typeof value === 'object' && part in value) {
                value = value[part];
            } else {
                return match; // Возвращаем оригинальное выражение если не найдено
            }
        }
        
        return value || match;
    });
    
    // Заменяем @{...} на правильные пути (кроме уже обработанных href и src)
    html = html.replace(/@\{([^}]+)\}/g, '$1');
    
    // Удаляем оставшиеся Thymeleaf выражения
    html = html.replace(/\[\[([^\]]+)\]\]/g, '');
    html = html.replace(/\(\(([^)]+)\)\)/g, '');
    
    return html;
}

// API endpoint для получения списка шаблонов
app.get('/api/templates', async (req, res) => {
    try {
        const templatesDir = path.join(__dirname, 'src/main/resources/templates');
        const files = await fs.readdir(templatesDir, { recursive: true });
        
        const templates = files
            .filter(file => file.endsWith('.html'))
            .map(file => ({
                name: file.replace('.html', ''),
                path: file,
                url: `/preview/${file.replace('.html', '')}`
            }));
        
        res.json(templates);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// API endpoint для получения мок-данных
app.get('/api/mock-data/:template', (req, res) => {
    const templateName = req.params.template;
    const data = mockData[templateName] || {};
    res.json(data);
});

// Endpoint для предварительного просмотра шаблона
app.get('/preview/*', async (req, res) => {
    try {
        // Получаем путь после /preview/
        const templatePath = req.params[0];
        const fullPath = path.join(__dirname, 'src/main/resources/templates', `${templatePath}.html`);
        
        // Проверяем существование файла
        try {
            await fs.access(fullPath);
        } catch {
            return res.status(404).send(`
                <html>
                    <head><title>Шаблон не найден</title></head>
                    <body>
                        <h1>Шаблон ${templatePath}.html не найден</h1>
                        <p>Проверьте правильность пути к файлу.</p>
                        <p>Ожидаемый путь: ${fullPath}</p>
                        <a href="/">Вернуться к списку шаблонов</a>
                    </body>
                </html>
            `);
        }
        
        // Читаем шаблон
        const templateContent = await fs.readFile(fullPath, 'utf8');
        
        // Определяем ключ для мок-данных (берем последнюю часть пути)
        const templateKey = templatePath.split('/').pop();
        
        // Обрабатываем Thymeleaf выражения
        const processedHtml = processThymeleafExpressions(templateContent, mockData[templateKey] || {});
        
        // Создаем HTML страницу с предварительным просмотром
        const previewHtml = `
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Предварительный просмотр - ${templatePath}</title>
    
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <!-- FontAwesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    
    <!-- Единый файл стилей -->
    <link rel="stylesheet" href="/css/main.css">
    
    <style>
        .preview-header {
            background: #f8f9fa;
            padding: 15px;
            border-bottom: 2px solid #007bff;
            margin-bottom: 20px;
        }
        
        .preview-info {
            background: #e9ecef;
            padding: 10px;
            border-radius: 5px;
            margin-bottom: 20px;
            font-size: 14px;
        }
        
        .mock-data-panel {
            background: #fff3cd;
            border: 1px solid #ffeaa7;
            border-radius: 5px;
            padding: 15px;
            margin-bottom: 20px;
        }
        
        .mock-data-panel pre {
            background: #f8f9fa;
            padding: 10px;
            border-radius: 3px;
            font-size: 12px;
            max-height: 200px;
            overflow-y: auto;
        }
        
        .template-actions {
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <div class="preview-header">
        <div class="container">
            <div class="row align-items-center">
                <div class="col">
                    <h2>Предварительный просмотр: ${templatePath}.html</h2>
                    <p class="mb-0">Это предварительный просмотр шаблона с мок-данными</p>
                </div>
                <div class="col-auto">
                    <a href="/" class="btn btn-outline-primary">← К списку шаблонов</a>
                </div>
            </div>
        </div>
    </div>
    
    <div class="container">
        <div class="preview-info">
            <strong>Файл:</strong> src/main/resources/templates/${templatePath}.html
            <br>
            <strong>Ключ мок-данных:</strong> ${templateKey}
            <br>
            <strong>Время загрузки:</strong> ${new Date().toLocaleString('ru-RU')}
        </div>
        
        <div class="mock-data-panel">
            <h5>Мок-данные для шаблона:</h5>
            <pre>${JSON.stringify(mockData[templateKey] || {}, null, 2)}</pre>
        </div>
        
        <div class="template-actions">
            <button class="btn btn-sm btn-outline-secondary" onclick="toggleMockData()">
                Скрыть/Показать мок-данные
            </button>
            <button class="btn btn-sm btn-outline-info" onclick="reloadTemplate()">
                Обновить
            </button>
        </div>
        
        <hr>
        
        <!-- Контент шаблона -->
        <div id="template-content">
            ${processedHtml}
        </div>
    </div>
    
    <!-- Bootstrap 5 JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    
    <!-- Единый файл скриптов -->
    <script src="/js/main.js"></script>
    
    <script>
        function toggleMockData() {
            const panel = document.querySelector('.mock-data-panel');
            panel.style.display = panel.style.display === 'none' ? 'block' : 'none';
        }
        
        function reloadTemplate() {
            location.reload();
        }
        
        // Добавляем информацию о том, что это предварительный просмотр
        console.log('Предварительный просмотр шаблона: ${templatePath}');
        console.log('Ключ мок-данных: ${templateKey}');
        console.log('Мок-данные:', ${JSON.stringify(mockData[templateKey] || {})});
    </script>
</body>
</html>`;
        
        res.send(previewHtml);
        
    } catch (error) {
        res.status(500).send(`
            <html>
                <head><title>Ошибка</title></head>
                <body>
                    <h1>Ошибка загрузки шаблона</h1>
                    <p>${error.message}</p>
                    <a href="/">Вернуться к списку шаблонов</a>
                </body>
            </html>
        `);
    }
});

// Главная страница со списком шаблонов
app.get('/', async (req, res) => {
    try {
        const templatesDir = path.join(__dirname, 'src/main/resources/templates');
        const files = await fs.readdir(templatesDir, { recursive: true });
        
        const templates = files
            .filter(file => file.endsWith('.html'))
            .map(file => ({
                name: file.replace('.html', ''),
                path: file,
                url: `/preview/${file.replace('.html', '')}`
            }));
        
        const html = `
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Предварительный просмотр шаблонов AutoRent</title>
    
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <!-- FontAwesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    
    <!-- Единый файл стилей -->
    <link rel="stylesheet" href="/css/main.css">
    
    <style>
        .template-card {
            transition: transform 0.2s;
        }
        
        .template-card:hover {
            transform: translateY(-2px);
        }
        
        .server-info {
            background: #e9ecef;
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
        }
        
        .test-link {
            background: #fff3cd;
            border: 1px solid #ffeaa7;
            border-radius: 5px;
            padding: 15px;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <div class="container mt-4">
        <div class="row">
            <div class="col-12">
                <div class="server-info">
                    <h2><i class="fas fa-server"></i> Сервер предварительного просмотра AutoRent</h2>
                    <p class="mb-0">
                        <strong>Порт:</strong> 3001 | 
                        <strong>Статус:</strong> <span class="text-success">Работает</span> | 
                        <strong>Время запуска:</strong> ${new Date().toLocaleString('ru-RU')}
                    </p>
                </div>
                
                <div class="test-link">
                    <h5><i class="fas fa-vial"></i> Тест подключения стилей и скриптов</h5>
                    <p class="mb-2">Проверьте, что общие CSS и JS файлы корректно загружаются:</p>
                    <a href="/test-styles" class="btn btn-warning">
                        <i class="fas fa-palette"></i> Тест стилей и скриптов
                    </a>
                </div>
                
                <h3><i class="fas fa-file-code"></i> Доступные шаблоны (${templates.length})</h3>
                <div class="row">
                    ${templates.map(template => `
                        <div class="col-md-4 mb-3">
                            <div class="card template-card h-100">
                                <div class="card-body">
                                    <h5 class="card-title">${template.name}</h5>
                                    <p class="card-text text-muted">${template.path}</p>
                                    <a href="${template.url}" class="btn btn-primary">
                                        <i class="fas fa-eye"></i> Просмотр
                                    </a>
                                </div>
                            </div>
                        </div>
                    `).join('')}
                </div>
            </div>
        </div>
    </div>
    
    <!-- Bootstrap 5 JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    
    <!-- Единый файл скриптов -->
    <script src="/js/main.js"></script>
</body>
</html>`;
        
        res.send(html);
        
    } catch (error) {
        res.status(500).send(`
            <html>
                <head><title>Ошибка</title></head>
                <body>
                    <h1>Ошибка загрузки списка шаблонов</h1>
                    <p>${error.message}</p>
                </body>
            </html>
        `);
    }
});

// Маршрут для тестового файла стилей
app.get('/test-styles', (req, res) => {
    res.sendFile(path.join(__dirname, 'test-styles.html'));
});

// Запуск сервера
app.listen(port, () => {
    console.log(`🚀 Сервер предварительного просмотра запущен на http://localhost:${port}`);
    console.log(`📁 Сканирование шаблонов в: src/main/resources/templates/`);
    console.log(`📊 Доступные мок-данные: ${Object.keys(mockData).join(', ')}`);
    console.log(`\n✨ Откройте браузер и перейдите по адресу: http://localhost:${port}`);
}); 