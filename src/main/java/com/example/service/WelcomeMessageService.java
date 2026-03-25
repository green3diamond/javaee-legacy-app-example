package com.example.service;

import org.springframework.stereotype.Service;

@Service
public class WelcomeMessageService {

    public String buildWelcomeMessage(String displayName, String email) {
        return """
                Welcome, %s!

                Your account for %s is active and ready to use.
                You can sign in now and access the secure area.
                """.formatted(displayName, email);
    }
}
