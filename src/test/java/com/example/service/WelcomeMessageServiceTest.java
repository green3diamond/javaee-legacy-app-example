package com.example.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class WelcomeMessageServiceTest {

    private final WelcomeMessageService welcomeMessageService = new WelcomeMessageService();

    @Test
    void rendersAReadableMultiLineMessage() {
        String message = welcomeMessageService.buildWelcomeMessage("Jamie", "jamie@example.com");

        assertThat(message)
                .contains("Welcome, Jamie!")
                .contains("jamie@example.com")
                .contains("secure area");
    }
}
