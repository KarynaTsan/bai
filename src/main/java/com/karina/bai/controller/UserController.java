//

/*
package com.karina.bai.controller;

import com.karina.bai.model.User;
import com.karina.bai.model.dto.CreateUserRequest;
import com.karina.bai.model.dto.LoginRequest;
import com.karina.bai.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/users")
    public ResponseEntity<?> register(@Valid @RequestBody CreateUserRequest req) {
        User created = service.createUser(req.getUsername(), req.getEmail(), req.getPassword());
        return ResponseEntity.ok("User created with id=" + created.getId());
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        User user = service.authenticate(req.getEmail(), req.getPassword());
        return ResponseEntity.ok("Logged in as " + user.getUsername());
    }
}
*/
