package com.karina.bai.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserPageController {

    @GetMapping("/user/hello")
    public String userHello() {
        return "user-hi"; // templates/user-hello.html
    }
}
