package com.example;

import org.springframework.stereotype.Service;

import com.example.registration.RegistrationValidator;

/**
 * Modern replacement for the legacy EJB 2.1 Session Bean.
 */
@Service
public class RegistrationBean {
	private final RegistrationValidator registrationValidator;

	public RegistrationBean(RegistrationValidator registrationValidator) {
		this.registrationValidator = registrationValidator;
	}

	public String register(String email, String password) {
		registrationValidator.validate(email, password);
		return "Hello";
	}
}

