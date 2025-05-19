package ru.anapa.autorent.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.anapa.autorent.controller.AuthController;
import ru.anapa.autorent.model.Car;
import ru.anapa.autorent.model.CarImage;
import ru.anapa.autorent.model.Role;
import ru.anapa.autorent.model.User;
import ru.anapa.autorent.repository.CarRepository;
import ru.anapa.autorent.repository.RentalRepository;
import ru.anapa.autorent.repository.RoleRepository;
import ru.anapa.autorent.repository.UserRepository;
import ru.anapa.autorent.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CarRepository carRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Override
    public void run(String... args) {
        // Создаем роли, если их нет
        createRolesIfNotFound();

        // Создаем пользователей, если их нет
        createUsersIfNotFound();

//        // Создаем автомобили, если их нет
//        createCarsIfNotFound();
    }

    private void createRolesIfNotFound() {
        if (roleRepository.count() == 0) {
            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            roleRepository.save(adminRole);

            Role userRole = new Role();
            userRole.setName("ROLE_USER");
            roleRepository.save(userRole);

            logger.info("Роли созданы: ROLE_ADMIN, ROLE_USER");
        }
    }

    private void createUsersIfNotFound() {
        logger.info("DataInitializer: проверка наличия пользователей");

        // Проверяем наличие конкретного пользователя admin@example.com
        boolean adminExists = userRepository.findByEmail("admin@example.com").isPresent();
        logger.info("DataInitializer: admin@example.com существует: " + adminExists);

        if (!adminExists) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN");
            Role userRole = roleRepository.findByName("ROLE_USER");

            if (adminRole == null || userRole == null) {
                logger.info("DataInitializer: роли не найдены, создаем заново");
                createRolesIfNotFound();
                adminRole = roleRepository.findByName("ROLE_ADMIN");
                userRole = roleRepository.findByName("ROLE_USER");
            }

            // Создаем администратора
            User admin = new User();
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("Админ");
            admin.setLastName("Системы");
            admin.setPhone("+7(999)111-22-33");
            admin.setEnabled(true);
            admin.setRegistrationDate(LocalDateTime.now());
            admin.setLastLoginDate(LocalDateTime.now());
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(adminRole);
            admin.setRoles(adminRoles);
            userRepository.save(admin);
            logger.info("Создан администратор: admin@example.com / admin123, enabled: " + admin.isEnabled());

//            boolean userExists = userRepository.findByEmail("user@example.com").isPresent();
//            if (!userExists) {
//                // Создаем обычного пользователя
//                User user = new User();
//                user.setEmail("user@example.com");
//                user.setPassword(passwordEncoder.encode("user123"));
//                user.setFirstName("Иван");
//                user.setLastName("Иванов");
//                user.setPhone("+7(999)444-55-66");
//                user.setEnabled(true);
//                user.setRegistrationDate(LocalDateTime.now());
//                user.setLastLoginDate(LocalDateTime.now());
//                Set<Role> userRoles = new HashSet<>();
//                userRoles.add(userRole);
//                user.setRoles(userRoles);
//                userRepository.save(user);
//                logger.info("Создан пользователь: user@example.com / user123");
//
//                // Создаем еще одного пользователя
//                User user2 = new User();
//                user2.setEmail("maria@example.com");
//                user2.setPassword(passwordEncoder.encode("maria123"));
//                user2.setFirstName("Мария");
//                user2.setLastName("Петрова");
//                user2.setPhone("+7(999)777-88-99");
//                user2.setEnabled(true);
//                user2.setRegistrationDate(LocalDateTime.now());
//                user2.setLastLoginDate(LocalDateTime.now());
//                Set<Role> user2Roles = new HashSet<>();
//                user2Roles.add(userRole);
//                user2.setRoles(user2Roles);
//                userRepository.save(user2);
//                logger.info("Создан пользователь: maria@example.com / maria123");
//
//                // Создаем третьего пользователя
//                User user3 = new User();
//                user3.setEmail("alex@example.com");
//                user3.setPassword(passwordEncoder.encode("alex123"));
//                user3.setFirstName("Александр");
//                user3.setLastName("Смирнов");
//                user3.setPhone("+7(999)333-44-55");
//                user3.setEnabled(true);
//                user3.setRegistrationDate(LocalDateTime.now());
//                user3.setLastLoginDate(LocalDateTime.now());
//                Set<Role> user3Roles = new HashSet<>();
//                user3Roles.add(userRole);
//                user3.setRoles(user3Roles);
//                userRepository.save(user3);
//                logger.info("Создан пользователь: alex@example.com / alex123");
//            }
//        }
//    }
//
//    private void createCarsIfNotFound() {
//        if (carRepository.count() == 0) {
//            // Создаем несколько автомобилей
//            Car car1 = Car.builder()
//                    .brand("Toyota")
//                    .model("Camry")
//                    .year(2022)
//                    .licensePlate("А123БВ777")
//                    .dailyRate(new BigDecimal("3500"))
//                    .description("Комфортный седан бизнес-класса с автоматической коробкой передач, кожаным салоном и климат-контролем.")
//                    .available(true)
//                    .color("Белый")
//                    .transmission("Автомат")
//                    .fuelType("Бензин")
//                    .seats(5)
//                    .category("Бизнес")
//                    .build();
//
//            CarImage car1Image = CarImage.builder()
//                    .car(car1)
//                    .imageUrl("/images/cars/toyota-camry.jpg")
//                    .description("Вид спереди")
//                    .displayOrder(0)
//                    .main(true)
//                    .build();
//            car1.getImages().add(car1Image);
//            carRepository.save(car1);
//
//            Car car2 = Car.builder()
//                    .brand("Volkswagen")
//                    .model("Polo")
//                    .year(2021)
//                    .licensePlate("В456ГД777")
//                    .dailyRate(new BigDecimal("2000"))
//                    .description("Экономичный седан с механической коробкой передач, кондиционером и подогревом сидений.")
//                    .available(true)
//                    .color("Серый")
//                    .transmission("Механика")
//                    .fuelType("Бензин")
//                    .seats(5)
//                    .category("Эконом")
//                    .build();
//
//            CarImage car2Image = CarImage.builder()
//                    .car(car2)
//                    .imageUrl("/images/cars/volkswagen-polo.jpg")
//                    .description("Вид спереди")
//                    .displayOrder(0)
//                    .main(true)
//                    .build();
//            car2.getImages().add(car2Image);
//            carRepository.save(car2);
//
//            Car car3 = Car.builder()
//                    .brand("BMW")
//                    .model("X5")
//                    .year(2023)
//                    .licensePlate("Е789ЖЗ777")
//                    .dailyRate(new BigDecimal("7000"))
//                    .description("Премиальный внедорожник с полным приводом, кожаным салоном, панорамной крышей и всеми современными системами безопасности.")
//                    .available(true)
//                    .color("Черный")
//                    .transmission("Автомат")
//                    .fuelType("Дизель")
//                    .seats(5)
//                    .category("Премиум")
//                    .build();
//
//            CarImage car3Image = CarImage.builder()
//                    .car(car3)
//                    .imageUrl("/images/cars/bmw-x5.jpg")
//                    .description("Вид спереди")
//                    .displayOrder(0)
//                    .main(true)
//                    .build();
//            car3.getImages().add(car3Image);
//            carRepository.save(car3);
//
//            Car car4 = Car.builder()
//                    .brand("Kia")
//                    .model("Rio")
//                    .year(2020)
//                    .licensePlate("И012КЛ777")
//                    .dailyRate(new BigDecimal("1800"))
//                    .description("Компактный городской автомобиль с экономичным расходом топлива, кондиционером и аудиосистемой.")
//                    .available(true)
//                    .color("Красный")
//                    .transmission("Автомат")
//                    .fuelType("Бензин")
//                    .seats(5)
//                    .category("Эконом")
//                    .build();
//
//            CarImage car4Image = CarImage.builder()
//                    .car(car4)
//                    .imageUrl("/images/cars/kia-rio.jpg")
//                    .description("Вид спереди")
//                    .displayOrder(0)
//                    .main(true)
//                    .build();
//            car4.getImages().add(car4Image);
//            carRepository.save(car4);
//
//            Car car5 = Car.builder()
//                    .brand("Mercedes-Benz")
//                    .model("E-Class")
//                    .year(2022)
//                    .licensePlate("М345НО777")
//                    .dailyRate(new BigDecimal("6000"))
//                    .description("Представительский седан с роскошным салоном, панорамной крышей и передовыми технологиями.")
//                    .available(true)
//                    .color("Серебристый")
//                    .transmission("Автомат")
//                    .fuelType("Бензин")
//                    .seats(5)
//                    .category("Премиум")
//                    .build();
//
//            CarImage car5Image = CarImage.builder()
//                    .car(car5)
//                    .imageUrl("/images/cars/mercedes-e.jpg")
//                    .description("Вид спереди")
//                    .displayOrder(0)
//                    .main(true)
//                    .build();
//            car5.getImages().add(car5Image);
//            carRepository.save(car5);
//
//            Car car6 = Car.builder()
//                    .brand("Hyundai")
//                    .model("Solaris")
//                    .year(2021)
//                    .licensePlate("П678РС777")
//                    .dailyRate(new BigDecimal("1900"))
//                    .description("Надежный и экономичный седан для городских поездок с кондиционером и мультимедийной системой.")
//                    .available(true)
//                    .color("Синий")
//                    .transmission("Автомат")
//                    .fuelType("Бензин")
//                    .seats(5)
//                    .category("Эконом")
//                    .build();
//
//            CarImage car6Image = CarImage.builder()
//                    .car(car6)
//                    .imageUrl("/images/cars/hyundai-solaris.jpg")
//                    .description("Вид спереди")
//                    .displayOrder(0)
//                    .main(true)
//                    .build();
//            car6.getImages().add(car6Image);
//            carRepository.save(car6);
//
//            Car car7 = Car.builder()
//                    .brand("Audi")
//                    .model("Q7")
//                    .year(2023)
//                    .licensePlate("Т901УФ777")
//                    .dailyRate(new BigDecimal("8000"))
//                    .description("Роскошный полноразмерный внедорожник с полным приводом, кожаным салоном и передовыми технологиями.")
//                    .available(true)
//                    .color("Черный")
//                    .transmission("Автомат")
//                    .fuelType("Дизель")
//                    .seats(7)
//                    .category("Премиум")
//                    .build();
//
//            CarImage car7Image = CarImage.builder()
//                    .car(car7)
//                    .imageUrl("/images/cars/audi-q7.jpg")
//                    .description("Вид спереди")
//                    .displayOrder(0)
//                    .main(true)
//                    .build();
//            car7.getImages().add(car7Image);
//            carRepository.save(car7);
//
//            Car car8 = Car.builder()
//                    .brand("Skoda")
//                    .model("Octavia")
//                    .year(2021)
//                    .licensePlate("Х234ЦЧ777")
//                    .dailyRate(new BigDecimal("2800"))
//                    .description("Просторный и комфортный лифтбек с большим багажником, климат-контролем и современными системами безопасности.")
//                    .available(true)
//                    .color("Белый")
//                    .transmission("Автомат")
//                    .fuelType("Бензин")
//                    .seats(5)
//                    .category("Комфорт")
//                    .build();
//
//            CarImage car8Image = CarImage.builder()
//                    .car(car8)
//                    .imageUrl("/images/cars/skoda-octavia.jpg")
//                    .description("Вид спереди")
//                    .displayOrder(0)
//                    .main(true)
//                    .build();
//            car8.getImages().add(car8Image);
//            carRepository.save(car8);
//
//            logger.info("Создано 8 автомобилей для проката");
        }
    }
}