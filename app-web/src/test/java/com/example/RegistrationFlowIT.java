package com.example;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the complete registration flow.
 *
 * Migration notes:
 * - Tests the full HTTP request cycle: Controller → Service → View
 * - Replaces Struts Action testing with Spring MVC integration tests
 * - Tests Thymeleaf template rendering instead of JSP
 * - Verifies Spring Security integration
 */
@SpringBootTest
@AutoConfigureWebMvc
class RegistrationFlowIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testFullRegistrationFlow() throws Exception {
        // 1. GET /register - Display registration form
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registrationDTO"));

        // 2. POST /register - Submit registration form
        mockMvc.perform(post("/register")
                .param("username", "testuser")
                .param("password", "testpass"))
                .andExpect(status().isOk())
                .andExpect(view().name("register_confirmation"))
                .andExpect(model().attribute("registrationResult", "Hello"));
    }

    @Test
    void testHomePageAccess() throws Exception {
        // Test home page loads correctly
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void testSecureAreaRequiresAuthentication() throws Exception {
        // Test that /secure requires login (Spring Security)
        mockMvc.perform(get("/secure"))
                .andExpect(status().is3xxRedirection())  // Should redirect to login
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
