package ru.anapa.autorent.service;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.anapa.autorent.model.PasswordResetLog;
import ru.anapa.autorent.model.User;
import ru.anapa.autorent.model.VerificationToken;
import ru.anapa.autorent.repository.PasswordResetLogRepository;
import ru.anapa.autorent.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@Service
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    
    // Лимиты безопасности
    private static final int MAX_ATTEMPTS_PER_EMAIL_PER_DAY = 5;
    private static final int MAX_ATTEMPTS_PER_IP_PER_DAY = 10;
    private static final int MIN_INTERVAL_BETWEEN_REQUESTS_MINUTES = 5;

    private final UserRepository userRepository;
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;
    private final PasswordResetLogRepository passwordResetLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordResetService(UserRepository userRepository,
                               VerificationTokenService verificationTokenService,
                               EmailService emailService,
                               PasswordResetLogRepository passwordResetLogRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.verificationTokenService = verificationTokenService;
        this.emailService = emailService;
        this.passwordResetLogRepository = passwordResetLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Инициирует процесс восстановления пароля
     */
    @Transactional
    public boolean initiatePasswordReset(String email, HttpServletRequest request) {
        logger.info("Инициация восстановления пароля для email: {}", email);
        
        // Нормализация email
        email = email.toLowerCase().trim();
        
        // Получаем информацию о запросе
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        try {
            // Проверяем лимиты безопасности
            if (!checkSecurityLimits(email, ipAddress)) {
                logPasswordResetActionAsync(email, "N/A", ipAddress, userAgent, 
                                     PasswordResetLog.ActionType.REQUEST_SENT, 
                                     PasswordResetLog.Status.FAILED, 
                                     "Превышен лимит попыток");
                return false;
            }
            
            // Проверяем существование пользователя
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                logger.warn("Попытка восстановления пароля для несуществующего email: {}", email);
                logPasswordResetActionAsync(email, "N/A", ipAddress, userAgent, 
                                     PasswordResetLog.ActionType.USER_NOT_FOUND, 
                                     PasswordResetLog.Status.FAILED, 
                                     "Пользователь не найден");
                // Возвращаем true для безопасности (не раскрываем информацию о существующих пользователях)
                return true;
            }
            
            // Проверяем интервал между запросами
            if (hasRecentRequest(email)) {
                logPasswordResetActionAsync(email, "N/A", ipAddress, userAgent, 
                                     PasswordResetLog.ActionType.REQUEST_SENT, 
                                     PasswordResetLog.Status.FAILED, 
                                     "Слишком частые запросы");
                return false;
            }
            
            // Создаем токен для сброса пароля
            VerificationToken token = verificationTokenService.createPasswordResetToken(email);
            
            // Логируем успешную отправку
            logPasswordResetActionAsync(email, token.getToken(), ipAddress, userAgent, 
                                 PasswordResetLog.ActionType.REQUEST_SENT, 
                                 PasswordResetLog.Status.SUCCESS, null);
            
            logger.info("Токен восстановления пароля создан для email: {}", email);
            return true;
            
        } catch (Exception e) {
            logger.error("Ошибка при инициации восстановления пароля для email: {}", email, e);
            logPasswordResetActionAsync(email, "N/A", ipAddress, userAgent, 
                                 PasswordResetLog.ActionType.REQUEST_SENT, 
                                 PasswordResetLog.Status.FAILED, e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет валидность токена для сброса пароля
     */
    @Transactional(readOnly = true)
    public boolean validateResetToken(String token, HttpServletRequest request) {
        logger.info("Проверка токена восстановления пароля: {}", token);
        
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        try {
            Optional<VerificationToken> tokenOpt = verificationTokenService.validatePasswordResetToken(token);
            
            if (tokenOpt.isPresent()) {
                VerificationToken verificationToken = tokenOpt.get();
                logPasswordResetActionAsync(verificationToken.getEmail(), token, ipAddress, userAgent, 
                                     PasswordResetLog.ActionType.TOKEN_VALIDATED, 
                                     PasswordResetLog.Status.SUCCESS, null);
                logger.info("Токен восстановления пароля валиден для email: {}", verificationToken.getEmail());
                return true;
            } else {
                logPasswordResetActionAsync("unknown", token, ipAddress, userAgent, 
                                     PasswordResetLog.ActionType.INVALID_TOKEN, 
                                     PasswordResetLog.Status.FAILED, "Неверный токен");
                logger.warn("Неверный токен восстановления пароля: {}", token);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при проверке токена восстановления пароля: {}", token, e);
            logPasswordResetActionAsync("unknown", token, ipAddress, userAgent, 
                                 PasswordResetLog.ActionType.INVALID_TOKEN, 
                                 PasswordResetLog.Status.FAILED, e.getMessage());
            return false;
        }
    }

    /**
     * Сбрасывает пароль пользователя
     */
    @Transactional
    public boolean resetPassword(String token, String newPassword, HttpServletRequest request) {
        logger.info("Сброс пароля по токену: {}", token);
        
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        try {
            // Проверяем токен
            Optional<VerificationToken> tokenOpt = verificationTokenService.validatePasswordResetToken(token);
            
            if (tokenOpt.isEmpty()) {
                logPasswordResetActionAsync("unknown", token, ipAddress, userAgent, 
                                     PasswordResetLog.ActionType.PASSWORD_RESET, 
                                     PasswordResetLog.Status.FAILED, "Неверный токен");
                return false;
            }
            
            VerificationToken verificationToken = tokenOpt.get();
            String email = verificationToken.getEmail();
            
            // Проверяем существование пользователя
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                logPasswordResetActionAsync(email, token, ipAddress, userAgent, 
                                     PasswordResetLog.ActionType.PASSWORD_RESET, 
                                     PasswordResetLog.Status.FAILED, "Пользователь не найден");
                return false;
            }
            
            User user = userOpt.get();
            
            // Валидация нового пароля
            if (newPassword == null || newPassword.length() < 8) {
                logPasswordResetActionAsync(email, token, ipAddress, userAgent, 
                                     PasswordResetLog.ActionType.PASSWORD_RESET, 
                                     PasswordResetLog.Status.FAILED, "Слабый пароль");
                return false;
            }
            
            // Проверяем, что новый пароль отличается от текущего
            if (passwordEncoder.matches(newPassword, user.getPassword())) {
                logPasswordResetActionAsync(email, token, ipAddress, userAgent, 
                                     PasswordResetLog.ActionType.PASSWORD_RESET, 
                                     PasswordResetLog.Status.FAILED, "Новый пароль совпадает с текущим");
                return false;
            }
            
            // Устанавливаем новый пароль
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            
            // Помечаем токен как использованный
            verificationTokenService.useToken(verificationToken);
            
            // Логируем успешный сброс пароля
            logPasswordResetActionAsync(email, token, ipAddress, userAgent, 
                                 PasswordResetLog.ActionType.PASSWORD_RESET, 
                                 PasswordResetLog.Status.SUCCESS, null);
            
            logger.info("Пароль успешно сброшен для пользователя: {}", email);
            return true;
            
        } catch (Exception e) {
            logger.error("Ошибка при сбросе пароля по токену: {}", token, e);
            logPasswordResetActionAsync("unknown", token, ipAddress, userAgent, 
                                 PasswordResetLog.ActionType.PASSWORD_RESET, 
                                 PasswordResetLog.Status.FAILED, e.getMessage());
            return false;
        }
    }

    /**
     * Получает email пользователя по токену
     */
    @Transactional(readOnly = true)
    public Optional<String> getEmailByToken(String token) {
        Optional<VerificationToken> tokenOpt = verificationTokenService.validatePasswordResetToken(token);
        return tokenOpt.map(VerificationToken::getEmail);
    }

    /**
     * Проверяет лимиты безопасности
     */
    private boolean checkSecurityLimits(String email, String ipAddress) {
        LocalDateTime since = LocalDateTime.now().minusDays(1);
        
        // Проверяем лимит по email
        long emailAttempts = passwordResetLogRepository.countFailedAttemptsByEmailSince(email, since);
        if (emailAttempts >= MAX_ATTEMPTS_PER_EMAIL_PER_DAY) {
            logger.warn("Превышен лимит попыток восстановления пароля для email: {}", email);
            return false;
        }
        
        // Проверяем лимит по IP
        long ipAttempts = passwordResetLogRepository.countFailedAttemptsByIpSince(ipAddress, since);
        if (ipAttempts >= MAX_ATTEMPTS_PER_IP_PER_DAY) {
            logger.warn("Превышен лимит попыток восстановления пароля для IP: {}", ipAddress);
            return false;
        }
        
        return true;
    }

    /**
     * Проверяет наличие недавнего запроса
     */
    private boolean hasRecentRequest(String email) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(MIN_INTERVAL_BETWEEN_REQUESTS_MINUTES);
        List<PasswordResetLog> recentLogs = passwordResetLogRepository.findByEmailSince(email, since);
        return !recentLogs.isEmpty();
    }

    /**
     * Логирует действие восстановления пароля в отдельной транзакции
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void logPasswordResetActionAsync(String email, String token, String ipAddress, String userAgent,
                                      PasswordResetLog.ActionType actionType, 
                                      PasswordResetLog.Status status, String errorMessage) {
        try {
            PasswordResetLog log = new PasswordResetLog(email, token, ipAddress, userAgent, actionType);
            
            if (status == PasswordResetLog.Status.SUCCESS) {
                log.markAsCompleted(PasswordResetLog.Status.SUCCESS);
            } else {
                log.setError(errorMessage);
            }
            
            passwordResetLogRepository.save(log);
            logger.debug("Лог восстановления пароля сохранен: {}", log.getId());
            
        } catch (Exception e) {
            logger.error("Ошибка при сохранении лога восстановления пароля", e);
            // Не пробрасываем исключение, чтобы не влиять на основную транзакцию
        }
    }

    /**
     * Логирует действие восстановления пароля (устаревший метод)
     */
    private void logPasswordResetAction(String email, String token, String ipAddress, String userAgent,
                                      PasswordResetLog.ActionType actionType, 
                                      PasswordResetLog.Status status, String errorMessage) {
        logPasswordResetActionAsync(email, token, ipAddress, userAgent, actionType, status, errorMessage);
    }

    /**
     * Получает IP адрес клиента
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Планировщик для очистки старых логов восстановления пароля
     * Запускается каждый день в 02:00
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupOldPasswordResetLogs() {
        logger.info("Начинаем очистку старых логов восстановления пароля");
        
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30); // Удаляем логи старше 30 дней
            passwordResetLogRepository.deleteOldLogs(cutoffDate);
            
            logger.info("Очистка старых логов восстановления пароля завершена");
        } catch (Exception e) {
            logger.error("Ошибка при очистке старых логов восстановления пароля", e);
        }
    }

    /**
     * Получает статистику восстановления паролей за последние 24 часа
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getPasswordResetStatistics() {
        LocalDateTime since = LocalDateTime.now().minusDays(1);
        
        Map<String, Object> statistics = new HashMap<>();
        
        // Общее количество запросов
        long totalRequests = passwordResetLogRepository.countByCreatedAtAfter(since);
        statistics.put("totalRequests", totalRequests);
        
        // Успешные запросы
        long successfulRequests = passwordResetLogRepository.countByStatusAndCreatedAtAfter(
                PasswordResetLog.Status.SUCCESS, since);
        statistics.put("successfulRequests", successfulRequests);
        
        // Неудачные запросы
        long failedRequests = passwordResetLogRepository.countByStatusAndCreatedAtAfter(
                PasswordResetLog.Status.FAILED, since);
        statistics.put("failedRequests", failedRequests);
        
        // Запросы по типам действий
        Map<PasswordResetLog.ActionType, Long> actionTypeStats = new HashMap<>();
        for (PasswordResetLog.ActionType actionType : PasswordResetLog.ActionType.values()) {
            long count = passwordResetLogRepository.countByActionTypeAndCreatedAtAfter(actionType, since);
            actionTypeStats.put(actionType, count);
        }
        statistics.put("actionTypeStats", actionTypeStats);
        
        return statistics;
    }
} 