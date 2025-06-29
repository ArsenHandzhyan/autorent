/**
 * AUTO RENT - ЕДИНЫЙ JAVASCRIPT ФАЙЛ
 * Все скрипты приложения в одном файле
 * 
 * @author AutoRent Team
 * @version 1.0.0
 */

// ========================================
// ГЛОБАЛЬНЫЕ ПЕРЕМЕННЫЕ И КОНСТАНТЫ
// ========================================

// CSRF токены
let csrfToken = null;
let csrfHeader = null;

// API endpoints
const API_ENDPOINTS = {
    CARS: '/api/cars',
    RENTALS: '/api/rentals',
    USERS: '/api/users',
    PAYMENTS: '/api/payments',
    REVIEWS: '/api/reviews'
};

// Статусы
const STATUSES = {
    ACTIVE: 'ACTIVE',
    PENDING: 'PENDING',
    COMPLETED: 'COMPLETED',
    CANCELLED: 'CANCELLED',
    PENDING_CANCELLATION: 'PENDING_CANCELLATION',
    MAINTENANCE: 'MAINTENANCE'
};

// ========================================
// ИНИЦИАЛИЗАЦИЯ ПРИ ЗАГРУЗКЕ СТРАНИЦЫ
// ========================================

document.addEventListener('DOMContentLoaded', function() {
    console.log('AutoRent: Инициализация приложения...');
    
    // Инициализация всех компонентов
    initCSRF();
    initNavigation();
    initAnimations();
    initFormValidation();
    initCarGallery();
    initAdminDashboard();
    initRentalManagement();
    initPaymentSystem();
    initNotificationSystem();
    initAccountPages();
    initCarDeletionConfirmation();
    initRejectCancellationForm();
    initUserStatusToggle();
    initCarEditPage();
    initPasswordReset();
    
    // Новые функции для автомобилей
    initCarImageUpload();
    initCarFormValidation();
    initCarImageManagement();
    
    // Глобальный обработчик ошибок
    initGlobalErrorHandler();
    
    // Кнопка возврата назад в хедере
    initBackButton();
    
    console.log('AutoRent: Приложение инициализировано');
});

// ========================================
// CSRF ТОКЕНЫ
// ========================================

function initCSRF() {
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    
    if (csrfMeta && csrfHeaderMeta) {
        csrfToken = csrfMeta.getAttribute('content');
        csrfHeader = csrfHeaderMeta.getAttribute('content');
        console.log('CSRF токены инициализированы');
    }
}

// ========================================
// НАВИГАЦИЯ И МЕНЮ
// ========================================

function initNavigation() {
    /**
     * Обработка формы выхода (оставляем, если нужен AJAX-logout)
     */
    const logoutForm = document.querySelector('form[action*="/auth/logout"]');
    if (logoutForm) {
        logoutForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const formData = new FormData(this);
            fetch(this.action, {
                method: 'POST',
                body: formData,
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            }).then(() => {
                window.location.href = '/auth/login?logout';
            });
        });
    }

    /**
     * Подсветка активного пункта меню
     */
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.nav-link');
    
    navLinks.forEach(link => {
        const linkPath = link.getAttribute('href');
        if (linkPath && currentPath.startsWith(linkPath) && linkPath !== '/') {
            link.classList.add('active');
        } else if (linkPath === '/' && currentPath === '/') {
            link.classList.add('active');
        }
    });

    /**
     * Улучшенная обработка мобильного меню
     */
    const navbarToggler = document.querySelector('.navbar-toggler');
    const navbarNav = document.querySelector('.navbar-nav');
    
    if (navbarToggler && navbarNav) {
        // Обработка клика по кнопке меню
        navbarToggler.addEventListener('click', function(e) {
            e.preventDefault();
            toggleMobileMenu();
        });

        // Закрытие меню при клике вне его
        document.addEventListener('click', function(e) {
            if (!navbarNav.contains(e.target) && !navbarToggler.contains(e.target)) {
                closeMobileMenu();
            }
        });

        // Закрытие меню при изменении ориентации экрана
        window.addEventListener('orientationchange', function() {
            setTimeout(() => {
                closeMobileMenu();
            }, 100);
        });

        // Закрытие меню при изменении размера окна
        let resizeTimer;
        window.addEventListener('resize', function() {
            clearTimeout(resizeTimer);
            resizeTimer = setTimeout(() => {
                if (window.innerWidth > 767.98) {
                    closeMobileMenu();
                }
            }, 250);
        });

        // Обработка выпадающих меню в мобильной версии
        const dropdownToggles = document.querySelectorAll('.dropdown-toggle');
        dropdownToggles.forEach(toggle => {
            toggle.addEventListener('click', function(e) {
                if (window.innerWidth <= 767.98) {
                    e.preventDefault();
                    const dropdownMenu = this.nextElementSibling;
                    if (dropdownMenu && dropdownMenu.classList.contains('dropdown-menu')) {
                        dropdownMenu.classList.toggle('show');
                    }
                }
            });
        });
    }
}

/**
 * Переключение мобильного меню
 */
function toggleMobileMenu() {
    const navbarNav = document.querySelector('.navbar-nav');
    if (navbarNav) {
        navbarNav.classList.toggle('show');
        
        // Обновляем aria-expanded для доступности
        const navbarToggler = document.querySelector('.navbar-toggler');
        if (navbarToggler) {
            const isExpanded = navbarNav.classList.contains('show');
            navbarToggler.setAttribute('aria-expanded', isExpanded);
        }
    }
}

/**
 * Закрытие мобильного меню
 */
function closeMobileMenu() {
    const navbarNav = document.querySelector('.navbar-nav');
    if (navbarNav) {
        navbarNav.classList.remove('show');
        
        // Закрываем все выпадающие меню
        const dropdownMenus = document.querySelectorAll('.dropdown-menu.show');
        dropdownMenus.forEach(menu => {
            menu.classList.remove('show');
        });
        
        // Обновляем aria-expanded для доступности
        const navbarToggler = document.querySelector('.navbar-toggler');
        if (navbarToggler) {
            navbarToggler.setAttribute('aria-expanded', 'false');
        }
    }
}

// ========================================
// АНИМАЦИИ И ЭФФЕКТЫ
// ========================================

function initAnimations() {
    /**
     * Плавная прокрутка для якорных ссылок (оставляю только этот блок)
     */
    const anchorLinks = document.querySelectorAll('a[href^="#"]');
    anchorLinks.forEach(link => {
        link.addEventListener('click', (event) => {
            const href = link.getAttribute('href');
            // Если просто "#", предотвращаем действие и выходим
            if (href === '#') {
                event.preventDefault();
                return;
            }
            // Только для валидных якорей
            if (href && href.length > 1 && href.startsWith('#')) {
                const target = document.querySelector(href);
                if (target) {
                    event.preventDefault();
                    target.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            }
        });
    });

    /**
     * Анимация при скролле
     */
    const animatedElements = document.querySelectorAll('.animate-on-scroll');
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('animate-in');
                observer.unobserve(entry.target); // Отключаем наблюдение после анимации
            }
        });
    }, {
        threshold: 0.1 // 10% элемента должно быть видно
    });

    animatedElements.forEach(element => {
        observer.observe(element);
    });
}

// ========================================
// ВАЛИДАЦИЯ ФОРМ
// ========================================

function initFormValidation() {
    /**
     * Валидация форм
     */
    (function () {
        'use strict';
        // Получаем все формы, к которым мы хотим применить кастомные стили валидации
        var forms = document.querySelectorAll('.needs-validation');

        // Проходим по ним и предотвращаем отправку
        Array.prototype.slice.call(forms)
            .forEach(function (form) {
                form.addEventListener('submit', function (event) {
                    if (!form.checkValidity()) {
                        event.preventDefault();
                        event.stopPropagation();
                    }

                    form.classList.add('was-validated');
                }, false);
            });
    })();
}

// ========================================
// ГАЛЕРЕЯ ИЗОБРАЖЕНИЙ АВТОМОБИЛЕЙ
// ========================================

function initCarGallery() {
    const mainImage = document.querySelector('.main-image img');
    const thumbnails = document.querySelectorAll('.thumbnail');
    
    if (!mainImage || thumbnails.length === 0) return;
    
    thumbnails.forEach(thumbnail => {
        thumbnail.addEventListener('click', function() {
            const newSrc = this.getAttribute('data-src');
            mainImage.setAttribute('src', newSrc);
            
            // Снимаем активный класс со всех миниатюр
            thumbnails.forEach(t => t.classList.remove('active'));
            // Добавляем активный класс к нажатой миниатюре
            this.classList.add('active');
        });
    });
    
    // Поворот изображений
    const rotateButtons = document.querySelectorAll('.rotate-btn');
    rotateButtons.forEach(button => {
        button.addEventListener('click', function() {
            const direction = this.dataset.direction;
            const imageId = this.dataset.imageId;
            rotateImage(imageId, direction);
        });
    });
}

function rotateImage(imageId, direction) {
    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");
    
    if (!token || !header) {
        showNotification('Ошибка: CSRF токен не найден', 'error');
        return;
    }
    
    fetch(`/cars/images/${imageId}/rotate?direction=${direction}`, {
        method: 'POST',
        headers: {
            [header]: token
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Обновляем изображение на странице
            const image = document.querySelector(`img[data-image-id="${imageId}"]`);
            if (image) {
                image.setAttribute('data-rotation', data.rotation);
                image.className = 'img-thumbnail rotated-' + data.rotation;
                image.style.transform = `rotate(${data.rotation}deg)`;
            }
            // Обновляем скрытое поле с углом
            const hiddenInput = document.querySelector(`input[name='imageRotation_${imageId}']`);
            if (hiddenInput) {
                hiddenInput.value = data.rotation;
            }
            showNotification('Изображение повернуто', 'success');
        } else {
            showNotification(data.error || 'Ошибка при повороте изображения', 'error');
        }
    })
    .catch(error => {
        console.error('Ошибка:', error);
        showNotification('Произошла ошибка при повороте изображения', 'error');
    });
}

// Функция удаления изображения
function deleteImage(imageId) {
    // Подтверждение удаления
    if (!confirm('Вы уверены, что хотите удалить это изображение?')) {
        return;
    }

    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");
    
    if (!token || !header) {
        showNotification('Ошибка: CSRF токен не найден', 'error');
        return;
    }

    // Показываем индикатор загрузки
    const deleteBtn = document.querySelector(`.delete-image[data-image-id="${imageId}"]`);
    const originalText = deleteBtn.innerHTML;
    deleteBtn.disabled = true;
    deleteBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Удаление...';

    fetch(`/cars/images/${imageId}/delete`, {
        method: 'POST',
        headers: {
            [header]: token,
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        if (data.success) {
            // Удаляем элемент изображения со страницы
            const imageItem = document.querySelector(`.image-item:has(img[data-image-id="${imageId}"])`);
            if (imageItem) {
                imageItem.remove();
                showNotification('Изображение успешно удалено', 'success');
            } else {
                // Альтернативный способ удаления, если :has() не поддерживается
                const image = document.querySelector(`img[data-image-id="${imageId}"]`);
                if (image) {
                    const imageContainer = image.closest('.image-item');
                    if (imageContainer) {
                        imageContainer.remove();
                        showNotification('Изображение успешно удалено', 'success');
                    }
                }
            }
        } else {
            showNotification(data.error || 'Ошибка при удалении изображения', 'error');
            // Восстанавливаем кнопку
            deleteBtn.disabled = false;
            deleteBtn.innerHTML = originalText;
        }
    })
    .catch(error => {
        console.error('Ошибка:', error);
        showNotification('Произошла ошибка при удалении изображения', 'error');
        // Восстанавливаем кнопку
        deleteBtn.disabled = false;
        deleteBtn.innerHTML = originalText;
    });
}

// ========================================
// АДМИН ПАНЕЛЬ
// ========================================

function initAdminDashboard() {
    // Обновление статуса автомобиля
    const statusSelects = document.querySelectorAll('.status-select');
    statusSelects.forEach(select => {
        select.addEventListener('focus', function() {
            this.setAttribute('data-previous-status', this.value);
        });
        
        select.addEventListener('change', function() {
            const carId = this.dataset.carId;
            const newStatus = this.value;
            const previousStatus = this.getAttribute('data-previous-status');
            
            updateCarStatus(carId, newStatus, previousStatus, this);
        });
    });
    
    // Кнопки обслуживания
    const maintenanceButtons = document.querySelectorAll('.btn-maintenance');
    maintenanceButtons.forEach(button => {
        button.addEventListener('click', function() {
            const carId = this.dataset.carId;
            const inMaintenance = this.dataset.inMaintenance === 'true';
            toggleMaintenance(carId, inMaintenance);
        });
    });
    
    // AJAX формы
    setupAjaxForms();
}

function updateCarStatus(carId, newStatus, previousStatus, selectElement) {
    const statusText = {
        'AVAILABLE': 'Доступен',
        'RENTED': 'Арендован',
        'MAINTENANCE': 'На обслуживании',
        'RESERVED': 'Забронирован',
        'PENDING': 'Ожидает подтверждения'
    };

    const reason = prompt(`Укажите причину изменения статуса на "${statusText[newStatus]}" (необязательно):`);
    
    if (reason === null) {
        // Если пользователь отменил ввод причины, возвращаем предыдущее значение
        selectElement.value = previousStatus;
        return;
    }

    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    if (!token || !header) {
        showNotification('Ошибка: CSRF токен не найден', 'error');
        selectElement.value = previousStatus;
        return;
    }

    fetch(`/cars/${carId}/status`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [header]: token
        },
        body: JSON.stringify({ 
            status: newStatus,
            reason: reason
        })
    })
    .then(response => {
        if (!response.ok) {
            if (response.status === 403) {
                throw new Error('У вас нет прав для выполнения этой операции');
            }
            throw new Error('Ошибка при обновлении статуса');
        }
        return response.json();
    })
    .then(data => {
        if (data.error) {
            showNotification(data.error, 'error');
            selectElement.value = previousStatus;
        } else {
            showNotification(data.message || 'Статус успешно обновлен', 'success');
            selectElement.className = `status-select status-${newStatus.toLowerCase()}`;
            selectElement.setAttribute('data-previous-status', newStatus);
            
            // Обновляем кнопки обслуживания
            updateMaintenanceButtons(carId, newStatus);
            
            // Обновляем страницу через 1 секунду для отображения актуального состояния
            setTimeout(() => window.location.reload(), 1000);
        }
    })
    .catch(error => {
        console.error('Ошибка:', error);
        showNotification(error.message || 'Произошла ошибка при обновлении статуса', 'error');
        selectElement.value = previousStatus;
    });
}

function toggleMaintenance(carId, inMaintenance) {
    const reason = prompt(`Укажите причину ${inMaintenance ? 'перевода на обслуживание' : 'снятия с обслуживания'} (необязательно):`);
    
    if (reason === null) {
        return;
    }

    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    if (!token || !header) {
        showNotification('Ошибка: CSRF токен не найден', 'error');
        return;
    }

    fetch(`/cars/${carId}/maintenance?inMaintenance=${inMaintenance}&reason=${encodeURIComponent(reason)}`, {
        method: 'POST',
        headers: {
            [header]: token
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.error) {
            showNotification(data.error, 'error');
        } else {
            showNotification(data.message, 'success');
            setTimeout(() => window.location.reload(), 1000);
        }
    })
    .catch(error => {
        console.error('Ошибка:', error);
        showNotification('Произошла ошибка при обновлении статуса', 'error');
    });
}

function updateMaintenanceButtons(carId, status) {
    const maintenanceButton = document.querySelector(`[data-car-id="${carId}"].btn-warning`);
    const removeMaintenanceButton = document.querySelector(`[data-car-id="${carId}"].btn-success`);
    
    if (status === 'MAINTENANCE') {
        if (maintenanceButton) maintenanceButton.style.display = 'none';
        if (removeMaintenanceButton) removeMaintenanceButton.style.display = 'inline-block';
    } else {
        if (maintenanceButton) maintenanceButton.style.display = 'inline-block';
        if (removeMaintenanceButton) removeMaintenanceButton.style.display = 'none';
    }
}

// ========================================
// УПРАВЛЕНИЕ АРЕНДАМИ
// ========================================

function initRentalManagement() {
    // Формы отмены аренды
    const cancelForms = document.querySelectorAll('.cancel-form');
    cancelForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            const rentalId = this.dataset.rentalId;
            const formData = new FormData(this);
            submitCancelRequest(rentalId, formData);
        });
    });
    
    // Формы отклонения отмены
    const rejectForms = document.querySelectorAll('.reject-form');
    rejectForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            const rentalId = this.dataset.rentalId;
            const formData = new FormData(this);
            submitRejectRequest(rentalId, formData);
        });
    });
    
    // Калькулятор стоимости аренды
    initRentalCalculator();
}

function submitCancelRequest(rentalId, formData) {
    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    if (!token || !header) {
        showNotification('Ошибка: CSRF токен не найден', 'error');
        return;
    }

    fetch(`/rentals/${rentalId}/cancel`, {
        method: 'POST',
        body: formData,
        headers: {
            [header]: token
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showNotification(data.message, 'success');
            updateRentalStatus(rentalId, data.newStatus, data.statusText);
            hideCancelForm(rentalId);
        } else {
            showNotification(data.error, 'error');
        }
    })
    .catch(error => {
        console.error('Ошибка:', error);
        showNotification('Произошла ошибка при отмене аренды', 'error');
    });
}

function submitRejectRequest(rentalId, formData) {
    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    if (!token || !header) {
        showNotification('Ошибка: CSRF токен не найден', 'error');
        return;
    }

    fetch(`/rentals/${rentalId}/reject-cancellation`, {
        method: 'POST',
        body: formData,
        headers: {
            [header]: token
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showNotification(data.message, 'success');
            updateRentalStatus(rentalId, data.newStatus, data.statusText);
            hideRejectForm(rentalId);
        } else {
            showNotification(data.error, 'error');
        }
    })
    .catch(error => {
        console.error('Ошибка:', error);
        showNotification('Произошла ошибка при отклонении запроса', 'error');
    });
}

function initRentalCalculator() {
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');
    const costElement = document.getElementById('cost');
    const pricePerDay = parseFloat(document.getElementById('pricePerDay')?.value || 0);

    if (!startDateInput || !endDateInput || !costElement) return;

    function updateRentalCost() {
        const startDate = new Date(startDateInput.value);
        const endDate = new Date(endDateInput.value);

        if (startDate && endDate && !isNaN(startDate) && !isNaN(endDate)) {
            const diffTime = Math.abs(endDate - startDate);
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
            const totalCost = diffDays * pricePerDay;
            costElement.textContent = totalCost.toLocaleString('ru-RU');
            
            const durationDaysInput = document.getElementById('durationDays');
            if (durationDaysInput) {
                durationDaysInput.value = diffDays;
            }
        } else {
            costElement.textContent = '0';
            const durationDaysInput = document.getElementById('durationDays');
            if (durationDaysInput) {
                durationDaysInput.value = '1';
            }
        }
    }

    startDateInput.addEventListener('change', updateRentalCost);
    endDateInput.addEventListener('change', updateRentalCost);
}

// ========================================
// СИСТЕМА ПЛАТЕЖЕЙ
// ========================================

function initPaymentSystem() {
    // Обработка платежей
    const processButtons = document.querySelectorAll('.process-payment');
    processButtons.forEach(button => {
        button.addEventListener('click', function() {
            const paymentId = this.dataset.paymentId;
            processPayment(paymentId);
        });
    });
    
    // Повторная обработка платежей
    const retryButtons = document.querySelectorAll('.retry-payment');
    retryButtons.forEach(button => {
        button.addEventListener('click', function() {
            const paymentId = this.dataset.paymentId;
            retryPayment(paymentId);
        });
    });
}

function processPayment(paymentId) {
    if (!confirm('Обработать этот платеж?')) {
        return;
    }

    fetch(`/admin/payments/process/${paymentId}`, {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken
        }
    })
    .then(response => {
        // Проверяем тип контента ответа
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('text/html')) {
            // Если получили HTML вместо JSON, значит сессия истекла
            throw new Error('SESSION_EXPIRED');
        }
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        return response.json();
    })
    .then(data => {
        if (data.success) {
            showNotification('Платеж успешно обработан', 'success');
            setTimeout(() => window.location.reload(), 1000);
        } else {
            showNotification('Ошибка: ' + data.error, 'error');
        }
    })
    .catch(error => {
        console.error('Ошибка:', error);
        
        if (error.message === 'SESSION_EXPIRED') {
            showNotification('Сессия истекла. Перенаправление на страницу входа...', 'warning');
            setTimeout(() => {
                window.location.href = '/auth/login';
            }, 2000);
        } else {
            showNotification('Произошла ошибка при обработке платежа', 'error');
        }
    });
}

function retryPayment(paymentId) {
    if (!confirm('Повторить обработку этого платежа?')) {
        return;
    }

    fetch(`/admin/payments/process/${paymentId}`, {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken
        }
    })
    .then(response => {
        // Проверяем тип контента ответа
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('text/html')) {
            // Если получили HTML вместо JSON, значит сессия истекла
            throw new Error('SESSION_EXPIRED');
        }
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        return response.json();
    })
    .then(data => {
        if (data.success) {
            showNotification('Платеж успешно обработан', 'success');
            setTimeout(() => window.location.reload(), 1000);
        } else {
            showNotification('Ошибка: ' + data.error, 'error');
        }
    })
    .catch(error => {
        console.error('Ошибка:', error);
        
        if (error.message === 'SESSION_EXPIRED') {
            showNotification('Сессия истекла. Перенаправление на страницу входа...', 'warning');
            setTimeout(() => {
                window.location.href = '/auth/login';
            }, 2000);
        } else {
            showNotification('Произошла ошибка при обработке платежа', 'error');
        }
    });
}

function processAllUnprocessedPayments() {
    const statusDiv = document.getElementById('allPaymentsStatus');
    if (statusDiv) {
        statusDiv.innerHTML = '<span class="text-info">Запрос отправлен...</span>';
    }
    
    if (!csrfToken) {
        if (statusDiv) {
            statusDiv.innerHTML = '<span class="text-danger">CSRF-токен не найден. Пожалуйста, обновите страницу.</span>';
        }
        return;
    }
    
    fetch('/admin/payments/process-all', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        credentials: 'same-origin'
    })
    .then(response => {
        // Проверяем тип контента ответа
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('text/html')) {
            // Если получили HTML вместо JSON, значит сессия истекла
            throw new Error('SESSION_EXPIRED');
        }
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        return response.json();
    })
    .then(data => {
        if (data.success) {
            if (statusDiv) {
                statusDiv.innerHTML = '<span class="text-success">' + data.message + '</span>';
            }
            showNotification(data.message, 'success');
            setTimeout(() => window.location.reload(), 1500);
        } else {
            if (statusDiv) {
                statusDiv.innerHTML = '<span class="text-danger">' + (data.error || 'Ошибка') + '</span>';
            }
            showNotification('Ошибка: ' + (data.error || 'Неизвестная ошибка'), 'error');
        }
    })
    .catch(error => {
        if (error.message === 'SESSION_EXPIRED') {
            if (statusDiv) {
                statusDiv.innerHTML = '<span class="text-warning">Сессия истекла. Перенаправление на страницу входа...</span>';
            }
            showNotification('Сессия истекла. Перенаправление на страницу входа...', 'warning');
            setTimeout(() => window.location.href = '/login', 2000);
        } else {
            if (statusDiv) {
                statusDiv.innerHTML = '<span class="text-danger">Ошибка соединения: ' + error.message + '</span>';
            }
            showNotification('Ошибка соединения: ' + error.message, 'error');
        }
    });
}

function diagnosePayments() {
    const statusDiv = document.getElementById('diagnoseStatus');
    if (statusDiv) {
        statusDiv.innerHTML = '<span class="text-info">Диагностика запущена...</span>';
    }
    
    if (!csrfToken) {
        if (statusDiv) {
            statusDiv.innerHTML = '<span class="text-danger">CSRF-токен не найден. Пожалуйста, обновите страницу.</span>';
        }
        return;
    }
    
    fetch('/admin/payments/diagnose', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        credentials: 'same-origin'
    })
    .then(response => {
        // Проверяем тип контента ответа
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('text/html')) {
            // Если получили HTML вместо JSON, значит сессия истекла
            throw new Error('SESSION_EXPIRED');
        }
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        return response.json();
    })
    .then(data => {
        if (data.success) {
            if (statusDiv) {
                statusDiv.innerHTML = '<span class="text-success">' + data.message + '</span>';
            }
            showNotification(data.message, 'success');
        } else {
            if (statusDiv) {
                statusDiv.innerHTML = '<span class="text-danger">' + (data.error || 'Ошибка') + '</span>';
            }
            showNotification('Ошибка: ' + (data.error || 'Неизвестная ошибка'), 'error');
        }
    })
    .catch(error => {
        if (error.message === 'SESSION_EXPIRED') {
            if (statusDiv) {
                statusDiv.innerHTML = '<span class="text-warning">Сессия истекла. Перенаправление на страницу входа...</span>';
            }
            showNotification('Сессия истекла. Перенаправление на страницу входа...', 'warning');
            setTimeout(() => window.location.href = '/login', 2000);
        } else {
            if (statusDiv) {
                statusDiv.innerHTML = '<span class="text-danger">Ошибка соединения: ' + error.message + '</span>';
            }
            showNotification('Ошибка соединения: ' + error.message, 'error');
        }
    });
}

// ========================================
// СИСТЕМА УВЕДОМЛЕНИЙ
// ========================================

function initNotificationSystem() {
    // Создаем контейнер для уведомлений, если его нет
    if (!document.getElementById('notification-container')) {
        const container = document.createElement('div');
        container.id = 'notification-container';
        container.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999;
            max-width: 400px;
        `;
        document.body.appendChild(container);
    }
}

function showNotification(message, type = 'info', duration = 5000) {
    const container = document.getElementById('notification-container') || createNotificationContainer();
    
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.innerHTML = `
        <div class="notification-content">
            <span class="notification-message">${message}</span>
            <button class="notification-close" onclick="this.parentElement.parentElement.remove()">&times;</button>
        </div>
    `;
    
    // Добавляем стили для уведомления
    notification.style.cssText = `
        background-color: ${getNotificationColor(type)};
        color: white;
        padding: 15px 20px;
        border-radius: 8px;
        margin-bottom: 10px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        animation: slideIn 0.3s ease-out;
        max-width: 100%;
    `;
    
    container.appendChild(notification);
    
    // Автоматическое удаление
    setTimeout(() => {
        if (notification.parentElement) {
            notification.style.animation = 'slideOut 0.3s ease-in';
            setTimeout(() => {
                if (notification.parentElement) {
                    notification.remove();
                }
            }, 300);
        }
    }, duration);
}

function createNotificationContainer() {
    const container = document.createElement('div');
    container.id = 'notification-container';
    container.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 9999;
        max-width: 400px;
    `;
    document.body.appendChild(container);
    return container;
}

function getNotificationColor(type) {
    const colors = {
        success: '#28a745',
        error: '#dc3545',
        warning: '#ffc107',
        info: '#17a2b8'
    };
    return colors[type] || colors.info;
}

// ========================================
// AJAX ФОРМЫ
// ========================================

function setupAjaxForms() {
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', function(e) {
            // Исключаем logout-форму из AJAX-обработки
            if (this.action.includes('/auth/logout')) {
                return;
            }
            
            // Проверяем, есть ли data-ajax атрибут
            if (!this.hasAttribute('data-ajax')) {
                return;
            }
            
            e.preventDefault();
            
            const formData = new FormData(this);
            const rentalRow = this.closest('tr');
            const rentalId = rentalRow ? rentalRow.dataset.rentalId : null;

            fetch(this.action, {
                method: 'POST',
                body: formData,
                headers: {
                    [csrfHeader]: csrfToken
                }
            })
            .then(response => {
                // Проверяем тип контента ответа
                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('text/html')) {
                    // Если получили HTML вместо JSON, значит сессия истекла
                    throw new Error('SESSION_EXPIRED');
                }
                
                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}: ${response.statusText}`);
                }
                
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    showNotification(data.message, 'success');
                    if (rentalId) {
                        updateRentalRow(rentalId, data.newStatus);
                    }
                } else {
                    showNotification(data.message || 'Ошибка', 'error');
                }
            })
            .catch(error => {
                console.error('Ошибка:', error);
                
                if (error.message === 'SESSION_EXPIRED') {
                    showNotification('Сессия истекла. Перенаправление на страницу входа...', 'warning');
                    setTimeout(() => {
                        window.location.href = '/auth/login';
                    }, 2000);
                } else {
                    showNotification('Произошла ошибка при обработке запроса', 'error');
                }
            });
        });
    });
}

// ========================================
// ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ
// ========================================

function showCancelForm(rentalId) {
    const form = document.getElementById(`cancelForm-${rentalId}`);
    if (form) {
        form.style.display = 'block';
    }
}

function hideCancelForm(rentalId) {
    const form = document.getElementById(`cancelForm-${rentalId}`);
    if (form) {
        form.style.display = 'none';
    }
}

function showRejectForm(rentalId) {
    const form = document.getElementById(`rejectForm-${rentalId}`);
    if (form) {
        form.style.display = 'block';
    }
}

function hideRejectForm(rentalId) {
    const form = document.getElementById(`rejectForm-${rentalId}`);
    if (form) {
        form.style.display = 'none';
    }
}

function updateRentalStatus(rentalId, newStatus, message) {
    const rentalCard = document.querySelector(`[data-rental-id="${rentalId}"]`);
    if (rentalCard) {
        const statusBadge = rentalCard.querySelector('.status-badge');
        if (statusBadge) {
            statusBadge.className = `status-badge status-${newStatus.toLowerCase()}`;
            statusBadge.textContent = message;
        }
    }
}

function updateRentalRow(rentalId, newStatus) {
    const row = document.querySelector(`tr[data-rental-id="${rentalId}"]`);
    if (row) {
        const statusCell = row.querySelector('.status-cell');
        if (statusCell) {
            statusCell.className = `status-cell status-${newStatus.toLowerCase()}`;
            statusCell.textContent = getStatusText(newStatus);
        }
    }
}

function getStatusText(status) {
    const statusTexts = {
        'PENDING': 'Ожидает подтверждения',
        'ACTIVE': 'Активна',
        'COMPLETED': 'Завершена',
        'CANCELLED': 'Отменена',
        'PENDING_CANCELLATION': 'Запрос на отмену'
    };
    return statusTexts[status] || status;
}

function formatDate(dateString, withTime = false) {
    const date = new Date(dateString);

    if (!date || isNaN(date.getTime())) {
        return 'Не указана';
    }

    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();

    if (withTime) {
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        return `${day}.${month}.${year} ${hours}:${minutes}`;
    }

    return `${day}.${month}.${year}`;
}

// ========================================
// ГЛОБАЛЬНЫЕ ФУНКЦИИ (для совместимости)
// ========================================

// Функции для совместимости с существующим кодом
window.showMessage = showNotification;
window.updateCarStatus = updateCarStatus;
window.toggleMaintenance = toggleMaintenance;
window.showCancelForm = showCancelForm;
window.hideCancelForm = hideCancelForm;
window.showRejectForm = showRejectForm;
window.hideRejectForm = hideRejectForm;
window.processPayment = processPayment;
window.retryPayment = retryPayment;
window.processAllUnprocessedPayments = processAllUnprocessedPayments;
window.diagnosePayments = diagnosePayments;

// ========================================
// CSS АНИМАЦИИ
// ========================================

// Добавляем CSS анимации для уведомлений
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
    
    .notification-content {
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    
    .notification-close {
        background: none;
        border: none;
        color: white;
        font-size: 18px;
        cursor: pointer;
        margin-left: 10px;
        opacity: 0.7;
        transition: opacity 0.2s;
    }
    
    .notification-close:hover {
        opacity: 1;
    }
`;
document.head.appendChild(style);

console.log('AutoRent: JavaScript модули загружены');

// ========================================
// СТРАНИЦЫ АККАУНТА
// ========================================

/**
 * Инициализация страниц аккаунта
 */
function initAccountPages() {
    initProfilePage();
    initSettingsPage();
    initTransactionsPage();
}

/**
 * Инициализация страницы профиля
 */
function initProfilePage() {
    const profileContainer = document.querySelector('.profile-container');
    if (!profileContainer) return;

    // Анимация загрузки данных профиля
    animateProfileLoading();
    
    // Инициализация быстрых действий
    initQuickActions();
    
    // Обновление баланса в реальном времени
    initBalanceUpdates();
}

/**
 * Анимация загрузки профиля
 */
function animateProfileLoading() {
    const profileElements = document.querySelectorAll('.profile-avatar, .profile-info, .balance-card');
    
    profileElements.forEach((element, index) => {
        element.style.opacity = '0';
        element.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            element.style.transition = 'all 0.5s ease';
            element.style.opacity = '1';
            element.style.transform = 'translateY(0)';
        }, index * 200);
    });
}

/**
 * Инициализация быстрых действий
 */
function initQuickActions() {
    const quickActionBtns = document.querySelectorAll('.quick-action-btn');
    
    quickActionBtns.forEach(btn => {
        btn.addEventListener('click', function(e) {
            // Добавляем эффект нажатия
            this.style.transform = 'scale(0.95)';
            setTimeout(() => {
                this.style.transform = '';
            }, 150);
        });
    });
}

/**
 * Обновление баланса в реальном времени
 */
function initBalanceUpdates() {
    const balanceElement = document.querySelector('.balance-amount');
    if (!balanceElement) return;

    // Обновляем баланс каждые 30 секунд
    setInterval(() => {
        updateAccountBalance();
    }, 30000);
}

/**
 * Обновление баланса аккаунта
 */
function updateAccountBalance() {
    fetch('/account/api/me', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        }
    })
    .then(response => response.json())
    .then(data => {
        const balanceElement = document.querySelector('.balance-amount');
        if (balanceElement) {
            balanceElement.textContent = data.balance + ' ₽';
        }
    })
    .catch(error => {
        console.error('Ошибка при обновлении баланса:', error);
    });
}

/**
 * Инициализация страницы настроек
 */
function initSettingsPage() {
    const settingsContainer = document.querySelector('.settings-container');
    if (!settingsContainer) return;

    // Валидация формы изменения пароля
    initPasswordChangeValidation();
    
    // Подтверждение удаления аккаунта
    initAccountDeletionConfirmation();
    
    // Автосохранение изменений
    initAutoSave();
}

/**
 * Валидация формы изменения пароля
 */
function initPasswordChangeValidation() {
    const passwordForm = document.querySelector('form[action*="change-password"]');
    if (!passwordForm) return;

    const currentPassword = passwordForm.querySelector('#currentPassword');
    const newPassword = passwordForm.querySelector('#newPassword');
    const confirmPassword = passwordForm.querySelector('#confirmPassword');

    function validatePasswords() {
        const newPass = newPassword.value;
        const confirmPass = confirmPassword.value;
        
        if (newPass !== confirmPass) {
            confirmPassword.setCustomValidity('Пароли не совпадают');
        } else if (newPass.length < 6) {
            newPassword.setCustomValidity('Пароль должен содержать не менее 6 символов');
        } else {
            newPassword.setCustomValidity('');
            confirmPassword.setCustomValidity('');
        }
    }

    newPassword.addEventListener('input', validatePasswords);
    confirmPassword.addEventListener('input', validatePasswords);
}

/**
 * Подтверждение удаления аккаунта
 */
function initAccountDeletionConfirmation() {
    const deleteAccountBtn = document.querySelector('button[data-bs-target="#deleteAccountModal"]');
    const deleteAccountForm = document.querySelector('form[action*="delete-account"]');
    
    if (deleteAccountBtn && deleteAccountForm) {
        deleteAccountBtn.addEventListener('click', function(e) {
            // Дополнительная проверка
            const confirmed = confirm('Вы уверены, что хотите удалить свой аккаунт? Это действие нельзя отменить.');
            if (!confirmed) {
                e.preventDefault();
                return false;
            }
        });
    }
}

/**
 * Автосохранение изменений в настройках
 */
function initAutoSave() {
    const profileForm = document.querySelector('form[action*="update-profile"]');
    if (!profileForm) return;

    const formInputs = profileForm.querySelectorAll('input[type="text"], input[type="tel"]');
    let autoSaveTimeout;

    formInputs.forEach(input => {
        input.addEventListener('input', function() {
            clearTimeout(autoSaveTimeout);
            autoSaveTimeout = setTimeout(() => {
                showNotification('Изменения сохранены автоматически', 'success', 2000);
            }, 2000);
        });
    });
}

/**
 * Инициализация страницы истории транзакций
 */
function initTransactionsPage() {
    const transactionsContainer = document.querySelector('.transactions-container');
    if (!transactionsContainer) return;

    // Фильтрация транзакций
    initTransactionFilters();
    
    // Экспорт транзакций
    initTransactionExport();
    
    // Пагинация транзакций
    initTransactionPagination();
}

/**
 * Фильтрация транзакций
 */
function initTransactionFilters() {
    const filterForm = document.querySelector('.transaction-filters form');
    if (!filterForm) return;

    const filterInputs = filterForm.querySelectorAll('select, input');
    
    filterInputs.forEach(input => {
        input.addEventListener('change', function() {
            // Автоматическая отправка формы при изменении фильтров
            filterForm.submit();
        });
    });
}

/**
 * Экспорт транзакций
 */
function initTransactionExport() {
    const exportBtn = document.createElement('button');
    exportBtn.className = 'btn btn-outline-success btn-sm';
    exportBtn.innerHTML = '<i class="fas fa-download"></i> Экспорт';
    exportBtn.addEventListener('click', exportTransactions);
    
    const filterForm = document.querySelector('.transaction-filters');
    if (filterForm) {
        filterForm.appendChild(exportBtn);
    }
}

/**
 * Экспорт транзакций в CSV
 */
function exportTransactions() {
    const table = document.querySelector('.transaction-table table');
    if (!table) return;

    let csv = 'Дата,Описание,Сумма,Тип,Статус\n';
    const rows = table.querySelectorAll('tbody tr');
    
    rows.forEach(row => {
        const cells = row.querySelectorAll('td');
        const rowData = Array.from(cells).map(cell => {
            let text = cell.textContent.trim();
            // Экранируем кавычки в CSV
            if (text.includes(',')) {
                text = '"' + text.replace(/"/g, '""') + '"';
            }
            return text;
        });
        csv += rowData.join(',') + '\n';
    });
    
    // Создаем и скачиваем файл
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', 'transactions.csv');
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

/**
 * Пагинация транзакций
 */
function initTransactionPagination() {
    const paginationContainer = document.querySelector('.pagination');
    if (!paginationContainer) return;

    const paginationLinks = paginationContainer.querySelectorAll('a');
    
    paginationLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const page = this.getAttribute('data-page');
            if (page) {
                loadTransactionsPage(page);
            }
        });
    });
}

/**
 * Загрузка страницы транзакций
 */
function loadTransactionsPage(page) {
    const currentUrl = new URL(window.location);
    currentUrl.searchParams.set('page', page);
    
    window.location.href = currentUrl.toString();
}

/**
 * Форматирование суммы транзакции
 */
function formatTransactionAmount(amount, type) {
    const formattedAmount = parseFloat(amount).toLocaleString('ru-RU');
    const sign = type === 'DEPOSIT' ? '+' : '-';
    return sign + formattedAmount + ' ₽';
}

/**
 * Получение цвета для типа транзакции
 */
function getTransactionTypeColor(type) {
    const colors = {
        'DEPOSIT': 'success',
        'WITHDRAWAL': 'danger',
        'TRANSFER': 'info'
    };
    return colors[type] || 'secondary';
}

// ========================================
// УТИЛИТЫ ДЛЯ АККАУНТА
// ========================================

/**
 * Форматирование даты для отображения
 */
function formatAccountDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

/**
 * Проверка валидности телефона
 */
function validatePhone(phone) {
    const phonePattern = /^\+7\(\d{3}\)\d{3}-\d{2}-\d{2}$/;
    return phonePattern.test(phone);
}

/**
 * Форматирование телефона
 */
function formatPhone(phone) {
    // Удаляем все нецифровые символы
    const digits = phone.replace(/\D/g, '');
    
    if (digits.length === 11) {
        return `+7(${digits.slice(1, 4)})${digits.slice(4, 7)}-${digits.slice(7, 9)}-${digits.slice(9, 11)}`;
    }
    
    return phone;
}

/**
 * Инициализация подтверждения удаления автомобиля
 */
function initCarDeletionConfirmation() {
    const deleteForms = document.querySelectorAll('.delete-car-form');
    deleteForms.forEach(form => {
        form.addEventListener('submit', function(event) {
            const confirmation = confirm('Вы уверены, что хотите удалить этот автомобиль?');
            if (!confirmation) {
                event.preventDefault();
            }
        });
    });
}

/**
 * Инициализация отображения формы отклонения отмены аренды
 */
function initRejectCancellationForm() {
    const rejectButtons = document.querySelectorAll('.reject-cancellation-btn');
    rejectButtons.forEach(button => {
        button.addEventListener('click', function() {
            const rentalId = this.getAttribute('data-rental-id');
            const rejectForm = document.getElementById('rejectForm-' + rentalId);
            if (rejectForm) {
                rejectForm.classList.toggle('d-none');
            }
        });
    });
}

/**
 * Инициализация переключения статуса пользователя (вкл/выкл)
 */
function initUserStatusToggle() {
    document.body.addEventListener('click', function(event) {
        const disableBtn = event.target.closest('.disable-user-btn');
        const enableBtn = event.target.closest('.enable-user-btn');

        if (disableBtn) {
            const userId = disableBtn.getAttribute('data-user-id');
            if (confirm('Вы уверены, что хотите отключить этого пользователя?')) {
                toggleUserStatus(userId, 'disable');
            }
        }

        if (enableBtn) {
            const userId = enableBtn.getAttribute('data-user-id');
            if (confirm('Вы уверены, что хотите активировать этого пользователя?')) {
                toggleUserStatus(userId, 'enable');
            }
        }
    });
}

function toggleUserStatus(userId, action) {
    // CSRF токены должны быть инициализированы в initCSRF()
    if (!csrfToken || !csrfHeader) {
        console.error('CSRF token not found');
        return;
    }

    fetch(`/admin/users/${userId}/${action}`, {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken
        }
    })
    .then(response => {
        if (response.ok) {
            location.reload();
        } else {
            console.error('Failed to update user status');
            alert('Не удалось обновить статус пользователя.');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Произошла ошибка.');
    });
}

/* =============================
   МОДУЛЬ: Все аренды (admin-rentals)
   ============================= */
(function() {
    // Получаем CSRF-токен из мета-тегов
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    // Функция для отображения сообщений
    function showMessage(type, message) {
        const container = document.getElementById('message-container');
        const alert = document.createElement('div');
        alert.className = `alert alert-${type} alert-dismissible fade show`;
        alert.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;
        container.appendChild(alert);
        setTimeout(() => { alert.remove(); }, 5000);
    }

    // Функция для обновления статуса аренды на странице
    function updateRentalStatus(rentalId, newStatus, message) {
        const rentalCard = document.querySelector(`[data-rental-id="${rentalId}"]`);
        if (rentalCard) {
            const statusBadge = rentalCard.querySelector('.status-badge');
            if (statusBadge) {
                statusBadge.className = `status-badge status-${newStatus.toLowerCase()}`;
                statusBadge.textContent = message;
            }
        }
    }

    // Функции для показа/скрытия форм
    window.showCancelForm = function(rentalId) {
        const form = document.getElementById(`cancelForm-${rentalId}`);
        if (form) form.style.display = 'block';
    };
    window.hideCancelForm = function(rentalId) {
        const form = document.getElementById(`cancelForm-${rentalId}`);
        if (form) form.style.display = 'none';
    };
    window.showRejectForm = function(rentalId) {
        const form = document.getElementById(`rejectForm-${rentalId}`);
        if (form) form.style.display = 'block';
    };
    window.hideRejectForm = function(rentalId) {
        const form = document.getElementById(`rejectForm-${rentalId}`);
        if (form) form.style.display = 'none';
    };

    document.addEventListener('DOMContentLoaded', function() {
        // Создаем контейнер для сообщений, если его нет
        if (!document.getElementById('message-container')) {
            const container = document.createElement('div');
            container.id = 'message-container';
            container.style.position = 'fixed';
            container.style.top = '20px';
            container.style.right = '20px';
            container.style.zIndex = '1000';
            document.body.appendChild(container);
        }
        // Преобразуем все формы в AJAX-запросы
        document.querySelectorAll('form').forEach(form => {
            form.addEventListener('submit', async function(e) {
                if (!form.closest('.rentals-list')) return; // Только для форм на странице all-rentals
                e.preventDefault();
                const formData = new FormData(form);
                const url = form.action;
                const method = form.method;
                try {
                    const response = await fetch(url, {
                        method: method,
                        body: formData,
                        headers: {
                            'X-Requested-With': 'XMLHttpRequest',
                            [csrfHeader]: csrfToken
                        }
                    });
                    const data = await response.json();
                    if (data.success) {
                        showMessage('success', data.message);
                        if (data.newStatus) {
                            updateRentalStatus(formData.get('id'), data.newStatus, data.message);
                        }
                        // Скрываем форму отмены/отклонения
                        const rentalId = url.split('/').pop();
                        hideCancelForm(rentalId);
                        hideRejectForm(rentalId);
                        if (url.includes('/cancel') || url.includes('/reject-cancellation')) {
                            setTimeout(() => { window.location.reload(); }, 2000);
                        }
                    } else {
                        showMessage('danger', data.message || 'Произошла ошибка');
                    }
                } catch (error) {
                    console.error('Error:', error);
                    showMessage('danger', 'Произошла ошибка при обработке запроса');
                }
            });
        });
    });
})();

/* =============================
   УНИВЕРСАЛЬНЫЕ JS-ФУНКЦИИ ДЛЯ ШАБЛОНОВ
   ============================= */
(function() {
    // Универсальное скрытие/отображение по классу
    window.toggleVisibility = function(selector) {
        document.querySelectorAll(selector).forEach(el => {
            el.classList.toggle('is-hidden');
        });
    };
    // Показать элемент
    window.showElement = function(selector) {
        document.querySelectorAll(selector).forEach(el => {
            el.classList.remove('is-hidden');
        });
    };
    // Скрыть элемент
    window.hideElement = function(selector) {
        document.querySelectorAll(selector).forEach(el => {
            el.classList.add('is-hidden');
        });
    };
    // Универсальный обработчик для форм с классом .ajax-form
    document.addEventListener('DOMContentLoaded', function() {
        document.querySelectorAll('form.ajax-form').forEach(form => {
            form.addEventListener('submit', async function(e) {
                e.preventDefault();
                const formData = new FormData(form);
                const url = form.action;
                const method = form.method;
                const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
                try {
                    const response = await fetch(url, {
                        method: method,
                        body: formData,
                        headers: {
                            'X-Requested-With': 'XMLHttpRequest',
                            [csrfHeader]: csrfToken
                        }
                    });
                    const data = await response.json();
                    if (data.success) {
                        window.showMessage && window.showMessage('success', data.message);
                        if (data.reload) setTimeout(() => window.location.reload(), 1500);
                    } else {
                        window.showMessage && window.showMessage('danger', data.message || 'Ошибка');
                    }
                } catch (error) {
                    window.showMessage && window.showMessage('danger', 'Ошибка при отправке формы');
                }
            });
        });
    });
    // Универсальная функция для всплывающих сообщений
    window.showMessage = function(type, message) {
        let container = document.getElementById('message-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'message-container';
            container.style.position = 'fixed';
            container.style.top = '20px';
            container.style.right = '20px';
            container.style.zIndex = '1000';
            document.body.appendChild(container);
        }
        const alert = document.createElement('div');
        alert.className = `alert alert-${type} alert-dismissible fade show`;
        alert.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;
        container.appendChild(alert);
        setTimeout(() => { alert.remove(); }, 5000);
    };
})();

/**
 * Инициализация страницы редактирования автомобиля
 */
function initCarEditPage() {
    // Проверяем, что мы на странице редактирования автомобиля
    const carForm = document.querySelector('.car-form');
    if (!carForm) return;

    // Обработчики для кнопок поворота
    document.querySelectorAll('.rotate-left, .rotate-right').forEach(btn => {
        btn.addEventListener('click', function() {
            const imageId = this.getAttribute('data-image-id');
            const direction = this.classList.contains('rotate-left') ? 'left' : 'right';
            rotateImage(imageId, direction);
        });
    });

    // Обработчики для кнопок удаления изображений
    document.querySelectorAll('.delete-image').forEach(btn => {
        btn.addEventListener('click', function() {
            const imageId = this.getAttribute('data-image-id');
            deleteImage(imageId);
        });
    });

    // После успешного поворота обновляем угол и класс
    // (доработка функции rotateImage ниже)
}

/**
 * Инициализация функционала восстановления пароля
 */
function initPasswordReset() {
    // Проверяем, что мы на странице восстановления пароля
    const forgotPasswordForm = document.querySelector('form[action*="forgot-password"]');
    const resetPasswordForm = document.querySelector('form[action*="reset-password"]');
    
    if (forgotPasswordForm) {
        initForgotPasswordForm();
    }
    
    if (resetPasswordForm) {
        initResetPasswordForm();
    }
}

/**
 * Инициализация формы "Забыли пароль"
 */
function initForgotPasswordForm() {
    const form = document.querySelector('form[action*="forgot-password"]');
    const emailInput = document.getElementById('email');
    const submitBtn = form.querySelector('button[type="submit"]');
    
    if (!form || !emailInput || !submitBtn) return;
    
    // Валидация email в реальном времени
    emailInput.addEventListener('input', function() {
        const email = this.value.trim();
        const isValid = validateEmail(email);
        
        if (email && !isValid) {
            this.classList.add('is-invalid');
            this.classList.remove('is-valid');
        } else if (email && isValid) {
            this.classList.remove('is-invalid');
            this.classList.add('is-valid');
        } else {
            this.classList.remove('is-invalid', 'is-valid');
        }
        
        updateSubmitButton();
    });
    
    // Обработка отправки формы
    form.addEventListener('submit', function(e) {
        const email = emailInput.value.trim();
        
        if (!validateEmail(email)) {
            e.preventDefault();
            showNotification('Пожалуйста, введите корректный email', 'error');
            return;
        }
        
        // Показываем индикатор загрузки
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Отправка...';
    });
    
    function validateEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }
    
    function updateSubmitButton() {
        const email = emailInput.value.trim();
        const isValid = validateEmail(email);
        submitBtn.disabled = !isValid;
    }
}

/**
 * Инициализация формы сброса пароля
 */
function initResetPasswordForm() {
    const form = document.querySelector('form[action*="reset-password"]');
    const newPasswordInput = document.getElementById('newPassword');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const submitBtn = form.querySelector('button[type="submit"]');
    const tokenInput = form.querySelector('input[name="token"]');
    
    if (!form || !newPasswordInput || !confirmPasswordInput || !submitBtn) return;
    
    // Валидация пароля в реальном времени
    newPasswordInput.addEventListener('input', function() {
        validatePassword(this.value);
        validatePasswordMatch();
        updateSubmitButton();
    });
    
    confirmPasswordInput.addEventListener('input', function() {
        validatePasswordMatch();
        updateSubmitButton();
    });
    
    // Обработка отправки формы
    form.addEventListener('submit', function(e) {
        const newPassword = newPasswordInput.value;
        const confirmPassword = confirmPasswordInput.value;
        
        if (!validatePassword(newPassword)) {
            e.preventDefault();
            showNotification('Пароль не соответствует требованиям безопасности', 'error');
            return;
        }
        
        if (newPassword !== confirmPassword) {
            e.preventDefault();
            showNotification('Пароли не совпадают', 'error');
            return;
        }
        
        // Показываем индикатор загрузки
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Сохранение...';
    });
    
    function validatePassword(password) {
        const minLength = 8;
        const hasUpperCase = /[A-Z]/.test(password);
        const hasLowerCase = /[a-z]/.test(password);
        const hasNumbers = /\d/.test(password);
        const hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(password);
        
        const isValid = password.length >= minLength && 
                       (hasUpperCase || hasLowerCase) && 
                       (hasNumbers || hasSpecialChar);
        
        // Обновляем визуальные индикаторы
        const strengthMeter = document.querySelector('.password-strength-meter');
        if (strengthMeter) {
            const strengthBar = strengthMeter.querySelector('.strength-bar');
            if (strengthBar) {
                let strength = 0;
                if (password.length >= minLength) strength++;
                if (hasUpperCase && hasLowerCase) strength++;
                if (hasNumbers) strength++;
                if (hasSpecialChar) strength++;
                
                strengthBar.className = 'strength-bar';
                if (strength >= 4) {
                    strengthBar.classList.add('strength-very-strong');
                } else if (strength >= 3) {
                    strengthBar.classList.add('strength-strong');
                } else if (strength >= 2) {
                    strengthBar.classList.add('strength-medium');
                } else if (strength >= 1) {
                    strengthBar.classList.add('strength-weak');
                }
            }
        }
        
        return isValid;
    }
    
    function validatePasswordMatch() {
        const newPassword = newPasswordInput.value;
        const confirmPassword = confirmPasswordInput.value;
        
        if (confirmPassword && newPassword !== confirmPassword) {
            confirmPasswordInput.classList.add('is-invalid');
            confirmPasswordInput.classList.remove('is-valid');
        } else if (confirmPassword && newPassword === confirmPassword) {
            confirmPasswordInput.classList.remove('is-invalid');
            confirmPasswordInput.classList.add('is-valid');
        } else {
            confirmPasswordInput.classList.remove('is-invalid', 'is-valid');
        }
    }
    
    function updateSubmitButton() {
        const newPassword = newPasswordInput.value;
        const confirmPassword = confirmPasswordInput.value;
        const isPasswordValid = validatePassword(newPassword);
        const doPasswordsMatch = newPassword === confirmPassword;
        
        submitBtn.disabled = !(isPasswordValid && doPasswordsMatch);
    }
}

// ========================================
// ГЛОБАЛЬНЫЕ ФУНКЦИИ ДЛЯ HTML
// ========================================

// Делаем функции доступными глобально для использования в HTML
window.processPayment = processPayment;
window.retryPayment = retryPayment;
window.processAllUnprocessedPayments = processAllUnprocessedPayments;
window.diagnosePayments = diagnosePayments;
window.showCancelForm = showCancelForm;
window.hideCancelForm = hideCancelForm;
window.showRejectForm = showRejectForm;
window.hideRejectForm = hideRejectForm;
window.updateCarStatus = updateCarStatus;
window.toggleMaintenance = toggleMaintenance;
window.toggleUserStatus = toggleUserStatus;
window.rotateImage = rotateImage;
window.deleteImage = deleteImage;

// ========================================
// ЗАГРУЗКА ИЗОБРАЖЕНИЙ АВТОМОБИЛЕЙ
// ========================================

function initCarImageUpload() {
    const imageInput = document.getElementById('newImages');
    const previewContainer = document.getElementById('imagePreviewContainer');
    
    if (!imageInput || !previewContainer) return;
    
    imageInput.addEventListener('change', function(e) {
        const files = Array.from(e.target.files);
        
        // Очищаем предыдущие превью
        previewContainer.innerHTML = '';
        
        files.forEach((file, index) => {
            if (file.type.startsWith('image/')) {
                const reader = new FileReader();
                const previewDiv = document.createElement('div');
                previewDiv.className = 'image-preview-item';
                
                reader.onload = function(e) {
                    previewDiv.innerHTML = `
                        <img src="${e.target.result}" alt="Превью ${index + 1}" class="img-thumbnail">
                        <div class="image-preview-info">
                            <span class="file-name">${file.name}</span>
                            <span class="file-size">${formatFileSize(file.size)}</span>
                            ${index === 0 ? '<span class="main-indicator">Основное фото</span>' : ''}
                        </div>
                        <button type="button" class="btn-remove-image" onclick="removeImagePreview(this)">
                            <i class="fas fa-times"></i>
                        </button>
                    `;
                };
                
                reader.readAsDataURL(file);
                previewContainer.appendChild(previewDiv);
            }
        });
    });
}

function removeImagePreview(button) {
    const previewItem = button.closest('.image-preview-item');
    const previewContainer = document.getElementById('imagePreviewContainer');
    
    if (previewItem) {
        previewItem.remove();
        
        // Обновляем индикаторы основного фото
        const remainingPreviews = previewContainer.querySelectorAll('.image-preview-item');
        remainingPreviews.forEach((preview, index) => {
            const mainIndicator = preview.querySelector('.main-indicator');
            if (index === 0) {
                if (!mainIndicator) {
                    const info = preview.querySelector('.image-preview-info');
                    const indicator = document.createElement('span');
                    indicator.className = 'main-indicator';
                    indicator.textContent = 'Основное фото';
                    info.appendChild(indicator);
                }
            } else {
                if (mainIndicator) {
                    mainIndicator.remove();
                }
            }
        });
    }
}

function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

// ========================================
// ВАЛИДАЦИЯ ФОРМ АВТОМОБИЛЕЙ
// ========================================

function initCarFormValidation() {
    const carForm = document.querySelector('.car-form');
    if (!carForm) return;
    
    const dailyRateInput = document.getElementById('dailyRate');
    const licensePlateInput = document.getElementById('licensePlate');
    const yearInput = document.getElementById('year');
    
    // Валидация стоимости аренды
    if (dailyRateInput) {
        dailyRateInput.addEventListener('input', function() {
            const value = parseFloat(this.value);
            if (value <= 0) {
                this.setCustomValidity('Стоимость аренды должна быть больше нуля');
            } else {
                this.setCustomValidity('');
            }
        });
    }
    
    // Валидация номера автомобиля
    if (licensePlateInput) {
        licensePlateInput.addEventListener('input', function() {
            const value = this.value.toUpperCase();
            this.value = value;
            
            // Проверяем формат номера
            const plateRegex = /^[АВЕКМНОРСТУХ]\d{3}[АВЕКМНОРСТУХ]{2}\d{2,3}$/;
            if (value && !plateRegex.test(value)) {
                this.setCustomValidity('Неверный формат номера. Пример: А000АА000');
            } else {
                this.setCustomValidity('');
            }
        });
    }
    
    // Валидация года выпуска
    if (yearInput) {
        yearInput.addEventListener('input', function() {
            const value = parseInt(this.value);
            const currentYear = new Date().getFullYear();
            
            if (value < 1900 || value > currentYear + 1) {
                this.setCustomValidity(`Год должен быть между 1900 и ${currentYear + 1}`);
            } else {
                this.setCustomValidity('');
            }
        });
    }
    
    // Валидация при отправке формы
    carForm.addEventListener('submit', function(e) {
        if (!this.checkValidity()) {
            e.preventDefault();
            e.stopPropagation();
            
            // Показываем ошибки
            showNotification('Пожалуйста, исправьте ошибки в форме', 'error');
        }
        
        this.classList.add('was-validated');
    });
}

// ========================================
// УПРАВЛЕНИЕ ИЗОБРАЖЕНИЯМИ В РЕДАКТИРОВАНИИ
// ========================================

function initCarImageManagement() {
    // Поворот изображений
    const rotateButtons = document.querySelectorAll('.rotate-left, .rotate-right');
    rotateButtons.forEach(button => {
        button.addEventListener('click', function() {
            const imageId = this.getAttribute('data-image-id');
            const direction = this.classList.contains('rotate-left') ? 'left' : 'right';
            rotateCarImage(imageId, direction);
        });
    });
    
    // Удаление изображений
    const deleteButtons = document.querySelectorAll('.delete-image');
    deleteButtons.forEach(button => {
        button.addEventListener('click', function() {
            const imageId = this.getAttribute('data-image-id');
            if (confirm('Вы уверены, что хотите удалить это изображение?')) {
                deleteCarImage(imageId);
            }
        });
    });
}

// ========================================
// ОБНОВЛЕНИЕ ИНИЦИАЛИЗАЦИИ
// ========================================

// Обновляем основную функцию инициализации
document.addEventListener('DOMContentLoaded', function() {
    console.log('AutoRent: Инициализация приложения...');
    
    // Инициализация всех компонентов
    initCSRF();
    initNavigation();
    initAnimations();
    initFormValidation();
    initCarGallery();
    initAdminDashboard();
    initRentalManagement();
    initPaymentSystem();
    initNotificationSystem();
    initAccountPages();
    initCarDeletionConfirmation();
    initRejectCancellationForm();
    initUserStatusToggle();
    initCarEditPage();
    initPasswordReset();
    
    // Новые функции для автомобилей
    initCarImageUpload();
    initCarFormValidation();
    initCarImageManagement();
    
    // Глобальный обработчик ошибок
    initGlobalErrorHandler();
    
    console.log('AutoRent: Приложение инициализировано');
}); 

// ========================================
// ГАЛЕРЕЯ ИЗОБРАЖЕНИЙ АВТОМОБИЛЯ
// ========================================

function initCarDetailGallery() {
    const mainImage = document.getElementById('mainCarImage');
    const thumbnails = document.querySelectorAll('.thumbnail');
    
    if (!mainImage || thumbnails.length === 0) return;
    
    // Обработчик клика по миниатюрам
    thumbnails.forEach(thumbnail => {
        thumbnail.addEventListener('click', function() {
            const img = this.querySelector('img');
            if (img && mainImage) {
                // Обновляем основное изображение
                mainImage.src = img.src;
                
                // Обновляем активную миниатюру
                thumbnails.forEach(t => t.classList.remove('active'));
                this.classList.add('active');
            }
        });
    });
    
    // Устанавливаем первую миниатюру как активную
    if (thumbnails.length > 0) {
        thumbnails[0].classList.add('active');
    }
}

// ========================================
// ВАЛИДАЦИЯ ДАТ АРЕНДЫ
// ========================================

function initRentalDateValidation() {
    const startInput = document.getElementById('startDate');
    const endInput = document.getElementById('endDate');
    const rentBtn = document.getElementById('rentBtn');
    const warning = document.getElementById('dateWarning');
    
    if (!startInput || !endInput || !rentBtn || !warning) return;
    
    // Получаем периоды бронирования из data-атрибута
    const bookedPeriodsData = document.querySelector('[data-booked-periods]');
    let bookedPeriods = [];
    
    if (bookedPeriodsData) {
        try {
            bookedPeriods = JSON.parse(bookedPeriodsData.getAttribute('data-booked-periods'));
        } catch (e) {
            console.error('Ошибка парсинга периодов бронирования:', e);
        }
    }
    
    function parsePeriods(periods) {
        return periods.map(p => ({
            start: new Date(p.startDate),
            end: new Date(p.endDate)
        }));
    }
    
    const periods = parsePeriods(bookedPeriods);
    
    function isOverlapping(start, end) {
        return periods.some(p => !(end <= p.start || start >= p.end));
    }
    
    function validateDates() {
        const start = new Date(startInput.value);
        const end = new Date(endInput.value);
        
        if (start && end && !isNaN(start) && !isNaN(end)) {
            // Проверяем, что дата окончания больше даты начала
            if (end <= start) {
                rentBtn.disabled = true;
                warning.textContent = 'Дата окончания должна быть позже даты начала!';
                warning.style.display = 'block';
                return;
            }
            
            // Проверяем, что дата начала не в прошлом
            const now = new Date();
            if (start < now) {
                rentBtn.disabled = true;
                warning.textContent = 'Дата начала не может быть в прошлом!';
                warning.style.display = 'block';
                return;
            }
            
            // Проверяем пересечение с забронированными периодами
            if (isOverlapping(start, end)) {
                rentBtn.disabled = true;
                warning.textContent = 'Выбранные даты пересекаются с уже забронированными!';
                warning.style.display = 'block';
            } else {
                rentBtn.disabled = false;
                warning.style.display = 'none';
            }
        } else {
            rentBtn.disabled = false;
            warning.style.display = 'none';
        }
    }
    
    // Добавляем обработчики событий
    startInput.addEventListener('change', validateDates);
    endInput.addEventListener('change', validateDates);
    
    // Устанавливаем минимальную дату для полей
    const now = new Date();
    const nowString = now.toISOString().slice(0, 16);
    startInput.min = nowString;
    endInput.min = nowString;
    
    // Обновляем минимальную дату окончания при изменении даты начала
    startInput.addEventListener('change', function() {
        if (this.value) {
            endInput.min = this.value;
            // Если дата окончания меньше новой даты начала, очищаем её
            if (endInput.value && endInput.value <= this.value) {
                endInput.value = '';
            }
        }
    });
}

// ========================================
// ДОПОЛНИТЕЛЬНАЯ ИНИЦИАЛИЗАЦИЯ
// ========================================

// Добавляем новые функции в основную инициализацию
document.addEventListener('DOMContentLoaded', function() {
    // ... existing initialization code ...
    
    // Инициализация новых компонентов
    initCarDetailGallery();
    initRentalDateValidation();
    
    console.log('AutoRent: Дополнительные компоненты инициализированы');
}); 

// ========================================
// ГЛОБАЛЬНЫЙ ОБРАБОТЧИК ОШИБОК
// ========================================

function initGlobalErrorHandler() {
    // Обработчик для AJAX запросов
    const originalFetch = window.fetch;
    window.fetch = function(...args) {
        return originalFetch.apply(this, args).then(response => {
            if (!response.ok) {
                // Если это JSON ответ с ошибкой
                if (response.headers.get('content-type')?.includes('application/json')) {
                    return response.json().then(errorData => {
                        // Показываем уведомление с ошибкой
                        const errorMessage = errorData.message || errorData.details || 'Произошла ошибка';
                        showNotification(errorMessage, 'error');
                        throw new Error(errorMessage);
                    });
                } else {
                    // Для не-JSON ответов (например, 404)
                    let errorMessage = 'Произошла ошибка';
                    if (response.status === 404) {
                        errorMessage = 'Страница не найдена. Проверьте правильность URL адреса.';
                    } else if (response.status === 403) {
                        errorMessage = 'Доступ запрещен. У вас нет прав для выполнения этой операции.';
                    } else if (response.status === 401) {
                        errorMessage = 'Необходима авторизация. Пожалуйста, войдите в систему.';
                    } else if (response.status >= 500) {
                        errorMessage = 'Ошибка сервера. Пожалуйста, попробуйте позже.';
                    }
                    showNotification(errorMessage, 'error');
                    throw new Error(errorMessage);
                }
            }
            return response;
        });
    };

    // Обработчик для глобальных ошибок JavaScript
    window.addEventListener('error', function(event) {
        console.error('Глобальная ошибка JavaScript:', event.error);
        showNotification('Произошла ошибка в приложении. Пожалуйста, обновите страницу.', 'error');
    });

    // Обработчик для необработанных промисов
    window.addEventListener('unhandledrejection', function(event) {
        console.error('Необработанная ошибка промиса:', event.reason);
        const errorMessage = event.reason?.message || 'Произошла непредвиденная ошибка';
        showNotification(errorMessage, 'error');
        event.preventDefault();
    });

    // Обработчик для ошибок загрузки ресурсов
    window.addEventListener('error', function(event) {
        if (event.target && (event.target.tagName === 'IMG' || event.target.tagName === 'SCRIPT' || event.target.tagName === 'LINK')) {
            console.warn('Ошибка загрузки ресурса:', event.target.src || event.target.href);
            // Не показываем уведомление для ошибок загрузки ресурсов, чтобы не спамить
        }
    }, true);

    console.log('Глобальный обработчик ошибок инициализирован');
}

// ========================================
// СТАТИСТИКА ПЛАТЕЖЕЙ (для кнопки 'Статистика' в admin/payments/list.html)
// ========================================
function getPaymentStatistics(rentalId) {
    fetch(`/admin/payments/statistics/${rentalId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success && data.statistics) {
                const stats = data.statistics;
                document.getElementById('statisticsContent').innerHTML = `
                    <ul>
                        <li>Всего платежей: <b>${stats.totalPayments}</b></li>
                        <li>Обработано: <b>${stats.processedPayments}</b></li>
                        <li>Ожидает: <b>${stats.pendingPayments}</b></li>
                        <li>Ошибки: <b>${stats.failedPayments}</b></li>
                        <li>Общая сумма: <b>${stats.totalAmount} ₽</b></li>
                        <li>Обработано на сумму: <b>${stats.processedAmount} ₽</b></li>
                    </ul>
                `;
            } else {
                document.getElementById('statisticsContent').innerHTML = `<span class="text-danger">Ошибка: ${data.error || 'Не удалось получить статистику'}</span>`;
            }
            // Открыть модальное окно (Bootstrap 5)
            const modal = new bootstrap.Modal(document.getElementById('statisticsModal'));
            modal.show();
        })
        .catch(() => {
            document.getElementById('statisticsContent').innerHTML = '<span class="text-danger">Ошибка соединения с сервером</span>';
            const modal = new bootstrap.Modal(document.getElementById('statisticsModal'));
            modal.show();
        });
}
window.getPaymentStatistics = getPaymentStatistics;

// ========================================
// КНОПКА ВОЗВРАТА НАЗАД В ХЕДЕРЕ
// ========================================
function initBackButton() {
    const backButton = document.getElementById('backButton');
    if (!backButton) return;

    // Проверка: есть ли куда вернуться
    const hasHistory = window.history.length > 1;
    const referrer = document.referrer;
    const current = window.location.href;
    let show = false;
    if (hasHistory && referrer && referrer !== current && !referrer.endsWith('/auth/login')) {
        show = true;
    } else if (hasHistory && document.referrer && document.referrer !== current) {
        show = true;
    }
    // Не показывать на главной
    if (window.location.pathname === '/' || window.location.pathname === '/main') {
        show = false;
    }
    if (show) {
        backButton.classList.remove('d-none');
    } else {
        backButton.classList.add('d-none');
    }
    backButton.addEventListener('click', function() {
        if (window.history.length > 1) {
            window.history.back();
        } else {
            window.location.href = '/';
        }
    });
}