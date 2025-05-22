package ru.anapa.autorent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.anapa.autorent.model.Rental;
import ru.anapa.autorent.model.User;
import ru.anapa.autorent.model.RentalStatus;
import ru.anapa.autorent.service.RentalService;
import ru.anapa.autorent.service.UserService;
import ru.anapa.autorent.model.CarStats;
import ru.anapa.autorent.model.UserStats;
import ru.anapa.autorent.service.CarService;
import org.springframework.web.servlet.ModelAndView;
import ru.anapa.autorent.model.Car;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final RentalService rentalService;
    private final CarService carService;

    @Autowired
    public AdminController(UserService userService, @Lazy RentalService rentalService, CarService carService) {
        this.userService = userService;
        this.rentalService = rentalService;
        this.carService = carService;
    }

    @GetMapping("/dashboard")
    public ModelAndView adminDashboard(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                ModelAndView mav = new ModelAndView("error");
                mav.addObject("error", "Необходимо авторизоваться для доступа к админ-панели");
                return mav;
            }

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                ModelAndView mav = new ModelAndView("error");
                mav.addObject("error", "У вас недостаточно прав для доступа к админ-панели");
                return mav;
            }

            List<User> users = userService.findAllUsers();
            List<Rental> pendingRentals = rentalService.findRentalsByStatus(RentalStatus.PENDING);
            List<Rental> activeRentals = rentalService.findRentalsByStatus(RentalStatus.ACTIVE);
            List<Rental> pendingCancellations = rentalService.findRentalsByStatus(RentalStatus.PENDING_CANCELLATION);

            ModelAndView mav = new ModelAndView("admin/dashboard");
            mav.addObject("userCount", users.size());
            mav.addObject("pendingRentals", pendingRentals);
            mav.addObject("activeRentals", activeRentals);
            mav.addObject("pendingCancellations", pendingCancellations);
            mav.addObject("carCount", carService.findAllCars().size());
            return mav;
        } catch (Exception e) {
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("error", "Ошибка при получении данных дашборда");
            return mav;
        }
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        try {
            List<User> users = userService.findAllUsers();
            model.addAttribute("users", users);
            return "admin/users";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при получении списка пользователей");
            return "error";
        }
    }

    @GetMapping("/users/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        try {
            User user = userService.findById(id);
            List<Rental> userRentals = rentalService.findRentalsByUser(user);

            model.addAttribute("user", user);
            model.addAttribute("rentals", userRentals);
            return "admin/user-details";
        } catch (Exception e) {
            model.addAttribute("error", "Пользователь не найден");
            return "error";
        }
    }

    @PostMapping("/users/{id}/disable")
    public ResponseEntity<?> disableUser(@PathVariable Long id) {
        try {
            User user = userService.findById(id);
            user.setEnabled(false);
            userService.updateUser(user);
            return ResponseEntity.ok(Map.of("message", "Пользователь успешно отключен"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при отключении пользователя"));
        }
    }

    @PostMapping("/users/{id}/enable")
    public ResponseEntity<?> enableUser(@PathVariable Long id) {
        try {
            User user = userService.findById(id);
            user.setEnabled(true);
            userService.updateUser(user);
            return ResponseEntity.ok(Map.of("message", "Пользователь успешно активирован"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при активации пользователя"));
        }
    }

    @GetMapping("/pending-cancellations")
    public String pendingCancellations(Model model) {
        List<Rental> rentals = rentalService.findRentalsByStatus(RentalStatus.PENDING_CANCELLATION);
        model.addAttribute("rentals", rentals);
        return "admin/pending-cancellations";
    }

    @GetMapping("/statistics")
    public String statistics(Model model) {
        // Получаем базовую статистику
        int userCount = userService.findAllUsers().size();
        int carCount = carService.findAllCars().size();
        double totalRevenue = rentalService.calculateTotalRevenue();
        double conversionRate = rentalService.calculateConversionRate();

        // Получаем статистику по арендам
        List<Rental> allRentals = rentalService.findAllRentals();
        Map<String, Integer> monthlyRentals = rentalService.getMonthlyRentalStats();
        Map<String, Integer> statusDistribution = rentalService.getRentalStatusDistribution();

        // Получаем популярные автомобили
        List<CarStats> popularCars = carService.getPopularCars();

        // Получаем активных пользователей
        List<UserStats> activeUsers = userService.getActiveUsers();

        // Подготавливаем данные для графиков
        Map<String, Object> rentalData = new HashMap<>();
        rentalData.put("labels", monthlyRentals.keySet().toArray());
        rentalData.put("data", monthlyRentals.values().toArray());

        Map<String, Object> statusData = new HashMap<>();
        statusData.put("labels", statusDistribution.keySet().toArray());
        statusData.put("data", statusDistribution.values().toArray());

        // Добавляем все данные в модель
        model.addAttribute("userCount", userCount);
        model.addAttribute("carCount", carCount);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("conversionRate", conversionRate);
        model.addAttribute("popularCars", popularCars);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("rentalData", rentalData);
        model.addAttribute("statusData", statusData);

        return "admin/statistics";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/synchronize-car-statuses")
    public String synchronizeCarStatuses(RedirectAttributes redirectAttributes) {
        try {
            rentalService.synchronizeAllCarStatuses();
            redirectAttributes.addFlashAttribute("success", "Статусы автомобилей успешно синхронизированы");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при синхронизации статусов: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/cars")
    public ModelAndView adminCars() {
        Page<Car> carPage = carService.findAllCarsWithPagination(0, 100);
        ModelAndView mav = new ModelAndView("admin/cars");
        mav.addObject("cars", carPage.getContent());
        return mav;
    }

    @PostMapping("/cars/{id}/delete")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteCar(@PathVariable Long id) {
        try {
            carService.deleteCar(id);
            return ResponseEntity.ok(Map.of("message", "Автомобиль успешно удален"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Ошибка при удалении автомобиля: " + e.getMessage()));
        }
    }
}