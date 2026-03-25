package com.example.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PasswordPolicyServiceTest {

    private final PasswordPolicyService passwordPolicyService = new PasswordPolicyService();

    @Test
    void acceptsStrongPasswords() {
        assertDoesNotThrow(() -> passwordPolicyService.validate("Modern123"));
    }

    @Test
    void rejectsPasswordsWithoutUppercaseLetters() {
        assertThrows(IllegalArgumentException.class, () -> passwordPolicyService.validate("modern123"));
    }
}
