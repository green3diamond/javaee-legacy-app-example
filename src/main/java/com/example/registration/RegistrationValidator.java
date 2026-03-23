package com.example.registration;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RegistrationValidator {
	public void validate(String email, String password) {
		// Legacy app използва "register(String username, String password)" с произволни имена на параметри.
		// В модерния вариант третираме първото поле като email/username.
		if (!StringUtils.hasText(email)) {
			throw new IllegalArgumentException("Email is required");
		}
		if (!StringUtils.hasText(password)) {
			throw new IllegalArgumentException("Password is required");
		}
	}
}

