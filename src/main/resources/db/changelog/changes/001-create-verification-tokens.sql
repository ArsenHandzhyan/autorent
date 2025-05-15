-- Create Verification Tokens table
CREATE TABLE verification_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    verification_code VARCHAR(20),
    token_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    verified_at TIMESTAMP,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    
    INDEX idx_token (token),
    INDEX idx_email_type (email, token_type),
    INDEX idx_expires_used (expires_at, is_used)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;