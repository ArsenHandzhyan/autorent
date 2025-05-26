// Основной JavaScript файл
document.addEventListener('DOMContentLoaded', function() {
    // Инициализация всех компонентов
    initNavigation();
    initAnimations();
    initFormValidation();
});

// Функция для инициализации навигации
function initNavigation() {
    const mobileMenuButton = document.querySelector('.mobile-menu-button');
    const navigation = document.querySelector('.main-navigation');

    if (mobileMenuButton && navigation) {
        mobileMenuButton.addEventListener('click', () => {
            navigation.classList.toggle('active');
        });
    }
}

// Функция для инициализации анимаций
function initAnimations() {
    // Анимация появления элементов при скролле
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('animate-in');
            }
        });
    });

    document.querySelectorAll('.animate-on-scroll').forEach((el) => observer.observe(el));
}

// Функция для валидации форм
function initFormValidation() {
    const forms = document.querySelectorAll('form[data-validate]');
    
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            if (!validateForm(this)) {
                e.preventDefault();
            }
        });
    });
}

// Вспомогательная функция для валидации формы
function validateForm(form) {
    let isValid = true;
    const requiredFields = form.querySelectorAll('[required]');

    requiredFields.forEach(field => {
        if (!field.value.trim()) {
            isValid = false;
            field.classList.add('error');
        } else {
            field.classList.remove('error');
        }
    });

    return isValid;
}

// Функция для обновления статуса автомобиля (админ)
function updateCarStatus(carId, newStatus) {
    // Проверяем, является ли пользователь администратором
    const isAdmin = document.documentElement.getAttribute('data-is-admin') === 'true';
    if (!isAdmin) {
        showMessage('У вас нет прав для выполнения этой операции', 'error');
        return;
    }

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
        const select = event.target;
        const previousStatus = select.getAttribute('data-previous-status');
        select.value = previousStatus;
        return;
    }

    const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

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
            showMessage(data.error, 'error');
            // В случае ошибки возвращаем предыдущее значение
            const select = event.target;
            const previousStatus = select.getAttribute('data-previous-status');
            select.value = previousStatus;
        } else {
            showMessage(data.message || 'Статус успешно обновлен', 'success');
            // Обновляем UI
            const select = event.target;
            select.className = `status-select status-${newStatus.toLowerCase()}`;
            select.setAttribute('data-previous-status', newStatus);
            
            // Обновляем кнопки обслуживания
            const maintenanceButton = select.closest('td').querySelector('.btn-warning');
            const removeMaintenanceButton = select.closest('td').querySelector('.btn-success');
            
            if (newStatus === 'MAINTENANCE') {
                if (maintenanceButton) maintenanceButton.style.display = 'none';
                if (removeMaintenanceButton) removeMaintenanceButton.style.display = 'inline-block';
            } else {
                if (maintenanceButton) maintenanceButton.style.display = 'inline-block';
                if (removeMaintenanceButton) removeMaintenanceButton.style.display = 'none';
            }

            // Обновляем страницу через 1 секунду для отображения актуального состояния
            setTimeout(() => window.location.reload(), 1000);
        }
    })
    .catch(error => {
        console.error('Ошибка:', error);
        showMessage(error.message || 'Произошла ошибка при обновлении статуса', 'error');
        // В случае ошибки возвращаем предыдущее значение
        const select = event.target;
        const previousStatus = select.getAttribute('data-previous-status');
        select.value = previousStatus;
    });
} 