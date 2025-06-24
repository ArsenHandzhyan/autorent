-- Миграция для создания таблицы логирования восстановления пароля
-- Автор: AutoRent Team
-- Дата: 2024-12-19

CREATE TABLE IF NOT EXISTS password_reset_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    token VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    action_type ENUM('REQUEST_SENT', 'TOKEN_VALIDATED', 'PASSWORD_RESET', 'TOKEN_EXPIRED', 'INVALID_TOKEN', 'USER_NOT_FOUND') NOT NULL,
    status ENUM('SUCCESS', 'FAILED', 'PENDING') NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    
    INDEX idx_email (email),
    INDEX idx_token (token),
    INDEX idx_ip_address (ip_address),
    INDEX idx_action_type (action_type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_email_created_at (email, created_at),
    INDEX idx_ip_created_at (ip_address, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Комментарии к таблице
ALTER TABLE password_reset_logs COMMENT = 'Логи операций восстановления пароля';

-- Комментарии к полям
ALTER TABLE password_reset_logs 
    MODIFY COLUMN email VARCHAR(255) NOT NULL COMMENT 'Email пользователя',
    MODIFY COLUMN token VARCHAR(255) NOT NULL COMMENT 'Токен восстановления пароля',
    MODIFY COLUMN ip_address VARCHAR(45) COMMENT 'IP адрес клиента',
    MODIFY COLUMN user_agent TEXT COMMENT 'User-Agent браузера',
    MODIFY COLUMN action_type ENUM('REQUEST_SENT', 'TOKEN_VALIDATED', 'PASSWORD_RESET', 'TOKEN_EXPIRED', 'INVALID_TOKEN', 'USER_NOT_FOUND') NOT NULL COMMENT 'Тип действия',
    MODIFY COLUMN status ENUM('SUCCESS', 'FAILED', 'PENDING') NOT NULL DEFAULT 'PENDING' COMMENT 'Статус операции',
    MODIFY COLUMN error_message TEXT COMMENT 'Сообщение об ошибке',
    MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Время создания записи',
    MODIFY COLUMN completed_at TIMESTAMP NULL COMMENT 'Время завершения операции'; 