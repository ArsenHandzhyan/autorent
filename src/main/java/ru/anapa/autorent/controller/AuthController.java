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

import java.security.Principal;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    // Регулярное выражение для проверки телефонного номера
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+7\\([0-9]{3}\\)[0-9]{3}-[0-9]{2}-[0-9]{2}$");
    // Регулярное выражение для проверки email
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    @Autowired
    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        logger.info("AuthController initialized");
    }

    @GetMapping("auth/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "expired", required = false) String expired,
                            @RequestParam(value = "disabled", required = false) String disabled,
                            HttpServletRequest request,
                            Model model) {
        logger.info("Login page requested");

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
                               BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        logger.info("Registration attempt with email: {}", registrationDto.getEmail());

        // Проверка совпадения паролей
        if (registrationDto.getPassword() != null && registrationDto.getConfirmPassword() != null &&
                !registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            result.addError(new FieldError("user", "confirmPassword", "Пароли не совпадают"));
        }

        // Проверка существования пользователя с таким email
        if (userService.existsByEmail(registrationDto.getEmail())) {
            result.addError(new FieldError("user", "email", "Пользователь с таким email уже зарегистрирован"));
        }

        // Проверка существования пользователя с таким телефоном
        if (userService.existsByPhone(registrationDto.getPhone())) {
            result.addError(new FieldError("user", "phone", "Пользователь с таким телефоном уже зарегистрирован"));
        }

        // Проверка сложности пароля (опционально)
        if (registrationDto.getPassword() != null && registrationDto.getPassword().length() >= 6) {
            boolean hasUpperCase = !registrationDto.getPassword().equals(registrationDto.getPassword().toLowerCase());
            boolean hasDigit = registrationDto.getPassword().matches(".*\\d.*");

            if (!(hasUpperCase && hasDigit)) {
                // Добавляем предупреждение, но не блокируем регистрацию
                model.addAttribute("passwordWarning", "Рекомендуется использовать заглавные буквы и цифры для повышения безопасности");
            }
        }

        if (result.hasErrors()) {
            logger.warn("Validation errors in registration form: {}", result.getAllErrors());
            return "auth/register";
        }

        try {
            logger.info("Attempting to create new user");

            // Нормализуем email перед сохранением (приводим к нижнему регистру и удаляем пробелы)
            String normalizedEmail = registrationDto.getEmail().toLowerCase().trim();

            User user = userService.registerUser(
                    normalizedEmail,
                    registrationDto.getPassword(),
                    registrationDto.getFirstName().trim(),
                    registrationDto.getLastName().trim(),
                    registrationDto.getPhone()
            );

            logger.info("User created successfully with ID: {}", user.getId());

            // Добавляем flash-атрибуты для отображения на странице логина
            redirectAttributes.addFlashAttribute("registrationSuccess", true);
            redirectAttributes.addFlashAttribute("welcomeMessage",
                    "Добро пожаловать, " + user.getFirstName() + "! Регистрация успешно завершена. Пожалуйста, войдите в систему.");

            // Перенаправляем на страницу логина
            return "redirect:/auth/login";

        } catch (Exception e) {
            logger.error("Error during registration: {}", e.getMessage(), e);

            // Обработка различных типов ошибок
            if (e.getMessage() != null) {
                if (e.getMessage().contains("email")) {
                    model.addAttribute("error", "Пользователь с таким email уже существует");
                } else if (e.getMessage().contains("телефон")) {
                    model.addAttribute("error", "Пользователь с таким телефоном уже существует");
                } else {
                    model.addAttribute("error", "Произошла ошибка при регистрации: " + e.getMessage());
                }
            } else {
                model.addAttribute("error", "Произошла неизвестная ошибка при регистрации");
            }

            return "auth/register";
        }
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
}