package ru.anapa.autorent.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.anapa.autorent.dto.RentalDto;
import ru.anapa.autorent.dto.RentalPeriodDto;
import ru.anapa.autorent.model.*;
import ru.anapa.autorent.service.AccountService;
import ru.anapa.autorent.service.CarService;
import ru.anapa.autorent.service.DailyPaymentService;
import ru.anapa.autorent.service.RentalService;
import ru.anapa.autorent.service.UserService;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/rentals")
public class RentalController {

    private static final Logger logger = LoggerFactory.getLogger(RentalController.class);

    private final RentalService rentalService;
    private final UserService userService;
    private final CarService carService;
    private final AccountService accountService;
    private final DailyPaymentService dailyPaymentService;

    @Autowired
    public RentalController(@Lazy RentalService rentalService, 
                          @Lazy UserService userService, 
                          @Lazy CarService carService, 
                          @Lazy AccountService accountService,
                          @Lazy DailyPaymentService dailyPaymentService) {
        this.rentalService = rentalService;
        this.userService = userService;
        this.carService = carService;
        this.accountService = accountService;
        this.dailyPaymentService = dailyPaymentService;
        logger.info("RentalController initialized");
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public String myRentals(Model model) {
        return myRentalsPage(model);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my-rentals")
    public String myRentalsPage(Model model) {
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
            logger.error("Ошибка при получении данных", e);
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
        if (car == null) {
            return "redirect:/cars";
        }

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
                               @Valid @ModelAttribute("rental") RentalDto rentalDto,
                               BindingResult bindingResult,
                               Model model,
                               Authentication authentication,
                               HttpServletRequest request) {
        logger.info("Создание новой аренды для автомобиля с ID: {}", carId);

        Car car = carService.findCarById(carId);
        if (car == null) {
            model.addAttribute("error", "Автомобиль не найден");
            return "error";
        }

        // Добавляем car и bookedPeriods в модель для корректного отображения формы при ошибках
        model.addAttribute("car", car);
        List<RentalPeriodDto> bookedPeriods = carService.getBookedPeriods(carId);
        model.addAttribute("bookedPeriods", bookedPeriods != null ? bookedPeriods : new ArrayList<>());

        if (bindingResult.hasErrors()) {
            logger.warn("Ошибки валидации при создании аренды: {}", bindingResult.getAllErrors());
            return "rentals/new";
        }

        try {
            User user = userService.findByEmail(authentication.getName());

            if (!rentalService.isCarAvailableForPeriod(carId, rentalDto.getStartDate(), rentalDto.getEndDate())) {
                logger.warn("Автомобиль с ID {} недоступен в указанный период", carId);
                bindingResult.rejectValue("startDate", "unavailable", "Автомобиль недоступен в указанный период");
                return "rentals/new";
            }

            // Создаем аренду
            Rental rental = new Rental();
            rental.setUser(user);
            rental.setCar(car);
            rental.setStartDate(rentalDto.getStartDate());
            rental.setEndDate(rentalDto.getEndDate());
            rental.setNotes(rentalDto.getNotes());
            rental.setStatus(RentalStatus.PENDING);

            // Рассчитываем стоимость
            BigDecimal totalCost = car.getPricePerDay().multiply(
                    BigDecimal.valueOf(rentalDto.getDurationDays())
            );
            rental.setTotalCost(totalCost);

            rentalService.createRental(rental);
            logger.info("Аренда успешно создана с ID: {}", rental.getId());

            return "redirect:/rentals";
        } catch (Exception e) {
            logger.error("Ошибка при создании аренды", e);
            model.addAttribute("error", "Произошла ошибка при создании аренды");
            return "rentals/new";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all-rentals")
    public String allRentals() {
        return "redirect:/admin/rentals";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/cancel/{id}")
    public String showCancelRentalForm(@PathVariable Long id, Model model) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.findByEmail(authentication.getName());
            Rental rental = rentalService.findById(id);

            if (rental == null || !rental.getUser().getId().equals(user.getId())) {
                return "redirect:/rentals";
            }

            model.addAttribute("rental", rental);
            return "rentals/cancel";
        } catch (Exception e) {
            logger.error("Ошибка при отображении формы отмены аренды", e);
            return "redirect:/rentals";
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cancel/{id}")
    public ResponseEntity<?> requestCancellation(@PathVariable Long id,
                                                 @RequestParam(required = false) String cancelReason) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.findByEmail(authentication.getName());
            Rental rental = rentalService.findById(id);

            if (rental == null || !rental.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Доступ запрещен");
            }

            rentalService.requestCancellation(id, cancelReason);
            return ResponseEntity.ok().body(Map.of("success", true));
        } catch (Exception e) {
            logger.error("Ошибка при запросе отмены аренды", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Ошибка при запросе отмены аренды"));
        }
    }

    @GetMapping("/{id}")
    public String getRentalDetails(@PathVariable Long id, Model model, Principal principal) {
        try {
            Rental rental = rentalService.findById(id);
            if (rental == null) {
                model.addAttribute("error", "Аренда не найдена");
                return "error";
            }

            // Проверка прав доступа
            User currentUser = userService.findByEmail(principal.getName());
            boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
            if (!isAdmin && !rental.getUser().getId().equals(currentUser.getId())) {
                model.addAttribute("error", "У вас нет прав для просмотра этой аренды");
                return "error";
            }

            List<DailyPayment> payments = dailyPaymentService.getPaymentsByRental(rental);

            model.addAttribute("rental", rental);
            model.addAttribute("payments", payments);
            return "rentals/rental-details";
        } catch (Exception e) {
            logger.error("Ошибка при получении деталей аренды с ID {}: {}", id, e.getMessage(), e);
            model.addAttribute("error", "Не удалось загрузить детали аренды");
            return "error";
        }
    }
}