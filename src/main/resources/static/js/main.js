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