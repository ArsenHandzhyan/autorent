package ru.anapa.autorent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.anapa.autorent.model.VerificationToken;
import ru.anapa.autorent.repository.VerificationTokenRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class VerificationTokenService {

    private static final Logger log = LoggerFactory.getLogger(VerificationTokenService.class);
    
    // Минимальный интервал между отправками кодов (в минутах)
    private static final int MIN_RESEND_INTERVAL_MINUTES = 2;
    private static final int MAX_ATTEMPTS_PER_HOUR = 5;
    
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;

    @Autowired
    public VerificationTokenService(VerificationTokenRepository tokenRepository, EmailService emailService) {
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    /**
     * Создает и отправляет новый токен для верификации email
     *
     * @param email Email пользователя
     * @return Токен верификации
     */
    @Transactional
    public VerificationToken createEmailVerificationToken(String email) {
        // Проверяем, не отправляли ли мы недавно код на этот email
        LocalDateTime timeThreshold = LocalDateTime.now().minus(MIN_RESEND_INTERVAL_MINUTES, ChronoUnit.MINUTES);
        if (tokenRepository.existsRecentToken(email, VerificationToken.TokenType.EMAIL_VERIFICATION, timeThreshold)) {
            throw new RuntimeException("Пожалуйста, подождите " + MIN_RESEND_INTERVAL_MINUTES + 
                                      " минуты перед повторной отправкой кода");
        }
        
        // Инвалидируем все предыдущие токены для этого email
        deleteExistingTokens(email, VerificationToken.TokenType.EMAIL_VERIFICATION);
        
        // Создаем и отправляем новый код подтверждения
        String verificationCode = emailService.sendVerificationCode(email);
        
        // Создаем и сохраняем новый токен
        VerificationToken token = VerificationToken.createEmailToken(email, verificationCode);
        return tokenRepository.save(token);
    }

    /**
     * Создает токен для сброса пароля и отправляет его на email
     *
     * @param email Email пользователя
     * @return Токен сброса пароля
     */
    @Transactional
    public VerificationToken createPasswordResetToken(String email) {
        // Инвалидируем все предыдущие токены для сброса пароля для этого email
        deleteExistingTokens(email, VerificationToken.TokenType.PASSWORD_RESET);
        
        // Создаем новый токен
        VerificationToken token = VerificationToken.createPasswordResetToken(email);
        VerificationToken savedToken = tokenRepository.save(token);
        
        // Отправляем email со ссылкой для сброса пароля
        boolean emailSent = emailService.sendPasswordResetEmail(email, token.getToken());
        if (!emailSent) {
            log.error("Не удалось отправить письмо для сброса пароля на {}", email);
        }
        
        return savedToken;
    }

    /**
     * Создает и сохраняет токен для SMS верификации
     *
     * @param phone Номер телефона
     * @param verificationCode Код подтверждения
     * @return Токен SMS верификации
     */
    @Transactional
    public VerificationToken createSmsVerificationToken(String phone, String verificationCode) {
        // Инвалидируем все предыдущие токены для этого телефона
        deleteExistingTokens(phone, VerificationToken.TokenType.SMS_VERIFICATION);
        
        // Создаем и сохраняем новый токен
        VerificationToken token = VerificationToken.createSmsToken(phone, verificationCode);
        return tokenRepository.save(token);
    }

    /**
     * Проверяет код верификации для email
     *
     * @param email Email пользователя
     * @param code Код верификации
     * @return true если код верный и действительный, иначе false
     */
    @Transactional
    public boolean verifyEmailCode(String email, String code) {
        Optional<VerificationToken> tokenOpt = tokenRepository.findValidTokenByCode(
                email,
                VerificationToken.TokenType.EMAIL_VERIFICATION,
                code,
                LocalDateTime.now()
        );
        
        if (tokenOpt.isPresent()) {
            VerificationToken token = tokenOpt.get();
            token.markAsUsed();
            tokenRepository.save(token);
            return true;
        }
        
        return false;
    }

    /**
     * Проверяет код верификации для SMS
     *
     * @param phone Номер телефона
     * @param code Код верификации
     * @return true если код верный и действительный, иначе false
     */
    @Transactional
    public boolean verifySmsCode(String phone, String code) {
        Optional<VerificationToken> tokenOpt = tokenRepository.findValidTokenByCode(
                phone,
                VerificationToken.TokenType.SMS_VERIFICATION,
                code,
                LocalDateTime.now()
        );
        
        if (tokenOpt.isPresent()) {
            VerificationToken token = tokenOpt.get();
            token.markAsUsed();
            tokenRepository.save(token);
            return true;
        }
        
        return false;
    }

    /**
     * Проверяет токен для сброса пароля
     *
     * @param token Токен сброса пароля
     * @return Optional с токеном если он действителен, иначе empty
     */
    @Transactional(readOnly = true)
    public Optional<VerificationToken> validatePasswordResetToken(String token) {
        Optional<VerificationToken> tokenOpt = tokenRepository.findByToken(token);
        
        if (tokenOpt.isPresent()) {
            VerificationToken verificationToken = tokenOpt.get();
            
            if (verificationToken.isExpired() || verificationToken.isUsed() || 
                verificationToken.getTokenType() != VerificationToken.TokenType.PASSWORD_RESET) {
                return Optional.empty();
            }
            
            return tokenOpt;
        }
        
        return Optional.empty();
    }

    /**
     * Помечает токен как использованный
     *
     * @param token Токен для маркировки
     */
    @Transactional
    public void useToken(VerificationToken token) {
        token.markAsUsed();
        tokenRepository.save(token);
    }

    /**
     * Удаляет все существующие токены указанного типа для данного email/телефона
     *
     * @param email Email или номер телефона
     * @param tokenType Тип токена
     */
    @Transactional
    public void deleteExistingTokens(String email, VerificationToken.TokenType tokenType) {
        tokenRepository.deleteByEmailAndTokenType(email, tokenType);
    }

    /**
     * Планировщик для очистки просроченных токенов
     * Запускается каждый день в 01:00
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Запуск очистки просроченных токенов");
        
        List<VerificationToken> expiredTokens = 
                tokenRepository.findByExpiresAtBeforeAndUsedFalse(LocalDateTime.now());
        
        if (!expiredTokens.isEmpty()) {
            tokenRepository.deleteAll(expiredTokens);
            log.info("Удалено {} просроченных токенов", expiredTokens.size());
        } else {
            log.info("Просроченных токенов не найдено");
        }
    }

    /**
     * Проверяет, не превышен ли лимит попыток отправки кода
     *
     * @param identifier Email или телефон
     * @param tokenType Тип токена
     * @return true если превышен лимит попыток
     */
    public boolean hasTooManyAttempts(String identifier, VerificationToken.TokenType tokenType) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minus(1, ChronoUnit.HOURS);
        long attemptsCount = tokenRepository.countByEmailAndTokenTypeAndCreatedAtAfter(
            identifier,
            tokenType,
            oneHourAgo
        );
        return attemptsCount >= MAX_ATTEMPTS_PER_HOUR;
    }
} 