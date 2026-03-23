package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class AuditServiceTest {
    
    @Test
    public void testLogRegistrationAttempt() {
        AuditService service = new AuditService();
        assertDoesNotThrow(() -> service.logRegistrationAttempt("testuser"));
    }
}
