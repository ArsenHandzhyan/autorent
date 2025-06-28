package ru.anapa.autorent.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute("javax.servlet.error.status_code");
        Object message = request.getAttribute("javax.servlet.error.message");
        Object exception = request.getAttribute("javax.servlet.error.exception");
        
        String errorMessage = "Произошла ошибка при обработке запроса";
        String errorDetails = null;
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            
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
        
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("errorDetails", errorDetails);
        model.addAttribute("statusCode", status);
        
        return "error";
    }
} 