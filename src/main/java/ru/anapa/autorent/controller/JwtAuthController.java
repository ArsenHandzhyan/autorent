package ru.anapa.autorent.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import ru.anapa.autorent.dto.JwtRequest;
import ru.anapa.autorent.dto.JwtResponse;
import ru.anapa.autorent.dto.UserRegistrationDto;
import ru.anapa.autorent.model.User;
import ru.anapa.autorent.service.UserService;
import ru.anapa.autorent.service.VerificationTokenService;
import ru.anapa.autorent.util.JwtTokenUtil;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class JwtAuthController {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthController.class);
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final VerificationTokenService verificationTokenService;

    @Autowired
    public JwtAuthController(AuthenticationManager authenticationManager,
                             @Lazy JwtTokenUtil jwtTokenUtil,
                             @Lazy UserDetailsService userDetailsService,
                             @Lazy UserService userService,
                             @Lazy VerificationTokenService verificationTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
        this.verificationTokenService = verificationTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) {
        logger.info("JWT login attempt with email: {}", authenticationRequest.getEmail());
        
        try {
            authenticate(authenticationRequest.getEmail(), authenticationRequest.getPassword());
            
            User user = userService.findByEmail(authenticationRequest.getEmail());
            
            // Обновляем дату последнего входа
            userService.updateLastLoginDate(authenticationRequest.getEmail());
            
            // Создаем JWT токен с дополнительной информацией о пользователе
            final String token = jwtTokenUtil.generateTokenWithUserInfo(user);
            
            // Создаем Refresh токен для продления сессии
            final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            final String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("refreshToken", refreshToken);
            response.put("email", user.getEmail());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("userId", user.getId());
            response.put("expiresIn", jwtTokenUtil.getTokenRemainingTimeInSeconds(token));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error during JWT authentication: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Неверное имя пользователя или пароль"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationDto registrationDto) {
        logger.info("API registration attempt with email: {}", registrationDto.getEmail());
        
        try {
            // Проверка существования пользователя с таким email
            if (userService.existsByEmail(registrationDto.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email уже используется другим пользователем"));
            }
            
            // Проверка совпадения паролей
            if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Пароли не совпадают"));
            }
            
            // Проверка существования пользователя с таким телефоном
            if (userService.existsByPhone(registrationDto.getPhone())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Телефон уже используется другим пользователем"));
            }
            
            // Нормализуем email перед сохранением
            String normalizedEmail = registrationDto.getEmail().toLowerCase().trim();
            
            User user = userService.registerUser(
                    normalizedEmail,
                    registrationDto.getPassword(),
                    registrationDto.getFirstName().trim(),
                    registrationDto.getLastName().trim(),
                    registrationDto.getPhone()
            );
            
            // Создаем JWT токен с дополнительной информацией о пользователе
            final String token = jwtTokenUtil.generateTokenWithUserInfo(user);
            
            // Создаем Refresh токен для продления сессии
            final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            final String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("refreshToken", refreshToken);
            response.put("email", user.getEmail());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("userId", user.getId());
            response.put("expiresIn", jwtTokenUtil.getTokenRemainingTimeInSeconds(token));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error during API registration: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Проверка валидности существующего токена
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Убираем "Bearer " из заголовка
            
            if (!jwtTokenUtil.validateTokenOnly(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("valid", false, "message", "Токен недействителен или срок его действия истек"));
            }
            
            String username = jwtTokenUtil.getUsernameFromToken(token);
            User user = userService.findByEmail(username);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("valid", false, "message", "Пользователь не найден"));
            }
            
            boolean isAdmin = jwtTokenUtil.isUserAdmin(token);
            
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "user", Map.of(
                            "email", user.getEmail(),
                            "firstName", user.getFirstName(),
                            "lastName", user.getLastName(),
                            "id", user.getId(),
                            "isAdmin", isAdmin
                    ),
                    "expiresIn", jwtTokenUtil.getTokenRemainingTimeInSeconds(token)
            ));
        } catch (Exception e) {
            logger.error("Error validating token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "message", "Токен недействителен"));
        }
    }
    
    // Обновление access токена с помощью refresh токена
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String refreshToken = authHeader.substring(7); // Убираем "Bearer " из заголовка
            
            if (!jwtTokenUtil.validateTokenOnly(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Refresh токен недействителен или срок его действия истек"));
            }
            
            String username = jwtTokenUtil.getUsernameFromToken(refreshToken);
            User user = userService.findByEmail(username);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Пользователь не найден"));
            }
            
            // Создаем новый access токен
            final String newToken = jwtTokenUtil.generateTokenWithUserInfo(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", newToken);
            response.put("expiresIn", jwtTokenUtil.getTokenRemainingTimeInSeconds(newToken));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error refreshing token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Не удалось обновить токен"));
        }
    }

    // Валидация полей при регистрации
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email.toLowerCase().trim());
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("message", exists ? "Email уже используется" : "Email доступен");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-phone")
    public ResponseEntity<?> checkPhone(@RequestParam String phone) {
        boolean exists = userService.existsByPhone(phone);
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("message", exists ? "Телефон уже используется" : "Телефон доступен");
        return ResponseEntity.ok(response);
    }

    // Отправка кода подтверждения на email
    @PostMapping("/send-email-code")
    public ResponseEntity<?> sendEmailVerificationCode(@RequestParam String email) {
        try {
            verificationTokenService.createEmailVerificationToken(email);
            return ResponseEntity.ok(Map.of(
                    "success", true, 
                    "message", "Код подтверждения отправлен на указанный email"
            ));
        } catch (Exception e) {
            logger.error("Error sending email verification code: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // Проверка кода подтверждения email
    @PostMapping("/verify-email-code")
    public ResponseEntity<?> verifyEmailCode(@RequestParam String email, @RequestParam String code) {
        boolean verified = verificationTokenService.verifyEmailCode(email, code);
        if (verified) {
            return ResponseEntity.ok(Map.of(
                    "verified", true,
                    "message", "Email успешно подтвержден"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "verified", false,
                    "message", "Неверный код подтверждения или срок его действия истек"
            ));
        }
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            
            if (!auth.isAuthenticated()) {
                throw new BadCredentialsException("Invalid credentials");
            }
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
} 