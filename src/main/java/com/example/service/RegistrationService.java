package com.example.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.model.User;
import com.example.repository.UserRepository;

/**
 * Replaces EJB 2.1 Stateless Session Bean {@code RegistrationBean}.
 * <p>
 * Original: JNDI lookup → RegistrationHome.create() → RegistrationEJB.register()
 * Now: constructor-injected Spring @Service with @Transactional.
 */
@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }
        var user = new User(email, passwordEncoder.encode(password));
        return userRepository.save(user);
    }
}
