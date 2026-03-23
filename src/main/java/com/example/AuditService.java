package com.example;

import org.springframework.stereotype.Service;

@Service
public class AuditService {
    public void logRegistrationAttempt(String username) {
        System.out.println("Audit log: Registration attempt for user " + username);
    }
}
