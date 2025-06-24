package ru.anapa.autorent.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.anapa.autorent.model.PasswordResetLog;
import ru.anapa.autorent.repository.PasswordResetLogRepository;
import ru.anapa.autorent.service.PasswordResetService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPasswordResetController {

    private static final Logger logger = LoggerFactory.getLogger(AdminPasswordResetController.class);

    private final PasswordResetService passwordResetService;
    private final PasswordResetLogRepository passwordResetLogRepository;

    @Autowired
    public AdminPasswordResetController(PasswordResetService passwordResetService,
                                       PasswordResetLogRepository passwordResetLogRepository) {
        this.passwordResetService = passwordResetService;
        this.passwordResetLogRepository = passwordResetLogRepository;
    }

    /**
     * Страница статистики восстановления паролей
     */
    @GetMapping("/password-reset-stats")
    public String showPasswordResetStats(Model model) {
        logger.info("Запрос страницы статистики восстановления паролей");

        try {
            // Получаем статистику за последние 24 часа
            Map<String, Object> statistics = passwordResetService.getPasswordResetStatistics();
            model.addAttribute("statistics", statistics);

            // Получаем последние логи
            List<PasswordResetLog> recentLogs = passwordResetLogRepository
                    .findTop50ByOrderByCreatedAtDesc();
            model.addAttribute("recentLogs", recentLogs);

            // Получаем статистику по типам действий
            @SuppressWarnings("unchecked")
            Map<PasswordResetLog.ActionType, Long> actionTypeStats = 
                    (Map<PasswordResetLog.ActionType, Long>) statistics.get("actionTypeStats");
            model.addAttribute("actionTypeStats", actionTypeStats);

            logger.info("Статистика восстановления паролей загружена");
            return "admin/password-reset-stats";

        } catch (Exception e) {
            logger.error("Ошибка при загрузке статистики восстановления паролей", e);
            model.addAttribute("error", "Ошибка при загрузке статистики: " + e.getMessage());
            return "admin/password-reset-stats";
        }
    }

    /**
     * Страница логов восстановления паролей
     */
    @GetMapping("/password-reset-logs")
    public String showPasswordResetLogs(@RequestParam(value = "email", required = false) String email,
                                       @RequestParam(value = "actionType", required = false) String actionType,
                                       @RequestParam(value = "status", required = false) String status,
                                       Model model) {
        logger.info("Запрос страницы логов восстановления паролей");

        try {
            List<PasswordResetLog> logs;

            if (email != null && !email.trim().isEmpty()) {
                // Фильтрация по email
                logs = passwordResetLogRepository.findByEmailOrderByCreatedAtDesc(email.trim());
                model.addAttribute("filterEmail", email);
            } else if (actionType != null && !actionType.trim().isEmpty()) {
                // Фильтрация по типу действия
                try {
                    PasswordResetLog.ActionType type = PasswordResetLog.ActionType.valueOf(actionType.toUpperCase());
                    logs = passwordResetLogRepository.findByActionTypeOrderByCreatedAtDesc(type);
                    model.addAttribute("filterActionType", actionType);
                } catch (IllegalArgumentException e) {
                    logs = passwordResetLogRepository.findTop100ByOrderByCreatedAtDesc();
                    model.addAttribute("error", "Неверный тип действия: " + actionType);
                }
            } else if (status != null && !status.trim().isEmpty()) {
                // Фильтрация по статусу
                try {
                    PasswordResetLog.Status statusEnum = PasswordResetLog.Status.valueOf(status.toUpperCase());
                    logs = passwordResetLogRepository.findByStatusOrderByCreatedAtDesc(statusEnum);
                    model.addAttribute("filterStatus", status);
                } catch (IllegalArgumentException e) {
                    logs = passwordResetLogRepository.findTop100ByOrderByCreatedAtDesc();
                    model.addAttribute("error", "Неверный статус: " + status);
                }
            } else {
                // Показать последние 100 записей
                logs = passwordResetLogRepository.findTop100ByOrderByCreatedAtDesc();
            }

            model.addAttribute("logs", logs);
            model.addAttribute("actionTypes", PasswordResetLog.ActionType.values());
            model.addAttribute("statuses", PasswordResetLog.Status.values());

            logger.info("Логи восстановления паролей загружены, количество: {}", logs.size());
            return "admin/password-reset-logs";

        } catch (Exception e) {
            logger.error("Ошибка при загрузке логов восстановления паролей", e);
            model.addAttribute("error", "Ошибка при загрузке логов: " + e.getMessage());
            return "admin/password-reset-logs";
        }
    }

    /**
     * Детали конкретной записи лога
     */
    @GetMapping("/password-reset-log/{id}")
    public String showPasswordResetLogDetails(@RequestParam("id") Long logId, Model model) {
        logger.info("Запрос деталей лога восстановления пароля с ID: {}", logId);

        try {
            PasswordResetLog log = passwordResetLogRepository.findById(logId)
                    .orElseThrow(() -> new RuntimeException("Лог не найден"));

            model.addAttribute("log", log);
            model.addAttribute("actionTypes", PasswordResetLog.ActionType.values());
            model.addAttribute("statuses", PasswordResetLog.Status.values());

            logger.info("Детали лога восстановления пароля загружены");
            return "admin/password-reset-log-details";

        } catch (Exception e) {
            logger.error("Ошибка при загрузке деталей лога восстановления пароля", e);
            model.addAttribute("error", "Ошибка при загрузке деталей: " + e.getMessage());
            return "admin/password-reset-log-details";
        }
    }
} 