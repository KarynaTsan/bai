package com.karina.bai.controller;

import com.karina.bai.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthPageController {
    private final UserService userService;

    public AuthPageController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            org.springframework.ui.Model model
    ) {
        try {
            userService.createUser(username, email, password);
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            // pokazuje błąd hjasła na stronie
            model.addAttribute("error", ex.getMessage());

            model.addAttribute("username", username);
            model.addAttribute("email", email);

            return "register";
        }
    }
}
