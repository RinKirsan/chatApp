package rk.chatApp.controller;

import rk.chatApp.model.User;
import rk.chatApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@Secured("ROLE_ADMIN") // Ограничиваем доступ только для администраторов
public class AdminController {

    @Autowired
    private UserService userService;

    // Страница с списком пользователей
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users"; // Страница с таблицей пользователей
    }

    // Удаление пользователя
    @RequestMapping(value = "/users/{id}", method = RequestMethod.POST)
    public String deleteUser(@PathVariable Long id, @RequestParam("_method") String method) {
        if ("delete".equalsIgnoreCase(method)) {
            userService.deleteUser(id);
        }
        return "redirect:/admin/users"; // После удаления редиректим на список
    }

    // Изменение роли пользователя
    @PostMapping("/users/{id}/role")
    public String changeUserRole(@PathVariable Long id, @RequestParam String role) {
        userService.changeUserRole(id, role);
        return "redirect:/admin/users"; // После изменения роли редиректим на список
    }
}
