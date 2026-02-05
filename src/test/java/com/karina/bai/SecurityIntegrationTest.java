package com.karina.bai;

import com.karina.bai.model.User;
import com.karina.bai.repository.NoteRepository;
import com.karina.bai.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        noteRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(buildUser("user", "user@example.com", "ROLE_USER"));
        userRepository.save(buildUser("admin", "admin@example.com", "ROLE_ADMIN"));
    }

    @Test
    void loginPageIsPublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void unauthenticatedUserIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/user/hello"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void userCanAccessUserEndpoint() throws Exception {
        mockMvc.perform(get("/user/hello"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-hi"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void userCannotAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/admin/hello"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void adminCanAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/admin/hello"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-hi"));
    }

    @Test
    void formLoginRedirectsBasedOnRole() throws Exception {
        mockMvc.perform(formLogin("/login").user("username", "user@example.com")
                        .password("password", "Valid1!Pass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/hello"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void postWithoutCsrfIsRejected() throws Exception {
        mockMvc.perform(post("/user/notes")
                        .param("title", "Note title")
                        .param("content", "Note content"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void postWithCsrfIsAccepted() throws Exception {
        mockMvc.perform(post("/user/notes")
                        .param("title", "Note title")
                        .param("content", "Note content")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/notes"));
    }

    private User buildUser(String username, String email, String role) {
        User user = new User(username, email, passwordEncoder.encode("Valid1!Pass"));
        user.setRole(role);
        return user;
    }
}