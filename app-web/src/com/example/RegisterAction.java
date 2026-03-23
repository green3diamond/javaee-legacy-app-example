package com.example;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegisterAction {
	private final RegistrationBean registrationBean;

	public RegisterAction(RegistrationBean registrationBean) {
		this.registrationBean = registrationBean;
	}

	@GetMapping("/register")
	public String showRegister() {
		return "register";
	}

	@PostMapping("/register")
	public String register(@ModelAttribute RegistrationRequest form, Model model) {
		var message = registrationBean.register(form.email(), form.password());
		model.addAttribute("message", message);
		return "register_confirmation";
	}

	/**
	 * Modern request DTO (Task 6: records).
	 */
	public record RegistrationRequest(String email, String password) {}
}

