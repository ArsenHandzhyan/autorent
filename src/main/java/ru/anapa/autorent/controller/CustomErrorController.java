package ru.anapa.autorent.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public Object handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute("javax.servlet.error.status_code");
        Object message = request.getAttribute("javax.servlet.error.message");
        Object exception = request.getAttribute("javax.servlet.error.exception");
        String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");
        
        // Определяем, является ли это API запросом
        boolean isApiRequest = isApiRequest(request, requestUri);
        
        String errorMessage = "Произошла ошибка при обработке запроса";
        String errorDetails = null;
        int statusCode = 500;
        
        if (status != null) {
            statusCode = Integer.parseInt(status.toString());
            
            switch (statusCode) {
                case 404:
                    errorMessage = "Страница не найдена";
                    errorDetails = "Запрашиваемая страница не существует. Проверьте правильность URL адреса.";
                    break;
                case 403:
                    errorMessage = "Доступ запрещен";
                    errorDetails = "У вас нет прав для доступа к этой странице.";
                    break;
                case 401:
                    errorMessage = "Необходима авторизация";
                    errorDetails = "Пожалуйста, войдите в систему для доступа к этой странице.";
                    break;
                case 500:
                    errorMessage = "Ошибка сервера";
                    errorDetails = "Произошла внутренняя ошибка сервера. Пожалуйста, попробуйте позже.";
                    break;
                default:
                    errorMessage = "Произошла ошибка";
                    errorDetails = "Код ошибки: " + statusCode;
                    break;
            }
        }
        
        if (message != null && !message.toString().isEmpty()) {
            errorDetails = message.toString();
        }
        
        // Для API запросов возвращаем JSON
        if (isApiRequest) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", errorMessage);
            errorResponse.put("details", errorDetails);
            errorResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            errorResponse.put("path", requestUri);
            errorResponse.put("status", statusCode);
            
            return ResponseEntity.status(HttpStatus.valueOf(statusCode))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
        
        // Для веб-запросов возвращаем HTML
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("errorDetails", errorDetails);
        model.addAttribute("statusCode", status);
        model.addAttribute("requestUri", requestUri);
        
        return "error";
    }
    
    /**
     * Определяет, является ли запрос API запросом
     */
    private boolean isApiRequest(HttpServletRequest request, String requestUri) {
        // Проверяем заголовок Accept
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("application/json")) {
            return true;
        }
        
        // Проверяем Content-Type
        String contentType = request.getHeader("Content-Type");
        if (contentType != null && contentType.contains("application/json")) {
            return true;
        }
        
        // Проверяем URL на наличие /api/
        if (requestUri != null && requestUri.startsWith("/api/")) {
            return true;
        }
        
        // Проверяем User-Agent на наличие API клиентов
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && (userAgent.contains("Postman") || userAgent.contains("curl") || userAgent.contains("Insomnia"))) {
            return true;
        }
        
        return false;
    }
} 