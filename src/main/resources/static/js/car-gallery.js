// Функция для предпросмотра загруженных изображений
function previewImages(input) {
    const container = document.getElementById('imagePreviewContainer');
    container.innerHTML = '';

    if (input.files) {
        Array.from(input.files).forEach((file, index) => {
            const reader = new FileReader();
            const preview = document.createElement('div');
            preview.className = 'image-preview';

            reader.onload = function(e) {
                preview.innerHTML = `
                    <img src="${e.target.result}" alt="Preview">
                    <button type="button" class="remove-image" onclick="removePreview(this)">
                        <i class="fas fa-times"></i>
                    </button>
                `;
            };

            reader.readAsDataURL(file);
            container.appendChild(preview);
        });
    }
}

// Функция для удаления превью изображения
function removePreview(button) {
    const preview = button.parentElement;
    preview.remove();
}

// Функция для изменения основного изображения в галерее
function changeMainImage(src) {
    const mainImage = document.querySelector('.main-image img');
    if (mainImage) {
        mainImage.src = src;
    }

    // Обновляем активный класс у миниатюр
    document.querySelectorAll('.thumbnail img').forEach(img => {
        img.classList.remove('active');
        if (img.src === src) {
            img.classList.add('active');
        }
    });
}

// Функция для удаления изображения
function deleteImage(imageId) {
    if (confirm('Вы уверены, что хотите удалить это изображение?')) {
        fetch(`/api/cars/images/${imageId}`, {
            method: 'DELETE',
            headers: {
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
            }
        })
        .then(response => {
            if (response.ok) {
                const imageItem = document.querySelector(`[data-image-id="${imageId}"]`).closest('.image-item');
                imageItem.remove();
            } else {
                alert('Ошибка при удалении изображения');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Ошибка при удалении изображения');
        });
    }
}

// Инициализация обработчиков событий
document.addEventListener('DOMContentLoaded', function() {
    // Обработчик загрузки изображений
    const imageInput = document.getElementById('images');
    if (imageInput) {
        imageInput.addEventListener('change', function() {
            previewImages(this);
        });
    }

    // Обработчик удаления изображений
    document.querySelectorAll('.delete-image').forEach(button => {
        button.addEventListener('click', function() {
            const imageId = this.dataset.imageId;
            deleteImage(imageId);
        });
    });
}); 