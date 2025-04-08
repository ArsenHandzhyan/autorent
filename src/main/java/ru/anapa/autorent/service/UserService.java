package ru.anapa.autorent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.anapa.autorent.model.Role;
import ru.anapa.autorent.model.User;
import ru.anapa.autorent.repository.RoleRepository;
import ru.anapa.autorent.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Загрузка пользователя по email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Пользователь не найден с email: {}", email);
                    return new UsernameNotFoundException("Пользователь не найден: " + email);
                });

        try {
            // Обновляем дату последнего входа
            user.setLastLoginDate(LocalDateTime.now());
            userRepository.save(user);
        } catch (Exception e) {
            logger.error("Ошибка при обновлении даты последнего входа: {}", e.getMessage(), e);
            // Не выбрасываем исключение, чтобы не прерывать процесс аутентификации
        }

        // Преобразуем коллекцию ролей в коллекцию SimpleGrantedAuthority
        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());

        logger.debug("Пользователь найден: {}, роли: {}, enabled: {}", email, authorities, user.isEnabled());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities
        );
    }

    @Transactional
    public User registerUser(String email, String password, String firstName,
                             String lastName, String phone) {
        logger.info("Регистрация нового пользователя с email: {}", email);

        if (userRepository.existsByEmail(email)) {
            logger.warn("Попытка регистрации с существующим email: {}", email);
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        if (userRepository.existsByPhone(phone)) {
            logger.warn("Попытка регистрации с существующим телефоном: {}", phone);
            throw new RuntimeException("Пользователь с таким телефоном уже существует");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setEnabled(true);

        // Устанавливаем текущую дату и время для поля registrationDate
        user.setRegistrationDate(LocalDateTime.now());

        // Добавляем роль USER по умолчанию
        Role userRole = roleRepository.findByName("ROLE_USER");
        if (userRole == null) {
            logger.info("Создание роли ROLE_USER");
            userRole = new Role("ROLE_USER");
            roleRepository.save(userRole);
        }
        user.addRole(userRole);

        User savedUser = userRepository.save(user);
        logger.info("Пользователь успешно зарегистрирован: {}", email);

        return savedUser;
    }

    public User findByEmail(String email) {
        logger.debug("Поиск пользователя по email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Пользователь не найден с email: {}", email);
                    return new RuntimeException("Пользователь не найден");
                });
    }

    public User findById(Long id) {
        logger.debug("Поиск пользователя по ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Пользователь не найден с ID: {}", id);
                    return new RuntimeException("Пользователь не найден с ID: " + id);
                });
    }

    public List<User> findAllUsers() {
        logger.debug("Получение списка всех пользователей");
        return userRepository.findAll();
    }

    public List<User> findDisabledUsers() {
        logger.debug("Получение списка отключенных пользователей");
        return userRepository.findByEnabledFalse();
    }

    public List<User> findUsersByRole(Role role) {
        logger.debug("Поиск пользователей по роли: {}", role.getName());
        return userRepository.findByRoles(role);
    }

    @Transactional
    public User updateUser(User user) {
        logger.info("Обновление пользователя с ID: {}", user.getId());
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        logger.info("Удаление пользователя с ID: {}", userId);
        userRepository.deleteById(userId);
    }

    @Transactional
    public User disableUser(Long userId) {
        logger.info("Отключение пользователя с ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Пользователь не найден с ID: {}", userId);
                    return new RuntimeException("Пользователь не найден");
                });
        user.setEnabled(false);
        return userRepository.save(user);
    }

    @Transactional
    public User enableUser(Long userId) {
        logger.info("Включение пользователя с ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Пользователь не найден с ID: {}", userId);
                    return new RuntimeException("Пользователь не найден");
                });
        user.setEnabled(true);
        return userRepository.save(user);
    }

    @Transactional
    public User promoteToAdmin(Long userId) {
        logger.info("Повышение пользователя до администратора, ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Пользователь не найден с ID: {}", userId);
                    return new RuntimeException("Пользователь не найден");
                });

        // Добавляем роль ADMIN
        Role adminRole = roleRepository.findByName("ROLE_ADMIN");
        if (adminRole == null) {
            logger.info("Создание роли ROLE_ADMIN");
            adminRole = new Role("ROLE_ADMIN");
            roleRepository.save(adminRole);
        }
        user.addRole(adminRole);

        User savedUser = userRepository.save(user);
        logger.info("Пользователь успешно повышен до администратора, ID: {}", userId);
        return savedUser;
    }

    @Transactional
    public User createAdminUser(String email, String password, String firstName, String lastName, String phone) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setEnabled(true);

        Role adminRole = roleRepository.findByName("ROLE_ADMIN");
        if (adminRole == null) {
            adminRole = new Role("ROLE_ADMIN");
            roleRepository.save(adminRole);
        }
        user.addRole(adminRole);

        return userRepository.save(user);
    }

    @Transactional
    public User updateUserProfile(Long userId, String firstName, String lastName, String phone) {
        logger.info("Обновление профиля пользователя с ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Пользователь не найден с ID: {}", userId);
                    return new RuntimeException("Пользователь не найден");
                });

        user.setFirstName(firstName);
        user.setLastName(lastName);

        // Проверяем, не занят ли телефон другим пользователем
        if (!phone.equals(user.getPhone()) && userRepository.existsByPhone(phone)) {
            logger.warn("Попытка обновления профиля с существующим телефоном: {}", phone);
            throw new RuntimeException("Пользователь с таким телефоном уже существует");
        }
        user.setPhone(phone);

        User savedUser = userRepository.save(user);
        logger.info("Профиль пользователя успешно обновлен, ID: {}", userId);
        return savedUser;
    }

    @Transactional
    public User changePassword(Long userId, String currentPassword, String newPassword) {
        logger.info("Изменение пароля пользователя с ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Пользователь не найден с ID: {}", userId);
                    return new RuntimeException("Пользователь не найден");
                });

        // Проверяем текущий пароль
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            logger.warn("Неверный текущий пароль при попытке изменения пароля, ID: {}", userId);
            throw new RuntimeException("Неверный текущий пароль");
        }

        // Устанавливаем новый пароль
        user.setPassword(passwordEncoder.encode(newPassword));

        User savedUser = userRepository.save(user);
        logger.info("Пароль пользователя успешно изменен, ID: {}", userId);
        return savedUser;
    }

    @Transactional
    public User resetPassword(String email, String newPassword) {
        logger.info("Сброс пароля для пользователя с email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Пользователь не найден с email: {}", email);
                    return new RuntimeException("Пользователь не найден");
                });

        // Устанавливаем новый пароль
        user.setPassword(passwordEncoder.encode(newPassword));

        User savedUser = userRepository.save(user);
        logger.info("Пароль пользователя успешно сброшен, email: {}", email);
        return savedUser;
    }

    // Метод для обновления времени последнего входа, если у вас есть такое поле
    @Transactional
    public void updateLastLoginDate(String email) {
        logger.debug("Обновление времени последнего входа для пользователя с email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Пользователь не найден с email: {}", email);
                    return new RuntimeException("Пользователь не найден");
                });

        // Если у вас есть поле lastLoginDate в User, раскомментируйте следующие строки
        // user.setLastLoginDate(LocalDateTime.now());
        // userRepository.save(user);
        // logger.debug("Время последнего входа обновлено для пользователя с email: {}", email);
    }
}