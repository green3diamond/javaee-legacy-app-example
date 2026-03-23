package com.example.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
	@GetMapping("/jaas/login")
	public String login() {
		return "jaas/login";
	}

	@GetMapping("/jaas/login_error")
	public String loginError() {
		return "jaas/login_error";
	}
}

