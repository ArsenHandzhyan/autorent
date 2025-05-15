package ru.anapa.autorent.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.Random;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final Random RANDOM = new Random();

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${spring.mail.username:noreply@autorent.ru}")
    private String fromEmail;
    
    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    @Autowired
    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * Отправляет код подтверждения на email
     *
     * @param to Email получателя
     * @return Сгенерированный код подтверждения
     */
    public String sendVerificationCode(String to) {
        String code = generateVerificationCode();
        String subject = "Код подтверждения регистрации в АвтоРент";
        
        try {
            Context context = new Context(new Locale("ru"));
            context.setVariable("code", code);
            context.setVariable("appUrl", appUrl);
            
            String emailContent = templateEngine.process("email/verification-code", context);
            sendEmail(to, subject, emailContent);
            
            log.info("Verification code sent to {}", to);
            return code;
        } catch (Exception e) {
            log.error("Failed to send verification code to {}: {}", to, e.getMessage());
            throw new RuntimeException("Не удалось отправить код подтверждения: " + e.getMessage());
        }
    }

    /**
     * Отправляет приветственное письмо новому пользователю
     *
     * @param to Email получателя
     * @param name Имя пользователя
     */
    public void sendWelcomeEmail(String to, String name) {
        String subject = "Добро пожаловать в АвтоРент!";
        
        try {
            Context context = new Context(new Locale("ru"));
            context.setVariable("name", name);
            context.setVariable("appUrl", appUrl);
            
            String emailContent = templateEngine.process("email/welcome", context);
            sendEmail(to, subject, emailContent);
            
            log.info("Welcome email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Отправляет письмо о сбросе пароля
     *
     * @param to Email получателя
     * @param resetToken Токен для сброса пароля
     */
    public void sendPasswordResetEmail(String to, String resetToken) {
        String subject = "Сброс пароля в АвтоРент";
        String resetLink = appUrl + "/auth/reset-password?token=" + resetToken;
        
        try {
            Context context = new Context(new Locale("ru"));
            context.setVariable("resetLink", resetLink);
            context.setVariable("appUrl", appUrl);
            
            String emailContent = templateEngine.process("email/password-reset", context);
            sendEmail(to, subject, emailContent);
            
            log.info("Password reset email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Не удалось отправить письмо для сброса пароля: " + e.getMessage());
        }
    }

    /**
     * Отправляет HTML письмо
     *
     * @param to Email получателя
     * @param subject Тема письма
     * @param htmlContent HTML содержимое письма
     */
    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }

    /**
     * Генерирует 6-значный код подтверждения
     *
     * @return 6-значный код
     */
    private String generateVerificationCode() {
        return String.format("%06d", RANDOM.nextInt(1000000));
    }
}
