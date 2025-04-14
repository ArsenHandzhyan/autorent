package ru.anapa.autorent.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, которое выбрасывается, когда запрашиваемый ресурс не найден.
 * Аннотация @ResponseStatus обеспечивает возврат HTTP статуса 404 (NOT_FOUND),
 * когда это исключение выбрасывается в контроллере.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Конструктор с сообщением об ошибке.
     *
     * @param message сообщение об ошибке
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Конструктор с сообщением об ошибке и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause причина исключения
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Конструктор для создания исключения с форматированным сообщением о ресурсе.
     *
     * @param resourceName название ресурса (например, "Автомобиль", "Пользователь")
     * @param fieldName название поля, по которому искали ресурс (например, "id", "email")
     * @param fieldValue значение поля, по которому искали ресурс
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s не найден с %s: '%s'", resourceName, fieldName, fieldValue));
    }
}