package com.example.registration;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class RegistrationValidatorTest {
	@Test
	void validate_rejectsBlankEmail() {
		var validator = new RegistrationValidator();

		assertThrows(IllegalArgumentException.class, () -> validator.validate("", "pw"));
		assertThrows(IllegalArgumentException.class, () -> validator.validate("   ", "pw"));
	}

	@Test
	void validate_rejectsBlankPassword() {
		var validator = new RegistrationValidator();

		assertThrows(IllegalArgumentException.class, () -> validator.validate("a@b.com", ""));
		assertThrows(IllegalArgumentException.class, () -> validator.validate("a@b.com", "   "));
	}
}

