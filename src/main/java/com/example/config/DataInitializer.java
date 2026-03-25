package com.example.config;

import java.time.Clock;
import java.time.Instant;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.domain.UserAccount;
import com.example.repository.UserAccountRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedUsers(UserAccountRepository repository, PasswordEncoder passwordEncoder, Clock clock) {
        return args -> {
            if (!repository.existsByEmailIgnoreCase("admin@example.com")) {
                repository.save(new UserAccount(
                        "admin@example.com",
                        passwordEncoder.encode("Admin123"),
                        "Administrator",
                        "ROLE_ADMIN",
                        Instant.now(clock)
                ));
            }
        };
    }
}
