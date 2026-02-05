package com.karina.bai.service;

import com.karina.bai.model.User;
import com.karina.bai.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;
    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void createUserHashesPasswordAndAssignsRole() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(passwordEncoder.encode("Strong!1A")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.createUser("user", "user@example.com", "Strong!1A");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User captured = captor.getValue();
        assertThat(captured.getPassword()).isEqualTo("hashed");
        assertThat(captured.getRole()).isEqualTo("ROLE_USER");
        assertThat(saved.getEmail()).isEqualTo("user@example.com");
    }

    @Test
    void createUserRejectsDuplicateEmail() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser("user", "user@example.com", "Strong!1A"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void createUserRejectsDuplicateUsername() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("user")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser("user", "user@example.com",  "Strong!1A"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void createUserRejectsWeakPassword() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("user")).thenReturn(false);

        assertThatThrownBy(() -> userService.createUser("user", "user@example.com", "short"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Hasło musi mieć co najmniej 8 znaków");

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void authenticateReturnsUserWhenPasswordMatches() {
        User user = new User("user", "user@example.com", "hashed");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Strong!1A", "hashed")).thenReturn(true);

        User result = userService.authenticate("user@example.com", "Strong!1A");

        assertThat(result).isSameAs(user);
    }

    @Test
    void authenticateRejectsInvalidPassword() {
        User user = new User("user", "user@example.com", "hashed");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> userService.authenticate("user@example.com", "wrong"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid credentials");
    }
}