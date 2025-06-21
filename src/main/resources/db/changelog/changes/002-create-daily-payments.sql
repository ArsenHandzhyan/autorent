-- Создание таблицы daily_payments
CREATE TABLE daily_payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rental_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    payment_date DATE NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    notes TEXT,
    
    FOREIGN KEY (rental_id) REFERENCES rentals(id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_rental_payment_date (rental_id, payment_date),
    INDEX idx_payment_date (payment_date),
    INDEX idx_status (status),
    INDEX idx_rental_status_date (rental_id, status, payment_date)
);

-- Добавление комментариев к таблице и колонкам
ALTER TABLE daily_payments COMMENT = 'Ежедневные платежи по аренде';
ALTER TABLE daily_payments MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT 'Уникальный идентификатор платежа';
ALTER TABLE daily_payments MODIFY COLUMN rental_id BIGINT NOT NULL COMMENT 'ID аренды';
ALTER TABLE daily_payments MODIFY COLUMN account_id BIGINT NOT NULL COMMENT 'ID счета пользователя';
ALTER TABLE daily_payments MODIFY COLUMN payment_date DATE NOT NULL COMMENT 'Дата платежа';
ALTER TABLE daily_payments MODIFY COLUMN amount DECIMAL(10,2) NOT NULL COMMENT 'Сумма платежа';
ALTER TABLE daily_payments MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'Статус платежа: PENDING, PROCESSED, FAILED, CANCELLED';
ALTER TABLE daily_payments MODIFY COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата создания записи';
ALTER TABLE daily_payments MODIFY COLUMN processed_at TIMESTAMP NULL COMMENT 'Дата обработки платежа';
ALTER TABLE daily_payments MODIFY COLUMN notes TEXT COMMENT 'Примечания к платежу'; 