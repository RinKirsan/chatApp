package rk.chatApp.controller;

import org.springframework.web.bind.annotation.PathVariable;
import rk.chatApp.model.Group;
import rk.chatApp.model.User;
import rk.chatApp.repository.UserRepository;
import rk.chatApp.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class ChatController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/chat")
    public String chatPage(@AuthenticationPrincipal User user, Model model) {
        // Получаем список групп пользователя
        var userGroups = groupService.getGroupsByUser(user);
        // Получаем список всех групп и исключаем те, в которых пользователь уже состоит
        var allGroups = groupService.getAllGroups();
        allGroups.removeAll(userGroups);
        model.addAttribute("userGroups", userGroups);
        model.addAttribute("allGroups", allGroups);
        return "chat";
    }

    @PostMapping("/createGroup")
    public String createGroup(@AuthenticationPrincipal User user,
                              @RequestParam String groupName,
                              @RequestParam(required = false) String password,
                              @RequestParam(required = false) boolean isPrivate) {
        groupService.createGroup(groupName, user, password, isPrivate);
        return "redirect:/chat";
    }

    @PostMapping("/joinGroup")
    public String joinGroup(Principal principal,
                            @RequestParam Long groupId,
                            @RequestParam(required = false) String password) {
        if (principal == null) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }

        // Загружаем пользователя
        User user = userRepository.findByUsername(principal.getName());
        if (user == null) {
            throw new RuntimeException("Пользователь не найден в базе данных");
        }

        Group group = groupService.getGroupById(groupId)
                .orElseThrow(() -> new RuntimeException("Группа не найдена"));

        // Если группа приватная, проверяем правильность пароля
        if (group.isPrivate() && (password == null || !password.equals(group.getPassword()))) {
            throw new RuntimeException("Неверный пароль для приватной группы");
        }

        groupService.addUserToGroup(groupId, user);
        return "redirect:/chat";
    }



    @GetMapping("/chat/group/{groupId}")
    public String groupChat(@PathVariable Long groupId,
                            Model model) {
        // Получаем информацию о группе
        Group group = groupService.getGroupById(groupId)
                .orElseThrow(() -> new RuntimeException("Группа не найдена"));
        model.addAttribute("group", group);
        return "groupChat";
    }
}