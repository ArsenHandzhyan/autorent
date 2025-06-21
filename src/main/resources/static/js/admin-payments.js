/**
 * JavaScript для управления ежедневными платежами
 */

// Глобальные переменные
let currentRentalId = null;
let currentPaymentDate = null;

/**
 * Инициализация страницы
 */
document.addEventListener('DOMContentLoaded', function() {
    initializePaymentManagement();
    setupEventListeners();
});

/**
 * Инициализация управления платежами
 */
function initializePaymentManagement() {
    // Устанавливаем текущую дату в поля ввода
    const today = new Date().toISOString().split('T')[0];
    const paymentDateInput = document.getElementById('paymentDate');
    const processDateInput = document.getElementById('processDate');
    
    if (paymentDateInput) {
        paymentDateInput.value = today;
    }
    if (processDateInput) {
        processDateInput.value = today;
    }
    
    // Получаем ID аренды из URL, если находимся на странице конкретной аренды
    const urlParams = new URLSearchParams(window.location.search);
    const rentalId = urlParams.get('rentalId') || getRentalIdFromUrl();
    if (rentalId) {
        currentRentalId = rentalId;
    }
}

/**
 * Настройка обработчиков событий
 */
function setupEventListeners() {
    // Обработчик для кнопки создания платежей
    const createButton = document.getElementById('createPaymentsBtn');
    if (createButton) {
        createButton.addEventListener('click', createPayments);
    }
    
    // Обработчик для кнопки обработки платежей
    const processButton = document.getElementById('processPaymentsBtn');
    if (processButton) {
        processButton.addEventListener('click', processPayments);
    }
    
    // Обработчики для кнопок обработки отдельных платежей
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('process-payment-btn')) {
            const paymentId = e.target.dataset.paymentId;
            processSpecificPayment(paymentId);
        }
        
        if (e.target.classList.contains('retry-payment-btn')) {
            const paymentId = e.target.dataset.paymentId;
            retryPayment(paymentId);
        }
    });
}

/**
 * Создание платежей на указанную дату
 */
function createPayments() {
    const paymentDate = document.getElementById('paymentDate').value;
    if (!paymentDate) {
        showAlert('Пожалуйста, выберите дату', 'warning');
        return;
    }
    
    showLoading('Создание платежей...');
    
    fetch('/admin/payments/create', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'paymentDate=' + encodeURIComponent(paymentDate)
    })
    .then(response => response.json())
    .then(data => {
        hideLoading();
        if (data.success) {
            showAlert('Платежи успешно созданы для даты ' + paymentDate, 'success');
            setTimeout(() => location.reload(), 1500);
        } else {
            showAlert('Ошибка: ' + data.error, 'danger');
        }
    })
    .catch(error => {
        hideLoading();
        console.error('Ошибка:', error);
        showAlert('Произошла ошибка при создании платежей', 'danger');
    });
}

/**
 * Обработка платежей на указанную дату
 */
function processPayments() {
    const processDate = document.getElementById('processDate').value;
    if (!processDate) {
        showAlert('Пожалуйста, выберите дату', 'warning');
        return;
    }
    
    showLoading('Обработка платежей...');
    
    fetch('/admin/payments/process', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'paymentDate=' + encodeURIComponent(processDate)
    })
    .then(response => response.json())
    .then(data => {
        hideLoading();
        if (data.success) {
            showAlert('Платежи успешно обработаны для даты ' + processDate, 'success');
            setTimeout(() => location.reload(), 1500);
        } else {
            showAlert('Ошибка: ' + data.error, 'danger');
        }
    })
    .catch(error => {
        hideLoading();
        console.error('Ошибка:', error);
        showAlert('Произошла ошибка при обработке платежей', 'danger');
    });
}

/**
 * Обработка конкретного платежа
 */
function processSpecificPayment(paymentId) {
    if (!confirm('Обработать этот платеж?')) {
        return;
    }
    
    showLoading('Обработка платежа...');
    
    fetch('/admin/payments/process/' + paymentId, {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        hideLoading();
        if (data.success) {
            showAlert('Платеж успешно обработан', 'success');
            setTimeout(() => location.reload(), 1500);
        } else {
            showAlert('Ошибка: ' + data.error, 'danger');
        }
    })
    .catch(error => {
        hideLoading();
        console.error('Ошибка:', error);
        showAlert('Произошла ошибка при обработке платежа', 'danger');
    });
}

/**
 * Повторная обработка неудачного платежа
 */
function retryPayment(paymentId) {
    if (!confirm('Повторить обработку этого платежа?')) {
        return;
    }
    
    processSpecificPayment(paymentId);
}

/**
 * Получение статистики платежей
 */
function getPaymentStatistics(rentalId) {
    showLoading('Загрузка статистики...');
    
    fetch('/admin/payments/statistics/' + rentalId)
    .then(response => response.json())
    .then(data => {
        hideLoading();
        if (data.success) {
            showStatisticsModal(data.statistics);
        } else {
            showAlert('Ошибка при получении статистики: ' + data.error, 'danger');
        }
    })
    .catch(error => {
        hideLoading();
        console.error('Ошибка:', error);
        showAlert('Произошла ошибка при получении статистики', 'danger');
    });
}

/**
 * Отображение модального окна со статистикой
 */
function showStatisticsModal(statistics) {
    const modal = document.getElementById('statisticsModal');
    const content = document.getElementById('statisticsContent');
    
    if (!modal || !content) {
        console.error('Модальное окно статистики не найдено');
        return;
    }
    
    const html = `
        <div class="row">
            <div class="col-md-6">
                <h6>Количество платежей:</h6>
                <ul class="list-unstyled">
                    <li><strong>Всего:</strong> ${statistics.totalPayments}</li>
                    <li><strong>Обработано:</strong> ${statistics.processedPayments}</li>
                    <li><strong>Ожидает:</strong> ${statistics.pendingPayments}</li>
                    <li><strong>Ошибки:</strong> ${statistics.failedPayments}</li>
                </ul>
            </div>
            <div class="col-md-6">
                <h6>Суммы:</h6>
                <ul class="list-unstyled">
                    <li><strong>Общая сумма:</strong> ${formatCurrency(statistics.totalAmount)}</li>
                    <li><strong>Обработано:</strong> ${formatCurrency(statistics.processedAmount)}</li>
                </ul>
            </div>
        </div>
    `;
    
    content.innerHTML = html;
    
    const bootstrapModal = new bootstrap.Modal(modal);
    bootstrapModal.show();
}

/**
 * Обновление страницы
 */
function refreshPayments() {
    location.reload();
}

/**
 * Получение ID аренды из URL
 */
function getRentalIdFromUrl() {
    const pathParts = window.location.pathname.split('/');
    const rentalIndex = pathParts.indexOf('rental');
    if (rentalIndex !== -1 && rentalIndex + 1 < pathParts.length) {
        return pathParts[rentalIndex + 1];
    }
    return null;
}

/**
 * Форматирование валюты
 */
function formatCurrency(amount) {
    return new Intl.NumberFormat('ru-RU', {
        style: 'currency',
        currency: 'RUB',
        minimumFractionDigits: 2
    }).format(amount);
}

/**
 * Отображение уведомления
 */
function showAlert(message, type = 'info') {
    const alertContainer = document.getElementById('alertContainer') || createAlertContainer();
    
    const alertHtml = `
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    
    alertContainer.innerHTML = alertHtml;
    
    // Автоматическое скрытие через 5 секунд
    setTimeout(() => {
        const alert = alertContainer.querySelector('.alert');
        if (alert) {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }
    }, 5000);
}

/**
 * Создание контейнера для уведомлений
 */
function createAlertContainer() {
    const container = document.createElement('div');
    container.id = 'alertContainer';
    container.className = 'position-fixed top-0 end-0 p-3';
    container.style.zIndex = '9999';
    document.body.appendChild(container);
    return container;
}

/**
 * Отображение индикатора загрузки
 */
function showLoading(message = 'Загрузка...') {
    const loadingHtml = `
        <div id="loadingOverlay" class="position-fixed top-0 start-0 w-100 h-100 d-flex justify-content-center align-items-center" 
             style="background: rgba(0,0,0,0.5); z-index: 9999;">
            <div class="spinner-border text-light" role="status">
                <span class="visually-hidden">${message}</span>
            </div>
        </div>
    `;
    
    if (!document.getElementById('loadingOverlay')) {
        document.body.insertAdjacentHTML('beforeend', loadingHtml);
    }
}

/**
 * Скрытие индикатора загрузки
 */
function hideLoading() {
    const loadingOverlay = document.getElementById('loadingOverlay');
    if (loadingOverlay) {
        loadingOverlay.remove();
    }
}

/**
 * Экспорт функций для глобального использования
 */
window.PaymentManager = {
    createPayments,
    processPayments,
    processSpecificPayment,
    retryPayment,
    getPaymentStatistics,
    refreshPayments,
    showAlert,
    showLoading,
    hideLoading
}; 