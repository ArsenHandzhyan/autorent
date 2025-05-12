package ru.anapa.autorent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import ru.anapa.autorent.model.User;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(User user) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(user.getEmail());
            helper.setSubject("Добро пожаловать в AutoRent!");
            helper.setText(
                    "<h1>Добро пожаловать, " + user.getFirstName() + "!</h1>" +
                            "<p>Спасибо за регистрацию в AutoRent. Мы рады видеть вас среди наших пользователей!</p>" +
                            "<p>Ваш email: " + user.getEmail() + "</p>" +
                            "<p>Если у вас есть вопросы, свяжитесь с нами.</p>",
                    true
            );

            mailSender.send(mimeMessage);
            logger.info("Приветственное письмо отправлено пользователю: {}", user.getEmail());
        } catch (MessagingException e) {
            logger.error("Ошибка при отправке приветственного письма пользователю {}: {}", user.getEmail(), e.getMessage(), e);
        }
    }
}
