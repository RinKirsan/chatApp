package rk.chatApp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AdminToolsController {
    @RequestMapping("/admintools")
    public String adminToolsPage() {
        return "admintools";
    }
}
