package rk.chatApp.controller;


import rk.chatApp.model.Group;
import rk.chatApp.model.User;
import rk.chatApp.service.GroupService;
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
    @Autowired
    private GroupService groupService;

    // Страница со списком пользователей
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    // Удаление пользователя
    @RequestMapping(value = "/users/{id}", method = RequestMethod.POST)
    public String deleteUser(@PathVariable Long id, @RequestParam("_method") String method) {
        if ("delete".equalsIgnoreCase(method)) {
            userService.deleteUser(id);
        }
        return "redirect:/admin/users";
    }

    // Изменение роли пользователя
    @PostMapping("/users/{id}/role")
    public String changeUserRole(@PathVariable Long id, @RequestParam String role) {
        userService.changeUserRole(id, role);
        return "redirect:/admin/users";
    }



    // Страница со списком групп
    @GetMapping("/groups")
    public String listGroups(Model model) {
        List<Group> groups = groupService.getAllGroups();
        model.addAttribute("groups", groups);
        return "admin/groups"; // Страница с таблицей групп
    }

    // Удаление группы
    @RequestMapping(value = "/groups/{id}", method = RequestMethod.POST)
    public String deleteGroup(@PathVariable Long id, @RequestParam("_method") String method) {
        if ("delete".equalsIgnoreCase(method)) {
            groupService.deleteGroup(id);
        }
        return "redirect:/admin/groups";
    }

    // Переименование группы
    @PostMapping("/groups/{id}/rename")
    public String renameGroup(@PathVariable Long id, @RequestParam String newName) {
        groupService.renameGroup(id, newName);
        return "redirect:/admin/groups";
    }

    @PostMapping("/groups/{groupId}/removeUser")
    public String removeUserFromGroup(@PathVariable Long groupId, @RequestParam Long userId) {
        groupService.removeUserFromGroup(groupId, userId);
        return "redirect:/admin/groups"; // После удаления редиректим на страницу групп
    }
}
