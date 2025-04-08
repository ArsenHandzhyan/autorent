package ru.anapa.autorent.controller;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.anapa.autorent.dto.UserRegistrationDto;
import ru.anapa.autorent.model.User;
import ru.anapa.autorent.service.UserService;

import java.security.Principal;

@Controller
@RequestMapping("/")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

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
                            Model model) {
        logger.info("Login page requested");

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
                               BindingResult result, Model model) {
        logger.info("Registration attempt with data: {}", registrationDto);

        if (result.hasErrors()) {
            logger.warn("Validation errors in registration form: {}", result.getAllErrors());
            return "auth/register";
        }

        try {
            logger.info("Attempting to create new user");
            User user = userService.registerUser(
                    registrationDto.getEmail(),
                    registrationDto.getPassword(),
                    registrationDto.getFirstName(),
                    registrationDto.getLastName(),
                    registrationDto.getPhone()
            );

            logger.info("User created successfully, attempting auto-login");
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            registrationDto.getEmail(),
                            registrationDto.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            logger.info("Auto-login successful, redirecting to dashboard");
            return "redirect:/admin/dashboard";
        } catch (RuntimeException e) {
            logger.error("Error during registration: {}", e.getMessage(), e);
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    @PostMapping("auth/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        Model model) {
        try {
            logger.info("Attempting to authenticate user: {}", username);

            // Проверка существования пользователя
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            logger.info("User found: {}, enabled: {}", username, userDetails.isEnabled());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            logger.info("Authentication successful for user: {}", username);
            return "redirect:/admin/dashboard";
        } catch (AuthenticationCredentialsNotFoundException e) {
            logger.error("Authentication failed: User not found - {}", e.getMessage(), e);
            model.addAttribute("loginError", "Пользователь не найден");
        } catch (BadCredentialsException e) {
            logger.error("Authentication failed: Invalid password - {}", e.getMessage(), e);
            model.addAttribute("loginError", "Неверный пароль");
        } catch (DisabledException e) {
            logger.error("Authentication failed: Account disabled - {}", e.getMessage(), e);
            model.addAttribute("loginError", "Ваш аккаунт отключен. Пожалуйста, свяжитесь с администратором.");
        } catch (Exception e) {
            logger.error("Authentication failed: Unexpected error - {}", e.getMessage(), e);
            model.addAttribute("loginError", "Произошла ошибка при аутентификации: " + e.getMessage());
        }

        return "auth/login";
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