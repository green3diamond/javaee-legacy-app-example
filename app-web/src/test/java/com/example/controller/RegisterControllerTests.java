package com.example.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Integration tests for RegisterController.
 *
 * Migration notes:
 * - Tests the Spring MVC @Controller replacement for Struts 1 RegisterAction
 * - Uses Spring Boot Test framework instead of Struts TestCase
 * - Tests HTTP endpoints instead of ActionForward mappings
 */
@SpringBootTest
@AutoConfigureWebMvc
class RegisterControllerTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Test
    void testShowRegistrationForm() throws Exception {
        // Set up MockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Test GET /register displays the form
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    void testProcessRegistration() throws Exception {
        // Set up MockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Test POST /register processes the form and redirects to confirmation
        mockMvc.perform(post("/register")
                .param("username", "testuser")
                .param("password", "testpass"))
                .andExpect(status().isOk())
                .andExpect(view().name("register_confirmation"))
                .andExpect(model().attributeExists("registrationResult"));
    }
}
