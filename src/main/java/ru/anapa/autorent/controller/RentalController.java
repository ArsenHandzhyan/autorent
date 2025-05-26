package ru.anapa.autorent.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.anapa.autorent.dto.RentalDto;
import ru.anapa.autorent.dto.RentalPeriodDto;
import ru.anapa.autorent.model.Car;
import ru.anapa.autorent.model.Rental;
import ru.anapa.autorent.model.User;
import ru.anapa.autorent.model.Account;
import ru.anapa.autorent.model.Transaction;
import ru.anapa.autorent.service.AccountService;
import ru.anapa.autorent.service.CarService;
import ru.anapa.autorent.service.RentalService;
import ru.anapa.autorent.service.UserService;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/rentals")
public class RentalController {

    private final RentalService rentalService;
    private final UserService userService;
    private final CarService carService;
    private final AccountService accountService;

    @Autowired
    public RentalController(RentalService rentalService, UserService userService, CarService carService, AccountService accountService) {
        this.rentalService = rentalService;
        this.userService = userService;
        this.carService = carService;
        this.accountService = accountService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public String myRentals(Model model) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.findByEmail(authentication.getName());
            List<Rental> rentals = rentalService.findRentalsByUser(user);

            List<Rental> activeRentals = rentals.stream()
                    .filter(rental ->
                            "ACTIVE".equals(rental.getStatus()) ||
                                    "PENDING".equals(rental.getStatus()) ||
                                    "PENDING_CANCELLATION".equals(rental.getStatus()))
                    .collect(Collectors.toList());

            List<Rental> historyRentals = rentals.stream()
                    .filter(rental ->
                            "COMPLETED".equals(rental.getStatus()) ||
                                    "CANCELLED".equals(rental.getStatus()))
                    .collect(Collectors.toList());

            // Получаем данные о счете пользователя
            Account account = accountService.getAccountByUserId(user.getId());
            List<Transaction> transactions = accountService.getAccountTransactions(user.getId());

            model.addAttribute("rentals", rentals);
            model.addAttribute("activeRentals", activeRentals);
            model.addAttribute("historyRentals", historyRentals);
            model.addAttribute("account", account);
            model.addAttribute("transactions", transactions);

            return "rentals/my-rentals";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при получении данных");
            return "error";
        }
    }

    @GetMapping("/new/{carId}")
    @PreAuthorize("isAuthenticated()")
    public String showRentalForm(@PathVariable Long carId,
                                 @RequestParam(required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                 Model model) {
        Car car = carService.findCarById(carId);

        RentalDto rentalDto = new RentalDto();

        // Если передана начальная дата (для бронирования занятого авто)
        if (startDate != null) {
            rentalDto.setStartDate(startDate);
            // Устанавливаем конечную дату на следующий день после начальной
            rentalDto.setEndDate(startDate.plusDays(1));
        } else {
            // Иначе устанавливаем текущую дату и время
            LocalDateTime now = LocalDateTime.now();
            rentalDto.setStartDate(now);
            rentalDto.setEndDate(now.plusDays(1));
        }

        model.addAttribute("rental", rentalDto);
        model.addAttribute("car", car);

        // Добавляем информацию о забронированных периодах
        List<RentalPeriodDto> bookedPeriods = carService.getBookedPeriods(carId);
        model.addAttribute("bookedPeriods", bookedPeriods);

        return "rentals/new";
    }

    @PostMapping("/new/{carId}")
    @PreAuthorize("isAuthenticated()")
    public String createRental(@PathVariable Long carId,
                               @ModelAttribute RentalDto rentalDto,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        try {
            LocalDateTime now = LocalDateTime.now();

            // Валидация дат
            if (rentalDto.getStartDate() == null || rentalDto.getEndDate() == null) {
                redirectAttributes.addFlashAttribute("error", "Даты начала и окончания аренды обязательны");
                return "redirect:/rentals/new/" + carId;
            }

            // Проверяем, что дата начала не в прошлом (с небольшим запасом в 5 минут)
            if (rentalDto.getStartDate().isBefore(now.minusMinutes(5))) {
                redirectAttributes.addFlashAttribute("error", "Дата начала аренды должна быть в настоящем или будущем");
                return "redirect:/rentals/new/" + carId;
            }

            if (rentalDto.getEndDate().isBefore(rentalDto.getStartDate().plusHours(1))) {
                redirectAttributes.addFlashAttribute("error", "Дата окончания аренды должна быть как минимум на 1 час позже даты начала");
                return "redirect:/rentals/new/" + carId;
            }

            User user = userService.findByEmail(userDetails.getUsername());

            // Создаем аренду с указанными датами
            Rental rental = rentalService.createRental(
                    user,
                    carId,
                    rentalDto.getStartDate(),
                    rentalDto.getEndDate()
            );

            redirectAttributes.addFlashAttribute("success",
                    "Заявка на аренду успешно создана! Ожидайте подтверждения.");
            return "redirect:/rentals";
        } catch (Exception e) {
            // Логирование ошибки
            System.err.println("Ошибка при создании аренды: " + e.getMessage());
            e.printStackTrace();

            redirectAttributes.addFlashAttribute("error", "Произошла ошибка: " + e.getMessage());
            return "redirect:/rentals/new/" + carId;
        }
    }

    // Этот метод перенаправляет на административный контроллер
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all-rentals")
    public String allRentals() {
        return "redirect:/admin/rentals";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/cancel/{id}")
    public String showCancelRentalForm(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(authentication.getName());
        Rental rental = rentalService.findById(id);

        // Проверяем, принадлежит ли аренда текущему пользователю или пользователь админ
        if (!rental.getUser().getId().equals(user.getId()) &&
                !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/rentals";
        }

        // Проверяем, можно ли отменить аренду
        if (rental.getStatus().equals("COMPLETED") || rental.getStatus().equals("CANCELLED")) {
            return "redirect:/rentals";
        }

        model.addAttribute("rental", rental);
        return "rentals/cancel";
    }

    // Метод для запроса отмены аренды пользователем
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cancel/{id}")
    public ResponseEntity<?> requestCancellation(@PathVariable Long id,
                                               @RequestParam(required = false) String cancelReason) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.findByEmail(authentication.getName());
            Rental rental = rentalService.findById(id);

            if (!rental.getUser().getId().equals(user.getId()) &&
                    !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "У вас нет прав для отмены этой аренды"));
            }

            rentalService.requestCancellation(id, cancelReason);
            return ResponseEntity.ok(Map.of("message", "Запрос на отмену аренды отправлен"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при отмене аренды"));
        }
    }

    // Вместо этого можно добавить методы, которые перенаправляют на административный контроллер
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<?> redirectToAdminRentals() {
        return ResponseEntity.ok(Map.of("redirect", "/admin/rentals"));
    }

    @GetMapping("/{id}")
    public String getRentalDetails(@PathVariable Long id, Model model, Principal principal) {
        // Получаем текущего пользователя
        String email = principal.getName();

        // Получаем аренду по ID
        Rental rental = rentalService.getRentalById(id);

        // Проверяем, принадлежит ли аренда текущему пользователю
        if (!rental.getUser().getEmail().equals(email)) {
            return "redirect:/rentals?error=Доступ запрещен";
        }
        Car car = carService.findCarById(id);
        model.addAttribute("car", car);
        model.addAttribute("rental", rental);
        return "rentals/rental-details"; // Имя шаблона для отображения деталей аренды
    }
}