package com.karina.bai.controller;

import com.karina.bai.model.dto.RegisterForm;
import com.karina.bai.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
    public String registerPage(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("form") RegisterForm form,
            BindingResult br,
            Model model
    ) {
        if (br.hasErrors()) {
            return "register";
        }

        try {
            userService.createUser(form.getUsername(), form.getEmail(), form.getPassword());
            return "redirect:/login?registered";
        } catch (IllegalArgumentException ex) {
            //  "Email already exists", "Hasło musi..."
            model.addAttribute("error", ex.getMessage());
            return "register";
        }
    }
}
