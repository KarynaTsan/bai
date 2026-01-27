package com.karina.bai.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {

    @GetMapping("/admin/hello")
    public String adminHello() {
        return "admin-hi"; // templates/admin-hello.html
    }
}
