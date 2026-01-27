package com.karina.bai.service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import com.karina.bai.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.karina.bai.model.User;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(String username, String email, String password) {
        if (repo.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (repo.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        // hash
        validatePasswordPolicy(password);

        String hashed = passwordEncoder.encode(password);
        User user = new User(username, email, hashed);
        user.setRole("ROLE_USER");

        return repo.save(user);
    }

    public User authenticate(String email, String password) {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        // check passwd
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return user;
    }

    private void validatePasswordPolicy(String password) {
        if (password.length() < 8) {
            throw new IllegalArgumentException("Hasło musi mieć co najmniej 8 znaków");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Hasło musi zawierać wielką literę");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Hasło musi zawierać małą literę");
        }

        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Hasło musi zawierać cyfrę");
        }

        if (!password.matches(".*[!@#$%^&*()_+=\\-].*")) {
            throw new IllegalArgumentException("Hasło musi zawierać znak specjalny");
        }

        String lower = password.toLowerCase();
        if (lower.contains("password") || lower.contains("123456") || lower.contains("qwerty")) {
            throw new IllegalArgumentException("Hasło jest zbyt proste");
        }
    }

}
