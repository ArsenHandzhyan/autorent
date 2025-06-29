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
            firstName: "Иван",
            lastName: "Петров",
            email: "ivan.petrov@example.com",
            phone: "+7 (999) 123-45-67",
            registrationDate: "15.01.2025"
        },
        rentals: [
            { id: 1, car: "Toyota Camry", startDate: "20.01.2025", endDate: "25.01.2025", status: "Активна" },
            { id: 2, car: "Honda Civic", startDate: "10.01.2025", endDate: "12.01.2025", status: "Завершена" }
        ]
    },
    home: {
        featuredCars: [
            { id: 1, brand: "Toyota", model: "Camry", year: 2023, price: 2500, image: "/images/car1.jpg" },
            { id: 2, brand: "Honda", model: "Civic", year: 2022, price: 2200, image: "/images/car2.jpg" },
            { id: 3, brand: "BMW", model: "X5", year: 2024, price: 4500, image: "/images/car3.jpg" }
        ],
        stats: {
            totalCars: 150,
            happyCustomers: 2500,
            cities: 12
        }
    },
    cars: {
        cars: [
            { id: 1, brand: "Toyota", model: "Camry", year: 2023, price: 2500, available: true, image: "/images/car1.jpg" },
            { id: 2, brand: "Honda", model: "Civic", year: 2022, price: 2200, available: true, image: "/images/car2.jpg" },
            { id: 3, brand: "BMW", model: "X5", year: 2024, price: 4500, available: false, image: "/images/car3.jpg" }
        ],
        filters: {
            brands: ["Toyota", "Honda", "BMW", "Mercedes", "Audi"],
            priceRange: { min: 1000, max: 10000 }
        }
    },
    auth: {
        error: null,
        success: null
    },
    admin: {
        stats: {
            totalUsers: 1250,
            totalCars: 150,
            activeRentals: 45,
            monthlyRevenue: 1250000
        },
        recentRentals: [
            { id: 1, user: "Иван Петров", car: "Toyota Camry", startDate: "20.01.2025", status: "Активна" },
            { id: 2, user: "Мария Сидорова", car: "Honda Civic", startDate: "19.01.2025", status: "Активна" }
        ]
    },
    new: {
        car: {
            id: 1,
            brand: "Toyota",
            model: "Camry",
            year: 2023,
            pricePerDay: 2500,
            image: "/images/car1.jpg",
            description: "Комфортный седан для поездок по городу и за городом",
            features: ["Кондиционер", "Автоматическая коробка", "Кожаный салон", "Навигация"],
            available: true
        },
        user: {
            id: 1,
            firstName: "Иван",
            lastName: "Петров",
            email: "ivan.petrov@example.com",
            phone: "+7 (999) 123-45-67",
            licenseNumber: "77АА123456"
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
            notes: "Нужен детский автокресло"
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
            unavailableDates: ["2025-01-22", "2025-01-23"]
        },
        paymentMethods: [
            { id: "card", name: "Банковская карта", icon: "fas fa-credit-card" },
            { id: "cash", name: "Наличные", icon: "fas fa-money-bill-wave" },
            { id: "transfer", name: "Банковский перевод", icon: "fas fa-university" }
        ],
        error: null
    }
};

// Функция обработки Thymeleaf выражений
function processThymeleafExpressions(html, data) {
    // Удаляем Thymeleaf атрибуты
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
    
    // Заменяем @{...} на #
    html = html.replace(/@\{([^}]+)\}/g, '#');
    
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
    </style>
</head>
<body>
    <div class="container mt-4">
        <div class="row">
            <div class="col-12">
                <h1 class="mb-4">
                    <i class="fas fa-eye"></i> 
                    Предварительный просмотр шаблонов AutoRent
                </h1>
                
                <div class="server-info">
                    <h5>Информация о сервере предварительного просмотра:</h5>
                    <ul class="mb-0">
                        <li><strong>Порт:</strong> ${port}</li>
                        <li><strong>Время запуска:</strong> ${new Date().toLocaleString('ru-RU')}</li>
                        <li><strong>Всего шаблонов:</strong> ${templates.length}</li>
                    </ul>
                </div>
                
                <div class="row">
                    ${templates.map(template => `
                        <div class="col-md-6 col-lg-4 mb-3">
                            <div class="card template-card h-100">
                                <div class="card-body">
                                    <h5 class="card-title">
                                        <i class="fas fa-file-code"></i> 
                                        ${template.name}
                                    </h5>
                                    <p class="card-text text-muted">
                                        <small>${template.path}</small>
                                    </p>
                                    <a href="${template.url}" class="btn btn-primary">
                                        <i class="fas fa-eye"></i> Просмотреть
                                    </a>
                                </div>
                            </div>
                        </div>
                    `).join('')}
                </div>
                
                <div class="mt-4">
                    <h5>Доступные мок-данные:</h5>
                    <ul>
                        ${Object.keys(mockData).map(key => `
                            <li><code>${key}</code> - ${Object.keys(mockData[key]).join(', ')}</li>
                        `).join('')}
                    </ul>
                </div>
            </div>
        </div>
    </div>
    
    <!-- Bootstrap 5 JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
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

// Запуск сервера
app.listen(port, () => {
    console.log(`🚀 Сервер предварительного просмотра запущен на http://localhost:${port}`);
    console.log(`📁 Сканирование шаблонов в: src/main/resources/templates/`);
    console.log(`📊 Доступные мок-данные: ${Object.keys(mockData).join(', ')}`);
    console.log(`\n✨ Откройте браузер и перейдите по адресу: http://localhost:${port}`);
}); 