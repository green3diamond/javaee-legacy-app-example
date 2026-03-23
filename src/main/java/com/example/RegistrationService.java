package com.example;

import org.springframework.stereotype.Service;

@Service
public class RegistrationService {
    
    private final UserRepository userRepository;
    private final AuditService auditService;

    public RegistrationService(UserRepository userRepository, AuditService auditService) {
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    public String register(String username, String password) {
        auditService.logRegistrationAttempt(username);
        if (userRepository.findByUsername(username) != null) {
            return "User already exists!";
        }
        User newUser = new User(username, password);
        userRepository.save(newUser);
        return "Hello " + username; 
    }
}
