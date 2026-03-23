package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.example.registration.RegistrationValidator;

class RegistrationBeanTest {
	@Test
	void register_delegatesToValidator_andReturnsMessage() {
		var validator = Mockito.mock(RegistrationValidator.class);
		var bean = new RegistrationBean(validator);

		var result = bean.register("a@b.com", "pw");

		assertEquals("Hello", result);
		verify(validator).validate(eq("a@b.com"), eq("pw"));
	}

	@Test
	void register_whenValidatorFails_propagatesException() {
		var validator = Mockito.mock(RegistrationValidator.class);
		doThrow(new IllegalArgumentException("Email is required")).when(validator).validate(Mockito.anyString(), Mockito.anyString());
		var bean = new RegistrationBean(validator);

		assertThrows(IllegalArgumentException.class, () -> bean.register("", "pw"));
	}
}

