package com.example.dto;

/**
 * Java 21 Record for registration data transfer.
 *
 * Migration notes:
 * - Replaces ActionForm/Action bean pattern from Struts 1
 * - Uses Java 21 record for immutable data transfer
 * - Eliminates getter/setter boilerplate
 * - Provides automatic equals(), hashCode(), toString()
 */
public record RegistrationDTO(
    String username,
    String password
) {
    // Java 21 record automatically provides:
    // - Constructor with all fields
    // - Getter methods (username(), password())
    // - equals(), hashCode(), toString()
    // - Immutability
    
    /**
     * Custom validation method.
     * 
     * @return true if the registration data is valid
     */
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() 
            && password != null && password.length() >= 6;
    }
}
