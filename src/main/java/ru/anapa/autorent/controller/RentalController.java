package ru.anapa.autorent.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import ru.anapa.autorent.model.*;
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
import java.math.BigDecimal;
import java.util.ArrayList;

@Controller
@RequestMapping("/rentals")
public class RentalController {

    private static final Logger logger = LoggerFactory.getLogger(RentalController.class);

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
            
            logger.info("Found {} rentals for user {}", rentals.size(), user.getEmail());
            rentals.forEach(rental -> logger.info("Rental ID: {}, Status: {}", rental.getId(), rental.getStatus()));

            List<Rental> pendingRentals = rentals.stream()
                    .filter(rental -> rental.getStatus() == RentalStatus.PENDING)
                    .collect(Collectors.toList());
            
            logger.info("Found {} pending rentals", pendingRentals.size());
            pendingRentals.forEach(rental -> logger.info("Pending Rental ID: {}, Status: {}", rental.getId(), rental.getStatus()));

            List<Rental> activeRentals = rentals.stream()
                    .filter(rental ->
                            rental.getStatus() == RentalStatus.ACTIVE ||
                            rental.getStatus() == RentalStatus.PENDING_CANCELLATION)
                    .collect(Collectors.toList());
            
            logger.info("Found {} active rentals", activeRentals.size());
            activeRentals.forEach(rental -> logger.info("Active Rental ID: {}, Status: {}", rental.getId(), rental.getStatus()));

            List<Rental> historyRentals = rentals.stream()
                    .filter(rental ->
                            rental.getStatus() == RentalStatus.COMPLETED ||
                            rental.getStatus() == RentalStatus.CANCELLED)
                    .collect(Collectors.toList());
            
            logger.info("Found {} history rentals", historyRentals.size());
            historyRentals.forEach(rental -> logger.info("History Rental ID: {}, Status: {}", rental.getId(), rental.getStatus()));

            // Получаем данные о счете пользователя
            Account account = accountService.getAccountByUserId(user.getId());
            List<Transaction> transactions = accountService.getAccountTransactions(user.getId());
            List<AccountHistory> accountHistory = accountService.getAccountHistory(account.getId());

            model.addAttribute("rentals", rentals);
            model.addAttribute("pendingRentals", pendingRentals);
            model.addAttribute("activeRentals", activeRentals);
            model.addAttribute("historyRentals", historyRentals);
            model.addAttribute("account", account);
            model.addAttribute("transactions", transactions);
            model.addAttribute("accountHistory", accountHistory);

            return "rentals/my-rentals";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при получении данных");
            return "error";
        }
    }

    @GetMapping("/new")
    @PreAuthorize("isAuthenticated()")
    public String showRentalForm(@RequestParam Long carId,
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
            rentalDto.setDurationDays(1);
        } else {
            // Иначе устанавливаем текущую дату и время
            LocalDateTime now = LocalDateTime.now();
            rentalDto.setStartDate(now);
            rentalDto.setEndDate(now.plusDays(1));
            rentalDto.setDurationDays(1);
        }

        model.addAttribute("rental", rentalDto);
        model.addAttribute("car", car);

        // Добавляем информацию о забронированных периодах
        List<RentalPeriodDto> bookedPeriods = carService.getBookedPeriods(carId);
        model.addAttribute("bookedPeriods", bookedPeriods != null ? bookedPeriods : new ArrayList<>());

        return "rentals/new";
    }

    @PostMapping("/new")
    @PreAuthorize("isAuthenticated()")
    public String createRental(@RequestParam Long carId,
                             @ModelAttribute RentalDto rentalDto,
                             BindingResult result,
                             Model model,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        logger.info("Creating rental for carId: {}, rentalDto: {}", carId, rentalDto);
        
        if (result.hasErrors()) {
            logger.error("Validation errors: {}", result.getAllErrors());
            Car car = carService.findCarById(carId);
            model.addAttribute("car", car);
            model.addAttribute("rental", rentalDto);
            return "rentals/new";
        }

        try {
            User user = userService.getCurrentUser(authentication);
            Car car = carService.findCarById(carId);
            
            // Проверяем ограничения счета
            BigDecimal totalAmount = car.getPricePerDay().multiply(BigDecimal.valueOf(rentalDto.getDurationDays()));
            accountService.validateRentalConstraints(user.getId(), totalAmount, rentalDto.getDurationDays());
            
            // Проверяем доступность автомобиля на выбранные даты
            if (!rentalService.isCarAvailableForPeriod(carId, rentalDto.getStartDate(), rentalDto.getEndDate())) {
                logger.warn("Car {} is not available for the selected period", carId);
                model.addAttribute("error", "Автомобиль недоступен на выбранные даты");
                model.addAttribute("car", car);
                model.addAttribute("rental", rentalDto);
                return "rentals/new";
            }
            
            // Создаем аренду
            Rental rental = new Rental();
            rental.setUser(user);
            rental.setCar(car);
            rental.setStartDate(rentalDto.getStartDate());
            rental.setEndDate(rentalDto.getEndDate());
            rental.setTotalCost(totalAmount);
            rental.setStatus(RentalStatus.PENDING);
            rental.setNotes(rentalDto.getNotes());
            rental.setCreatedAt(LocalDateTime.now());
            rental.setUpdatedAt(LocalDateTime.now());
            
            rentalService.createRental(rental);
            logger.info("Rental created successfully: {}", rental.getId());
            
            redirectAttributes.addFlashAttribute("success", "Заявка на аренду успешно создана");
            return "redirect:/rentals";
        } catch (Exception e) {
            logger.error("Error creating rental", e);
            model.addAttribute("error", e.getMessage());
            Car car = carService.findCarById(carId);
            model.addAttribute("car", car);
            model.addAttribute("rental", rentalDto);
            return "rentals/new";
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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/admin-cancel")
    public String adminCancelRental(@PathVariable Long id, 
                                  @RequestParam(required = false) String cancelReason,
                                  RedirectAttributes redirectAttributes) {
        try {
            rentalService.cancelRental(id, cancelReason);
            redirectAttributes.addFlashAttribute("success", "Аренда успешно отменена");
            return "redirect:/rentals";
        } catch (Exception e) {
            logger.error("Error cancelling rental: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Ошибка при отмене аренды: " + e.getMessage());
            return "redirect:/rentals";
        }
    }
}