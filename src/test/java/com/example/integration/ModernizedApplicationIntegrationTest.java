package com.example.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class ModernizedApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registrationAndLoginFlowWorks() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("displayName", "Taylor")
                        .param("email", "taylor@example.com")
                        .param("password", "Strong123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register_confirmation"));

        MvcResult loginResult = mockMvc.perform(formLogin("/login").user("taylor@example.com").password("Strong123"))
                .andExpect(authenticated().withUsername("taylor@example.com"))
                .andExpect(redirectedUrl("/secure"))
                .andReturn();

        HttpSession session = loginResult.getRequest().getSession(false);

        mockMvc.perform(get("/secure").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(view().name("secure/index"));
    }
}
