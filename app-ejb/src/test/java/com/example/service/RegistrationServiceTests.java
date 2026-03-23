package com.example.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RegistrationServiceTests {

    @Autowired
    private RegistrationService registrationService;

    @Test
    void testRegistrationServiceInjection() {
        assertThat(registrationService).isNotNull();
    }

    @Test
    void testRegisterReturnsHello() {
        assertThat(registrationService.register("testuser", "testpass")).isEqualTo("Hello");
    }
}
