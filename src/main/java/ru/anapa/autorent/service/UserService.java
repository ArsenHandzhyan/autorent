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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    // Регулярное выражение для проверки телефонного номера в формате +7(XXX)XXX-XX-XX
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+7\\(\\d{3}\\)\\d{3}-\\d{2}-\\d{2}$");

    // Регулярное выражение для проверки email
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

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

        User user = userRepository.findByEmail(email.toLowerCase().trim())
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

    /**
     * Регистрирует нового пользователя с валидацией данных
     */
    @Transactional
    public User registerUser(String email, String password, String firstName,
                             String lastName, String phone) {
        logger.info("Регистрация нового пользователя с email: {}", email);

        // Нормализация и валидация email
        email = email.toLowerCase().trim();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            logger.warn("Попытка регистрации с некорректным email: {}", email);
            throw new IllegalArgumentException("Некорректный формат email");
        }

        // Проверка существования пользователя с таким email
        if (userRepository.existsByEmail(email)) {
            logger.warn("Попытка регистрации с существующим email: {}", email);
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        // Валидация телефона
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            logger.warn("Попытка регистрации с некорректным телефоном: {}", phone);
            throw new IllegalArgumentException("Телефон должен быть в формате +7(XXX)XXX-XX-XX");
        }

        // Проверка существования пользователя с таким телефоном
        if (userRepository.existsByPhone(phone)) {
            logger.warn("Попытка регистрации с существующим телефоном: {}", phone);
            throw new RuntimeException("Пользователь с таким телефоном уже существует");
        }

        // Валидация имени и фамилии
        if (firstName == null || firstName.trim().length() < 2) {
            logger.warn("Попытка регистрации с некорректным именем: {}", firstName);
            throw new IllegalArgumentException("Имя должно содержать не менее 2 символов");
        }

        if (lastName == null || lastName.trim().length() < 2) {
            logger.warn("Попытка регистрации с некорректной фамилией: {}", lastName);
            throw new IllegalArgumentException("Фамилия должна содержать не менее 2 символов");
        }

        // Валидация пароля
        if (password == null || password.length() < 6) {
            logger.warn("Попытка регистрации со слабым паролем");
            throw new IllegalArgumentException("Пароль должен содержать не менее 6 символов");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setPhone(phone);
        user.setEnabled(true);

        // Устанавливаем текущую дату и время для поля registrationDate
        user.setRegistrationDate(LocalDateTime.now());
        user.setLastLoginDate(LocalDateTime.now()); // Устанавливаем время первого входа

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

    /**
     * Проверяет существование пользователя с указанным email
     */
    public boolean existsByEmail(String email) {
        if (email == null) {
            return false;
        }
        return userRepository.existsByEmail(email.toLowerCase().trim());
    }

    /**
     * Проверяет существование пользователя с указанным телефоном
     */
    public boolean existsByPhone(String phone) {
        if (phone == null) {
            return false;
        }
        return userRepository.existsByPhone(phone);
    }

    /**
     * Находит пользователя по email
     */
    public User findByEmail(String email) {
        logger.debug("Поиск пользователя по email: {}", email);
        if (email == null) {
            throw new IllegalArgumentException("Email не может быть null");
        }
        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElse(null); // Возвращаем null вместо исключения для проверки существования
    }

    /**
     * Находит пользователя по ID
     */
    public User findById(Long id) {
        logger.debug("Поиск пользователя по ID: {}", id);
        if (id == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Пользователь не найден с ID: {}", id);
                    return new RuntimeException("Пользователь не найден с ID: " + id);
                });
    }

    /**
     * Возвращает список всех пользователей
     */
    public List<User> findAllUsers() {
        logger.debug("Получение списка всех пользователей");
        return userRepository.findAll();
    }

    /**
     * Возвращает список отключенных пользователей
     */
    public List<User> findDisabledUsers() {
        logger.debug("Получение списка отключенных пользователей");
        return userRepository.findByEnabledFalse();
    }

    /**
     * Находит пользователей по роли
     */
    public List<User> findUsersByRole(Role role) {
        logger.debug("Поиск пользователей по роли: {}", role.getName());
        if (role == null) {
            throw new IllegalArgumentException("Роль не может быть null");
        }
        return userRepository.findByRoles(role);
    }

    /**
     * Обновляет данные пользователя
     */
    @Transactional
    public User updateUser(User user) {
        logger.info("Обновление пользователя с ID: {}", user.getId());
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Пользователь или ID не может быть null");
        }

        // Проверяем существование пользователя
        if (!userRepository.existsById(user.getId())) {
            throw new RuntimeException("Пользователь не найден с ID: " + user.getId());
        }

        return userRepository.save(user);
    }

    /**
     * Удаляет пользователя
     */
    @Transactional
    public void deleteUser(Long userId) {
        logger.info("Удаление пользователя с ID: {}", userId);
        if (userId == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        // Проверяем существование пользователя
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Пользователь не найден с ID: " + userId);
        }

        userRepository.deleteById(userId);
    }

    /**
     * Отключает пользователя
     */
    @Transactional
    public User disableUser(Long userId) {
        logger.info("Отключение пользователя с ID: {}", userId);
        User user = findById(userId);
        user.setEnabled(false);
        return userRepository.save(user);
    }

    /**
     * Включает пользователя
     */
    @Transactional
    public User enableUser(Long userId) {
        logger.info("Включение пользователя с ID: {}", userId);
        User user = findById(userId);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    /**
     * Повышает пользователя до администратора
     */
    @Transactional
    public User promoteToAdmin(Long userId) {
        logger.info("Повышение пользователя до администратора, ID: {}", userId);
        User user = findById(userId);

        // Добавляем роль ADMIN
        Role adminRole = roleRepository.findByName("ROLE_ADMIN");
        if (adminRole == null) {
            logger.info("Создание роли ROLE_ADMIN");
            adminRole = new Role("ROLE_ADMIN");
            roleRepository.save(adminRole);
        }

        // Проверяем, есть ли уже роль администратора
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            logger.info("Пользователь уже имеет роль администратора, ID: {}", userId);
            return user;
        }

        user.addRole(adminRole);

        User savedUser = userRepository.save(user);
        logger.info("Пользователь успешно повышен до администратора, ID: {}", userId);
        return savedUser;
    }

    /**
     * Создает администратора
     */
    @Transactional
    public User createAdminUser(String email, String password, String firstName, String lastName, String phone) {
        logger.info("Создание администратора с email: {}", email);

        // Нормализация и валидация email
        email = email.toLowerCase().trim();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Некорректный формат email");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        // Валидация телефона
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("Телефон должен быть в формате +7(XXX)XXX-XX-XX");
        }

        if (userRepository.existsByPhone(phone)) {
            throw new RuntimeException("Пользователь с таким телефоном уже существует");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setPhone(phone);
        user.setEnabled(true);
        user.setRegistrationDate(LocalDateTime.now());
        user.setLastLoginDate(LocalDateTime.now());

        // Добавляем роль USER
        Role userRole = roleRepository.findByName("ROLE_USER");
        if (userRole == null) {
            userRole = new Role("ROLE_USER");
            roleRepository.save(userRole);
        }
        user.addRole(userRole);
        // Добавляем роль ADMIN
        Role adminRole = roleRepository.findByName("ROLE_ADMIN");
        if (adminRole == null) {
            adminRole = new Role("ROLE_ADMIN");
            roleRepository.save(adminRole);
        }
        user.addRole(adminRole);

        User savedUser = userRepository.save(user);
        logger.info("Администратор успешно создан: {}", email);
        return savedUser;
    }

    /**
     * Обновляет профиль пользователя с валидацией телефона
     */
    @Transactional
    public User updateUserProfile(Long userId, String firstName, String lastName, String phone) {
        logger.info("Обновление профиля пользователя с ID: {}", userId);
        User user = findById(userId);

        // Валидация имени
        if (firstName == null || firstName.trim().length() < 2) {
            logger.warn("Попытка обновления профиля с некорректным именем: {}", firstName);
            throw new IllegalArgumentException("Имя должно содержать не менее 2 символов");
        }
        user.setFirstName(firstName.trim());

        // Валидация фамилии
        if (lastName == null || lastName.trim().length() < 2) {
            logger.warn("Попытка обновления профиля с некорректной фамилией: {}", lastName);
            throw new IllegalArgumentException("Фамилия должна содержать не менее 2 символов");
        }
        user.setLastName(lastName.trim());

        // Валидация телефона
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            logger.warn("Попытка обновления профиля с некорректным телефоном: {}", phone);
            throw new IllegalArgumentException("Телефон должен быть в формате +7(XXX)XXX-XX-XX");
        }

        // Проверяем, не занят ли телефон другим пользователем
        if (!phone.equals(user.getPhone())) {
            if (userRepository.existsByPhone(phone)) {
                logger.warn("Попытка обновления профиля с существующим телефоном: {}", phone);
                throw new RuntimeException("Пользователь с таким телефоном уже существует");
            }
            user.setPhone(phone);
        }

        User savedUser = userRepository.save(user);
        logger.info("Профиль пользователя успешно обновлен, ID: {}", userId);
        return savedUser;
    }

    /**
     * Изменяет пароль пользователя с проверкой текущего пароля
     */
    @Transactional
    public User changePassword(Long userId, String currentPassword, String newPassword) {
        logger.info("Изменение пароля пользователя с ID: {}", userId);
        User user = findById(userId);

        // Проверяем текущий пароль
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            logger.warn("Неверный текущий пароль при попытке изменения пароля, ID: {}", userId);
            throw new RuntimeException("Неверный текущий пароль");
        }

        // Валидация нового пароля
        if (newPassword == null || newPassword.length() < 6) {
            logger.warn("Попытка установки слабого пароля, ID: {}", userId);
            throw new IllegalArgumentException("Пароль должен содержать не менее 6 символов");
        }

        // Проверка, что новый пароль отличается от текущего
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            logger.warn("Попытка установки того же пароля, ID: {}", userId);
            throw new RuntimeException("Новый пароль должен отличаться от текущего");
        }

        // Устанавливаем новый пароль
        user.setPassword(passwordEncoder.encode(newPassword));

        User savedUser = userRepository.save(user);
        logger.info("Пароль пользователя успешно изменен, ID: {}", userId);
        return savedUser;
    }

    /**
     * Сбрасывает пароль пользователя (для администраторов или восстановления)
     */
    @Transactional
    public User resetPassword(String email, String newPassword) {
        logger.info("Сброс пароля для пользователя с email: {}", email);

        // Нормализация email
        email = email.toLowerCase().trim();

        String finalEmail = email;
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Пользователь не найден с email: {}", finalEmail);
                    return new RuntimeException("Пользователь не найден");
                });

        // Валидация нового пароля
        if (newPassword == null || newPassword.length() < 6) {
            logger.warn("Попытка сброса пароля на слабый пароль, email: {}", email);
            throw new IllegalArgumentException("Пароль должен содержать не менее 6 символов");
        }

        // Устанавливаем новый пароль
        user.setPassword(passwordEncoder.encode(newPassword));

        User savedUser = userRepository.save(user);
        logger.info("Пароль пользователя успешно сброшен, email: {}", email);
        return savedUser;
    }

    /**
     * Обновляет время последнего входа пользователя
     */
    @Transactional
    public void updateLastLoginDate(String email) {
        logger.debug("Обновление времени последнего входа для пользователя с email: {}", email);

        if (email == null || email.trim().isEmpty()) {
            logger.warn("Попытка обновления времени входа с пустым email");
            return;
        }

        // Нормализация email
        email = email.toLowerCase().trim();

        try {
            String finalEmail = email;
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        logger.warn("Пользователь не найден с email: {}", finalEmail);
                        return new RuntimeException("Пользователь не найден");
                    });

            user.setLastLoginDate(LocalDateTime.now());
            userRepository.save(user);
            logger.debug("Время последнего входа обновлено для пользователя с email: {}", email);
        } catch (Exception e) {
            // Логируем ошибку, но не прерываем выполнение программы
            logger.error("Ошибка при обновлении времени последнего входа: {}", e.getMessage(), e);
        }
    }

    /**
     * Проверяет формат телефонного номера
     */
    public boolean isValidPhoneFormat(String phone) {
        if (phone == null) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Форматирует телефонный номер в стандартный формат +7(XXX)XXX-XX-XX
     * Возвращает null, если номер невозможно отформатировать
     */
    public String formatPhoneNumber(String phone) {
        if (phone == null) {
            return null;
        }

        // Удаляем все нецифровые символы
        String digitsOnly = phone.replaceAll("\\D", "");

        // Проверяем длину (должно быть 11 цифр для российского номера)
        if (digitsOnly.length() != 11) {
            return null;
        }

        // Проверяем, что номер начинается с 7 или 8
        if (!digitsOnly.startsWith("7") && !digitsOnly.startsWith("8")) {
            return null;
        }

        // Заменяем 8 на 7 в начале, если нужно
        if (digitsOnly.startsWith("8")) {
            digitsOnly = "7" + digitsOnly.substring(1);
        }

        // Форматируем номер
        return "+" + digitsOnly.charAt(0) +
                "(" + digitsOnly.substring(1, 4) + ")" +
                digitsOnly.substring(4, 7) + "-" +
                digitsOnly.substring(7, 9) + "-" +
                digitsOnly.substring(9, 11);
    }

    /**
     * Находит пользователей по части имени, фамилии или email
     */
    public List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAllUsers();
        }

        String searchQuery = "%" + query.toLowerCase().trim() + "%";
        return userRepository.findByFirstNameLikeIgnoreCaseOrLastNameLikeIgnoreCaseOrEmailLikeIgnoreCase(
                searchQuery, searchQuery, searchQuery);
    }

    /**
     * Проверяет, является ли пользователь администратором
     */
    public boolean isAdmin(Long userId) {
        User user = findById(userId);
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
    }

    /**
     * Удаляет роль администратора у пользователя
     */
    @Transactional
    public User removeAdminRole(Long userId) {
        logger.info("Удаление роли администратора у пользователя с ID: {}", userId);
        User user = findById(userId);

        Role adminRole = roleRepository.findByName("ROLE_ADMIN");
        if (adminRole != null) {
            user.getRoles().removeIf(role -> role.getName().equals("ROLE_ADMIN"));
            User savedUser = userRepository.save(user);
            logger.info("Роль администратора успешно удалена у пользователя с ID: {}", userId);
            return savedUser;
        }

        return user;
    }

    /**
     * Получает статистику по пользователям
     */
    public UserStatistics getUserStatistics() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByEnabledTrue();
        long disabledUsers = userRepository.countByEnabledFalse();

        Role adminRole = roleRepository.findByName("ROLE_ADMIN");
        long adminCount = adminRole != null ? userRepository.countByRoles(adminRole) : 0;

        // Получаем количество новых пользователей за последние 30 дней
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long newUsers = userRepository.countByRegistrationDateAfter(thirtyDaysAgo);

        return new UserStatistics(totalUsers, activeUsers, disabledUsers, adminCount, newUsers);
    }

    /**
         * Класс для хранения статистики по пользователям
         */
        public record UserStatistics(long totalUsers, long activeUsers, long disabledUsers, long adminCount,
                                     long newUsersLast30Days) {
    }
}