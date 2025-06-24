package ru.anapa.autorent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.anapa.autorent.model.DailyPayment;
import ru.anapa.autorent.model.Role;
import ru.anapa.autorent.model.User;
import ru.anapa.autorent.repository.RoleRepository;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PaymentNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentNotificationService.class);

    private final EmailService emailService;
    private final SmsService smsService;
    private final UserService userService;
    private final RoleRepository roleRepository;

    @Autowired
    public PaymentNotificationService(EmailService emailService, SmsService smsService, @Lazy UserService userService, RoleRepository roleRepository) {
        this.emailService = emailService;
        this.smsService = smsService;
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    /**
     * Отправка уведомления пользователю о списании средств
     */
    public void sendPaymentProcessedNotification(DailyPayment payment) {
        try {
            User user = payment.getAccount().getUser();
            String subject = "Списание средств за аренду автомобиля";
            String message = createPaymentProcessedMessage(payment);
            
            // Отправляем email через существующий метод
            sendEmailNotification(user.getEmail(), subject, message);
            
            // Отправляем SMS (если есть номер телефона)
            if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                String smsMessage = createPaymentProcessedSmsMessage(payment);
                sendSmsNotification(user.getPhone(), smsMessage);
            }
            
            logger.info("Уведомление о списании отправлено пользователю {}", user.getEmail());
            
        } catch (Exception e) {
            logger.error("Ошибка при отправке уведомления о списании: {}", e.getMessage());
        }
    }

    /**
     * Отправка уведомления пользователю о неудачном платеже
     */
    public void sendPaymentFailedNotification(DailyPayment payment, String errorMessage) {
        try {
            User user = payment.getAccount().getUser();
            String subject = "Ошибка списания средств за аренду";
            String message = createPaymentFailedMessage(payment, errorMessage);
            
            // Отправляем email
            sendEmailNotification(user.getEmail(), subject, message);
            
            // Отправляем SMS (если есть номер телефона)
            if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                String smsMessage = createPaymentFailedSmsMessage(payment, errorMessage);
                sendSmsNotification(user.getPhone(), smsMessage);
            }
            
            logger.info("Уведомление об ошибке списания отправлено пользователю {}", user.getEmail());
            
        } catch (Exception e) {
            logger.error("Ошибка при отправке уведомления об ошибке списания: {}", e.getMessage());
        }
    }

    /**
     * Отправка уведомления администратору о неудачном платеже
     */
    public void sendAdminPaymentFailureNotification(DailyPayment payment, String errorMessage) {
        try {
            // Получаем роль ADMIN через репозиторий
            Role adminRole = roleRepository.findByName("ROLE_ADMIN");
            if (adminRole == null) {
                logger.error("Роль ROLE_ADMIN не найдена в базе данных!");
                return;
            }
            List<User> admins = userService.findUsersByRole(adminRole);
            String subject = "ТРЕВОГА: Неудачный платеж по аренде";
            String message = createAdminPaymentFailureMessage(payment, errorMessage);
            for (User admin : admins) {
                sendEmailNotification(admin.getEmail(), subject, message);
            }
            logger.info("Уведомление о неудачном платеже отправлено {} администраторам", admins.size());
        } catch (Exception e) {
            logger.error("Ошибка при отправке уведомления администратору: {}", e.getMessage());
        }
    }

    /**
     * Отправка уведомления о низком балансе
     */
    public void sendLowBalanceNotification(DailyPayment payment, BigDecimal currentBalance, BigDecimal requiredAmount) {
        try {
            User user = payment.getAccount().getUser();
            String subject = "Внимание: Низкий баланс счета";
            String message = createLowBalanceMessage(payment, currentBalance, requiredAmount);
            
            // Отправляем email
            sendEmailNotification(user.getEmail(), subject, message);
            
            // Отправляем SMS (если есть номер телефона)
            if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                String smsMessage = createLowBalanceSmsMessage(payment, currentBalance, requiredAmount);
                sendSmsNotification(user.getPhone(), smsMessage);
            }
            
            logger.info("Уведомление о низком балансе отправлено пользователю {}", user.getEmail());
            
        } catch (Exception e) {
            logger.error("Ошибка при отправке уведомления о низком балансе: {}", e.getMessage());
        }
    }

    /**
     * Отправка email уведомления
     */
    private void sendEmailNotification(String email, String subject, String message) {
        try {
            // Используем рефлексию для вызова приватного метода sendEmail
            java.lang.reflect.Method sendEmailMethod = EmailService.class.getDeclaredMethod("sendEmail", String.class, String.class, String.class);
            sendEmailMethod.setAccessible(true);
            sendEmailMethod.invoke(emailService, email, subject, message);
        } catch (Exception e) {
            logger.error("Ошибка при отправке email на {}: {}", email, e.getMessage());
        }
    }

    /**
     * Отправка SMS уведомления
     */
    private void sendSmsNotification(String phone, String message) {
        try {
            // Используем рефлексию для вызова приватного метода sendSms
            java.lang.reflect.Method sendSmsMethod = SmsService.class.getDeclaredMethod("sendSms", String.class, String.class);
            sendSmsMethod.setAccessible(true);
            sendSmsMethod.invoke(smsService, phone, message);
        } catch (Exception e) {
            logger.error("Ошибка при отправке SMS на {}: {}", phone, e.getMessage());
        }
    }

    /**
     * Создание сообщения о успешном списании
     */
    private String createPaymentProcessedMessage(DailyPayment payment) {
        User user = payment.getAccount().getUser();
        String carInfo = payment.getRental().getCar().getBrand() + " " + payment.getRental().getCar().getModel();
        
        return String.format("""
            <html>
            <body>
                <h2>Списание средств за аренду</h2>
                <p>Уважаемый(ая) %s %s!</p>
                <p>С вашего счета было списано <strong>%s ₽</strong> за аренду автомобиля <strong>%s</strong>.</p>
                <p><strong>Детали платежа:</strong></p>
                <ul>
                    <li>Дата платежа: %s</li>
                    <li>Сумма: %s ₽</li>
                    <li>Автомобиль: %s</li>
                    <li>Номер счета: %s</li>
                </ul>
                <p>Текущий баланс вашего счета: <strong>%s ₽</strong></p>
                <p>Спасибо за использование нашего сервиса!</p>
                <br>
                <p><small>Это автоматическое уведомление. Пожалуйста, не отвечайте на это письмо.</small></p>
            </body>
            </html>
            """,
            user.getFirstName(),
            user.getLastName(),
            payment.getAmount(),
            carInfo,
            payment.getPaymentDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
            payment.getAmount(),
            carInfo,
            payment.getAccount().getAccountNumber(),
            payment.getAccount().getBalance()
        );
    }

    /**
     * Создание SMS сообщения о успешном списании
     */
    private String createPaymentProcessedSmsMessage(DailyPayment payment) {
        String carInfo = payment.getRental().getCar().getBrand() + " " + payment.getRental().getCar().getModel();
        return String.format(
            "Списано %s ₽ за аренду %s. Баланс: %s ₽. Дата: %s",
            payment.getAmount(),
            carInfo,
            payment.getAccount().getBalance(),
            payment.getPaymentDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        );
    }

    /**
     * Создание сообщения о неудачном платеже
     */
    private String createPaymentFailedMessage(DailyPayment payment, String errorMessage) {
        User user = payment.getAccount().getUser();
        String carInfo = payment.getRental().getCar().getBrand() + " " + payment.getRental().getCar().getModel();
        
        return String.format("""
            <html>
            <body>
                <h2>Ошибка списания средств</h2>
                <p>Уважаемый(ая) %s %s!</p>
                <p>Не удалось списать средства за аренду автомобиля <strong>%s</strong>.</p>
                <p><strong>Причина:</strong> %s</p>
                <p><strong>Детали:</strong></p>
                <ul>
                    <li>Дата платежа: %s</li>
                    <li>Требуемая сумма: %s ₽</li>
                    <li>Текущий баланс: %s ₽</li>
                    <li>Автомобиль: %s</li>
                </ul>
                <p><strong>Что делать:</strong></p>
                <ul>
                    <li>Пополните счет на необходимую сумму</li>
                    <li>Или обратитесь к администратору для разрешения отрицательного баланса</li>
                    <li>Платеж будет повторно обработан автоматически</li>
                </ul>
                <p>Спасибо за понимание!</p>
                <br>
                <p><small>Это автоматическое уведомление. Пожалуйста, не отвечайте на это письмо.</small></p>
            </body>
            </html>
            """,
            user.getFirstName(),
            user.getLastName(),
            carInfo,
            errorMessage,
            payment.getPaymentDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
            payment.getAmount(),
            payment.getAccount().getBalance(),
            carInfo
        );
    }

    /**
     * Создание SMS сообщения о неудачном платеже
     */
    private String createPaymentFailedSmsMessage(DailyPayment payment, String errorMessage) {
        return String.format(
            "ОШИБКА: Не удалось списать %s ₽. %s. Пополните счет или обратитесь к администратору.",
            payment.getAmount(),
            errorMessage
        );
    }

    /**
     * Создание сообщения администратору о неудачном платеже
     */
    private String createAdminPaymentFailureMessage(DailyPayment payment, String errorMessage) {
        User user = payment.getAccount().getUser();
        String carInfo = payment.getRental().getCar().getBrand() + " " + payment.getRental().getCar().getModel();
        
        return String.format("""
            <html>
            <body>
                <h2>ТРЕВОГА: Неудачный платеж по аренде</h2>
                <p><strong>Детали платежа:</strong></p>
                <ul>
                    <li>ID платежа: %d</li>
                    <li>Пользователь: %s %s (%s)</li>
                    <li>Телефон: %s</li>
                    <li>Автомобиль: %s</li>
                    <li>Дата платежа: %s</li>
                    <li>Сумма: %s ₽</li>
                    <li>Текущий баланс: %s ₽</li>
                    <li>Причина ошибки: %s</li>
                </ul>
                <p><strong>Рекомендуемые действия:</strong></p>
                <ul>
                    <li>Связаться с пользователем</li>
                    <li>Пополнить счет пользователя</li>
                    <li>Или разрешить отрицательный баланс</li>
                    <li>Повторно обработать платеж</li>
                </ul>
                <p><a href="http://localhost:8080/admin/payments/rental/%d">Просмотреть детали аренды</a></p>
            </body>
            </html>
            """,
            payment.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getPhone() != null ? user.getPhone() : "Не указан",
            carInfo,
            payment.getPaymentDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
            payment.getAmount(),
            payment.getAccount().getBalance(),
            errorMessage,
            payment.getRental().getId()
        );
    }

    /**
     * Создание сообщения о низком балансе
     */
    private String createLowBalanceMessage(DailyPayment payment, BigDecimal currentBalance, BigDecimal requiredAmount) {
        User user = payment.getAccount().getUser();
        String carInfo = payment.getRental().getCar().getBrand() + " " + payment.getRental().getCar().getModel();
        
        return String.format("""
            <html>
            <body>
                <h2>Внимание: Низкий баланс счета</h2>
                <p>Уважаемый(ая) %s %s!</p>
                <p>На вашем счете недостаточно средств для оплаты аренды автомобиля <strong>%s</strong>.</p>
                <p><strong>Детали:</strong></p>
                <ul>
                    <li>Текущий баланс: %s ₽</li>
                    <li>Требуемая сумма: %s ₽</li>
                    <li>Недостаток: %s ₽</li>
                    <li>Автомобиль: %s</li>
                    <li>Дата платежа: %s</li>
                </ul>
                <p><strong>Что делать:</strong></p>
                <ul>
                    <li>Пополните счет на недостающую сумму</li>
                    <li>Или обратитесь к администратору для разрешения отрицательного баланса</li>
                </ul>
                <p>Спасибо за понимание!</p>
                <br>
                <p><small>Это автоматическое уведомление. Пожалуйста, не отвечайте на это письмо.</small></p>
            </body>
            </html>
            """,
            user.getFirstName(),
            user.getLastName(),
            carInfo,
            currentBalance,
            requiredAmount,
            requiredAmount.subtract(currentBalance),
            carInfo,
            payment.getPaymentDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        );
    }

    /**
     * Создание SMS сообщения о низком балансе
     */
    private String createLowBalanceSmsMessage(DailyPayment payment, BigDecimal currentBalance, BigDecimal requiredAmount) {
        return String.format(
            "ВНИМАНИЕ: Недостаточно средств. Баланс: %s ₽, требуется: %s ₽. Пополните счет или обратитесь к администратору.",
            currentBalance,
            requiredAmount
        );
    }
} 