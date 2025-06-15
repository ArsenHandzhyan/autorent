// Получаем CSRF-токен из мета-тегов
const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

// Функции для работы с формами
function showCancelForm(id) {
    document.getElementById('cancelForm-' + id).style.display = 'block';
}

function hideCancelForm(id) {
    document.getElementById('cancelForm-' + id).style.display = 'none';
}

// Функция для отображения сообщений
function showMessage(type, message) {
    const messageContainer = document.getElementById('message-container');
    messageContainer.innerHTML = '';

    const messageElement = document.createElement('div');
    messageElement.className = `alert alert-${type === 'success' ? 'success' : 'danger'} alert-dismissible fade show`;
    messageElement.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;

    messageContainer.appendChild(messageElement);

    setTimeout(() => {
        messageElement.classList.remove('show');
        setTimeout(() => {
            messageElement.remove();
        }, 300);
    }, 5000);
}

// Функция для обновления строки аренды
function updateRentalRow(rentalId, newStatus) {
    const row = document.getElementById('rental-row-' + rentalId);
    if (!row) return;

    if (newStatus === 'DELETED' || newStatus === 'CANCELLED' || newStatus === 'ACTIVE') {
        row.classList.add('fade-out');
        setTimeout(() => {
            row.remove();
            updateCounters();
        }, 500);
    } else {
        const statusCell = row.querySelector('.status-badge');
        if (statusCell) {
            statusCell.textContent = getStatusText(newStatus);
            statusCell.className = `status-badge status-${newStatus.toLowerCase()}`;
        }
    }
}

// Функция для преобразования статуса в текст
function getStatusText(status) {
    switch (status) {
        case 'PENDING': return 'Ожидает подтверждения';
        case 'ACTIVE': return 'Активна';
        case 'COMPLETED': return 'Завершена';
        case 'CANCELLED': return 'Отменена';
        case 'PENDING_CANCELLATION':
        case 'CANCELLATION_REQUESTED': return 'Запрос на отмену';
        default: return status;
    }
}

// Функция для обновления счетчиков
function updateCounters() {
    const pendingCount = document.querySelectorAll('tr[data-rental-status="PENDING"]').length;
    const pendingCountElement = document.getElementById('pending-count');
    if (pendingCountElement) {
        pendingCountElement.textContent = pendingCount;
    }

    const table = document.querySelector('.data-table tbody');
    if (table && table.children.length === 0) {
        table.innerHTML = '<tr><td colspan="8" class="text-center">Нет ожидающих аренд</td></tr>';
    }
}

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    const style = document.createElement('style');
    style.textContent = `
        .fade-out {
            opacity: 0;
            transition: opacity 0.5s;
        }
    `;
    document.head.appendChild(style);

    // Преобразуем все формы в AJAX-запросы
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', function(e) {
            // Исключаем logout-форму из AJAX-обработки
            if (this.action.includes('/auth/logout')) {
                return; // пусть браузер обработает logout как обычную форму
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
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    showMessage('success', data.message);
                    if (rentalId) {
                        updateRentalRow(rentalId, data.newStatus);
                    }
                } else {
                    showMessage('error', data.message);
                }
            })
            .catch(error => {
                showMessage('error', 'Произошла ошибка при обработке запроса');
            });
        });
    });
}); 