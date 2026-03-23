package com.example.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Full request-cycle integration test replacing the manual JBoss deployment verification.
 * Boots the entire Spring Boot context with embedded H2 and exercises:
 *   1. Public registration flow (GET + POST)
 *   2. Security redirect for unauthenticated access to /secure/
 *   3. Authenticated access to /secure/
 */
@SpringBootTest
@AutoConfigureMockMvc
class FullRequestCycleTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void homePage_returnsIndexView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void registrationPage_displaysForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registrationRequest"));
    }

    @Test
    void registration_withValidData_showsConfirmation() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "newuser@example.com")
                        .param("password", "secret123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register_confirmation"));
    }

    @Test
    void registration_withInvalidEmail_returnsFormWithErrors() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "not-an-email")
                        .param("password", "secret123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().hasErrors());
    }

    @Test
    void secureArea_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/secure/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "USER")
    void secureArea_authenticated_returnsSecureView() throws Exception {
        mockMvc.perform(get("/secure/"))
                .andExpect(status().isOk())
                .andExpect(view().name("secure"))
                .andExpect(model().attribute("username", "admin@example.com"));
    }
}
