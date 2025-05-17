package ru.anapa.autorent.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.anapa.autorent.dto.UserRegistrationDto;
import ru.anapa.autorent.model.User;
import ru.anapa.autorent.service.UserService;
import ru.anapa.autorent.service.SmsService;
import ru.anapa.autorent.service.VerificationTokenService;
import ru.anapa.autorent.model.VerificationToken;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.concurrent.ConcurrentHashMap;


@Controller
@RequestMapping("/")
public class AuthController {
    // В классе AuthController добавляем временное хранилище кодов
    private final ConcurrentHashMap<String, String> emailVerificationCodes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> smsVerificationCodes = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final SmsService smsService;
    private final VerificationTokenService verificationTokenService;

    // Регулярное выражение для проверки телефонного номера
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+7\\([0-9]{3}\\)[0-9]{3}-[0-9]{2}-[0-9]{2}$");
    // Регулярное выражение для проверки email
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    @Autowired
    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService,
                          SmsService smsService,
                          VerificationTokenService verificationTokenService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.smsService = smsService;
        this.verificationTokenService = verificationTokenService;
        logger.info("AuthController initialized");
    }

    @GetMapping("auth/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "expired", required = false) String expired,
                            @RequestParam(value = "disabled", required = false) String disabled,
                            @RequestParam(value = "success", required = false) String success,
                            @RequestParam(value = "name", required = false) String name,
                            HttpServletRequest request,
                            Model model) {
        logger.info("Login page requested");

        if (success != null && success.equals("true")) {
            model.addAttribute("registrationSuccess", true);
            model.addAttribute("welcomeMessage",
                    "Добро пожаловать, " + (name != null ? name : "") + "! Регистрация успешно завершена. Пожалуйста, войдите в систему.");
        }

        // Инициализируем переменную по умолчанию как false
        model.addAttribute("adminLoginRequired", false);

        // Проверяем наличие атрибутов сессии для сообщения о доступе к админ-панели
        HttpSession session = request.getSession(false);
        if (session != null) {
            Boolean adminLoginRequired = (Boolean) session.getAttribute("adminLoginRequired");
            if (adminLoginRequired != null && adminLoginRequired) {
                logger.info("Admin login required message detected in session");
                model.addAttribute("adminLoginRequired", true);
                model.addAttribute("adminLoginError", session.getAttribute("error"));

                // Удаляем атрибуты из сессии после использования
                session.removeAttribute("adminLoginRequired");
                session.removeAttribute("error");
            }
        }

        if (error != null) {
            logger.warn("Login error detected");
            if (disabled != null) {
                model.addAttribute("loginError", "Ваш аккаунт отключен. Пожалуйста, свяжитесь с администратором.");
            } else {
                model.addAttribute("loginError", "Неверное имя пользователя или пароль");
            }
        }

        if (logout != null) {
            logger.info("User logged out");
            model.addAttribute("logoutMessage", "Вы успешно вышли из системы");
        }

        if (expired != null) {
            logger.info("Session expired");
            model.addAttribute("expiredMessage", "Ваша сессия истекла. Пожалуйста, войдите снова.");
        }

        if (model.containsAttribute("registrationSuccess")) {
            logger.info("Registration success message detected");
        }

        return "auth/login";
    }

    @GetMapping("auth/register")
    public String showRegistrationForm(Model model) {
        logger.info("Registration form requested");
        model.addAttribute("user", new UserRegistrationDto());
        return "auth/register";
    }

    @PostMapping("auth/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
                               BindingResult result, Model model, HttpServletRequest request) {
        logger.info("Registration attempt with email: {}", registrationDto.getEmail());

        // Проверка совпадения паролей
        if (registrationDto.getPassword() != null && registrationDto.getConfirmPassword() != null &&
                !registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            result.addError(new FieldError("user", "confirmPassword", registrationDto.getConfirmPassword(),
                    false, null, null, "Пароли не совпадают"));
        }

        // Проверка существования пользователя с таким email
        if (userService.existsByEmail(registrationDto.getEmail())) {
            result.addError(new FieldError("user", "email", registrationDto.getEmail(),
                    false, null, null, "Пользователь с таким email уже зарегистрирован"));
        }

        // Проверка существования пользователя с таким телефоном
        if (userService.existsByPhone(registrationDto.getPhone())) {
            result.addError(new FieldError("user", "phone", registrationDto.getPhone(),
                    false, null, null, "Пользователь с таким телефоном уже зарегистрирован"));
        }

        if (result.hasErrors()) {
            logger.warn("Validation errors in registration form: {}", result.getAllErrors());
            return "auth/register";
        }

        try {
            logger.info("Attempting to create new user");

            // Нормализуем email перед сохранением
            String normalizedEmail = registrationDto.getEmail().toLowerCase().trim();

            User user = userService.registerUser(
                    normalizedEmail,
                    registrationDto.getPassword(),
                    registrationDto.getFirstName().trim(),
                    registrationDto.getLastName().trim(),
                    registrationDto.getPhone()
            );

            logger.info("User created successfully with ID: {}", user.getId());

            return "redirect:/auth/login?success=true&name=" + user.getFirstName();

        } catch (Exception e) {
            logger.error("Error during registration: {}", e.getMessage(), e);
            model.addAttribute("error", "Произошла ошибка при регистрации: " + e.getMessage());
            return "auth/register";
        }
    }


    @GetMapping("/api/validate/email")
    @ResponseBody
    public Map<String, Object> validateEmail(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean exists = userService.existsByEmail(email);
            response.put("valid", !exists);
            response.put("message", exists ? "Email уже используется" : "Email доступен");
        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", "Ошибка проверки email");
        }
        return response;
    }

    @PostMapping("auth/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        Model model,
                        HttpServletRequest request,
                        RedirectAttributes redirectAttributes) {
        try {
            logger.info("Attempting to authenticate user: {}", username);

            // Нормализация email перед аутентификацией
            username = username.toLowerCase().trim();

            // Проверка существования пользователя
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                logger.info("User found: {}, enabled: {}", username, userDetails.isEnabled());

                if (!userDetails.isEnabled()) {
                    logger.warn("Attempted login to disabled account: {}", username);
                    model.addAttribute("loginError", "Ваш аккаунт отключен. Пожалуйста, свяжитесь с администратором.");
                    return "auth/login";
                }
            } catch (Exception e) {
                logger.warn("User not found during login attempt: {}", username);
                model.addAttribute("loginError", "Пользователь не найден");
                return "auth/login";
            }

            // Попытка аутентификации
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // Если аутентификация успешна, устанавливаем контекст безопасности
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("Authentication successful for user: {}", username);

            // Сохраняем информацию о последнем входе
            userService.updateLastLoginDate(username);

            // Получаем информацию о пользователе для приветственного сообщения
            User user = userService.findByEmail(username);
            if (user != null) {
                redirectAttributes.addFlashAttribute("welcomeBack", true);
                redirectAttributes.addFlashAttribute("userName", user.getFirstName());
            }

            // Если пользователь пытался получить доступ к админ-панели, перенаправляем его туда
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("adminAccessAttempt") != null) {
                session.removeAttribute("adminAccessAttempt");
                boolean isAdmin = authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
                if (isAdmin) {
                    logger.info("Admin successfully authenticated, redirecting to admin dashboard");
                    return "redirect:/admin/dashboard";
                }
            }

            // Если в сессии указан сохранённый URL, перенаправляем туда
            String redirectUrl = (String) session.getAttribute("REDIRECT_URL");
            if (redirectUrl != null) {
                session.removeAttribute("REDIRECT_URL");
                return "redirect:" + redirectUrl;
            }

            return "redirect:/";
        } catch (BadCredentialsException e) {
            logger.error("Authentication failed: Invalid password for user {}", username);
            model.addAttribute("loginError", "Неверный пароль");
        } catch (DisabledException e) {
            logger.error("Authentication failed: Account disabled - {}", e.getMessage());
            model.addAttribute("loginError", "Ваш аккаунт отключен. Пожалуйста, свяжитесь с администратором.");
        } catch (LockedException e) {
            logger.error("Authentication failed: Account locked - {}", e.getMessage());
            model.addAttribute("loginError", "Ваш аккаунт временно заблокирован. Пожалуйста, попробуйте позже или свяжитесь с администратором.");
        } catch (Exception e) {
            logger.error("Authentication failed: Unexpected error - {}", e.getMessage(), e);
            model.addAttribute("loginError", "Произошла ошибка при аутентификации. Пожалуйста, попробуйте позже.");
        }

        return "auth/login";
    }

    @GetMapping("auth/logout")
    public String logout(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        logger.info("Logout requested");

        // Получаем текущего пользователя перед выходом
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() &&
                !auth.getPrincipal().equals("anonymousUser")) {
            logger.info("User {} is logging out", auth.getName());

            // Добавляем сообщение о успешном выходе
            redirectAttributes.addFlashAttribute("logoutMessage", "Вы успешно вышли из системы");

            // Инвалидация сессии
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            // Очистка контекста безопасности
            SecurityContextHolder.clearContext();
        }

        return "redirect:/auth/login?logout";
    }

    @GetMapping("auth/check-email")
    @ResponseBody
    public boolean checkEmailExists(@RequestParam("email") String email) {
        logger.debug("Checking if email exists: {}", email);
        return userService.existsByEmail(email.toLowerCase().trim());
    }

    @GetMapping("auth/check-phone")
    @ResponseBody
    public boolean checkPhoneExists(@RequestParam("phone") String phone) {
        logger.debug("Checking if phone exists: {}", phone);
        return userService.existsByPhone(phone);
    }

    @GetMapping("/error")
    public String handleError(Model model, Principal principal) {
        try {
            if (principal != null) {
                logger.debug("Error handling for authenticated user: {}", principal.getName());
                model.addAttribute("errorMessage", "Произошла ошибка при обработке запроса");
            } else {
                logger.debug("Error handling for unauthenticated user");
                model.addAttribute("errorMessage", "Необходимо авторизоваться");
            }
            return "error";
        } catch (Exception ex) {
            logger.error("Error during error handling", ex);
            model.addAttribute("errorMessage", "Произошла внутренняя ошибка");
            return "error";
        }
    }


    // Эндпоинт для отправки кода на email
    @PostMapping("/auth/send-email-code")
    @ResponseBody
    public Map<String, Object> sendEmailCode(@RequestParam("email") String email) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Имитация генерации кода
            String code = "123456"; // В реальном проекте генерировать случайный код
            emailVerificationCodes.put(email.toLowerCase().trim(), code);
            logger.info("Generated email verification code for {}: {}", email, code);
            // В реальном проекте здесь отправка email через сервис (например, JavaMailSender)
            response.put("success", true);
            response.put("message", "Код отправлен на email");
        } catch (Exception e) {
            logger.error("Error sending email code to {}: {}", email, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Ошибка при отправке кода на email");
        }
        return response;
    }

    // Эндпоинт для проверки кода email
    @PostMapping("/auth/verify-email-code")
    @ResponseBody
    public Map<String, Object> verifyEmailCode(@RequestParam("email") String email, @RequestParam("code") String code) {
        Map<String, Object> response = new HashMap<>();
        try {
            String storedCode = emailVerificationCodes.get(email.toLowerCase().trim());
            if (storedCode != null && storedCode.equals(code)) {
                response.put("success", true);
                response.put("message", "Код подтвержден");
                emailVerificationCodes.remove(email.toLowerCase().trim()); // Удаляем после успешной проверки
            } else {
                response.put("success", false);
                response.put("message", "Неверный код");
            }
        } catch (Exception e) {
            logger.error("Error verifying email code for {}: {}", email, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Ошибка при проверке кода");
        }
        return response;
    }

    // Эндпоинт для отправки SMS кода
    @PostMapping("/auth/send-sms-code")
    @ResponseBody
    public Map<String, Object> sendSmsCode(@RequestParam("phone") String phone) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Проверяем формат номера телефона
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                response.put("success", false);
                response.put("message", "Неверный формат номера телефона");
                return response;
            }

            // Проверяем, не превышен ли лимит попыток
            if (verificationTokenService.hasTooManyAttempts(phone, VerificationToken.TokenType.SMS_VERIFICATION)) {
                response.put("success", false);
                response.put("message", "Превышен лимит попыток. Попробуйте позже");
                return response;
            }

            // Генерируем и отправляем код
            String code = smsService.generateAndSendOtp(phone);
            
            // Сохраняем код в базе данных
            verificationTokenService.createSmsVerificationToken(phone, code);
            
            response.put("success", true);
            response.put("message", "Код отправлен на телефон");
        } catch (Exception e) {
            logger.error("Error sending SMS code to {}: {}", phone, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Ошибка при отправке кода на телефон");
        }
        return response;
    }

    // Эндпоинт для отправки кода верификации через звонок
    @PostMapping("/auth/send-call-code")
    @ResponseBody
    public Map<String, Object> sendCallCode(@RequestParam("phone") String phone) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Валидация номера телефона
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                response.put("success", false);
                response.put("message", "Неверный формат номера телефона");
                return response;
            }

            // Проверка, не существует ли уже такой телефон
            if (userService.existsByPhone(phone)) {
                response.put("success", false);
                response.put("message", "Пользователь с таким номером телефона уже зарегистрирован");
                return response;
            }

            // Генерируем и отправляем код через звонок
            String code = smsService.generateAndSendCallOtp(phone);

            // Сохраняем код для последующей проверки
            smsVerificationCodes.put(phone, code);

            response.put("success", true);
            response.put("message", "Код верификации отправлен через звонок на номер " + phone);
            logger.info("Код верификации через звонок отправлен на номер: {}", phone);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка при отправке кода через звонок: " + e.getMessage());
            logger.error("Ошибка при отправке кода через звонок на номер {}: {}", phone, e.getMessage(), e);
        }

        return response;
    }

    // Эндпоинт для проверки кода из звонка
    @PostMapping("/auth/verify-call-code")
    @ResponseBody
    public Map<String, Object> verifyCallCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Получаем сохраненный код из кэша
            String savedCode = smsVerificationCodes.get(phone);

            if (savedCode == null) {
                response.put("success", false);
                response.put("message", "Код верификации не был отправлен или истек срок его действия");
                return response;
            }

            // Проверка кода
            if (savedCode.equals(code)) {
                response.put("success", true);
                response.put("message", "Номер телефона успешно подтвержден");
                logger.info("Номер телефона {} успешно подтвержден через звонок", phone);
            } else {
                response.put("success", false);
                response.put("message", "Неверный код верификации");
                logger.warn("Введен неверный код при проверке через звонок для номера {}", phone);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка при проверке кода: " + e.getMessage());
            logger.error("Ошибка при проверке кода из звонка для номера {}: {}", phone, e.getMessage(), e);
        }

        return response;
    }

    // Эндпоинт для проверки SMS кода
    @PostMapping("/auth/verify-sms-code")
    @ResponseBody
    public Map<String, Object> verifySmsCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                response.put("success", false);
                response.put("message", "Неверный формат номера телефона");
                return response;
            }

            boolean isValid = verificationTokenService.verifySmsCode(phone, code);
            if (isValid) {
                response.put("success", true);
                response.put("message", "Код подтвержден");
            } else {
                response.put("success", false);
                response.put("message", "Неверный код или срок его действия истек");
            }
        } catch (Exception e) {
            logger.error("Error verifying SMS code for {}: {}", phone, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Ошибка при проверке кода");
        }
        return response;
    }

}