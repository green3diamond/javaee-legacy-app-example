package com.example.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Java 21 record replacing the Struts ActionForm pattern.
 * Immutable DTO for registration form data — modern Java idiom.
 */
public record RegistrationRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password
) {}
