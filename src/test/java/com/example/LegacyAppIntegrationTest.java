package com.example;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@SpringBootTest
@AutoConfigureMockMvc
class LegacyAppIntegrationTest {
	@Autowired
	private MockMvc mockMvc;

	@Test
	void fullRequestCycle_login_register_andSecurePage() throws Exception {
		MockHttpSession session = new MockHttpSession();

		mockMvc.perform(post("/login")
				.param("username", "admin")
				.param("password", "123456")
				.with(csrf())
				.session(session))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/"));

		mockMvc.perform(get("/")
				.session(session))
			.andExpect(status().isOk())
			.andExpect(view().name("index"));

		mockMvc.perform(post("/register")
				.param("email", "a@b.com")
				.param("password", "pw")
				.with(csrf())
				.session(session))
			.andExpect(status().isOk())
			.andExpect(view().name("register_confirmation"))
			.andExpect(model().attributeExists("message"));

		mockMvc.perform(get("/secure")
				.session(session))
			.andExpect(status().isOk())
			.andExpect(view().name("secure/index"));
	}
}

