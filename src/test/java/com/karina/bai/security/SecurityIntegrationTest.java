package com.karina.bai.security;

import com.karina.bai.config.SecurityConfig;
import com.karina.bai.controller.AdminPageController;
import com.karina.bai.controller.AuthPageController;
import com.karina.bai.controller.NoteController;
import com.karina.bai.controller.UserPageController;
import com.karina.bai.model.Note;
import com.karina.bai.service.NoteService;
import com.karina.bai.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = {
        UserPageController.class,
        AdminPageController.class,
        AuthPageController.class,
        NoteController.class
})
@Import(SecurityConfig.class)
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private NoteService noteService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void unauthenticatedUserIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/user/hello"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void userRoleCanAccessUserPage() throws Exception {
        mockMvc.perform(get("/user/hello"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-hi"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminRoleCanAccessAdminPage() throws Exception {
        mockMvc.perform(get("/admin/hello"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-hi"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void userRoleCannotAccessAdminPage() throws Exception {
        mockMvc.perform(get("/admin/hello"))
                .andExpect(status().isForbidden());
    }

    @Test
    void csrfIsRequiredOnRegister() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "user")
                        .param("email", "user@example.com")
                        .param("password", "Strong!1A"))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerSucceedsWithCsrf() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "user")
                        .param("email", "user@example.com")
                        .param("password", "Strong!1A"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login?registered"));

        verify(userService).createUser("user", "user@example.com", "Strong!1A");
    }

    @Test
    @WithMockUser(roles = "USER")
    void csrfIsRequiredForNotesCreate() throws Exception {
        mockMvc.perform(post("/user/notes")
                        .param("title", "Test")
                        .param("content", "Content"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void notesCreateWithCsrfIsAccepted() throws Exception {
        when(noteService.create(any(), any(), any())).thenReturn(new Note(1L, "Test", "Content"));

        mockMvc.perform(post("/user/notes")
                        .with(csrf())
                        .param("title", "Test")
                        .param("content", "Content"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/user/notes"));

        verify(noteService).create("user@example.com", "Test", "Content");
    }
}