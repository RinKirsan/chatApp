package rk.chatApp.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@ControllerAdvice
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");

        if (statusCode != null) {
            if (statusCode == 404) {
                model.addAttribute("errorMessage", "Ресурс не найден.");
            } else if (statusCode == 403) {
                model.addAttribute("errorMessage", "У вас нет доступа к этому ресурсу.");
            } else {
                model.addAttribute("errorMessage", "Произошла ошибка: " + statusCode);
            }
        } else {
            model.addAttribute("errorMessage", "Произошла неизвестная ошибка.");
        }

        return "errorPage";
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "errorPage";
    }
}