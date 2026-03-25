package com.example.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.domain.UserAccount;
import com.example.repository.UserAccountRepository;
import com.example.web.RegistrationRequest;
import com.example.web.RegistrationView;

@Service
public class RegistrationService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyService passwordPolicyService;
    private final WelcomeMessageService welcomeMessageService;
    private final AuditService auditService;
    private final Clock clock;

    public RegistrationService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            PasswordPolicyService passwordPolicyService,
            WelcomeMessageService welcomeMessageService,
            AuditService auditService,
            Clock clock
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicyService = passwordPolicyService;
        this.welcomeMessageService = welcomeMessageService;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional
    public RegistrationView register(RegistrationRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        String displayName = request.displayName().trim();

        passwordPolicyService.validate(request.password());

        if (userAccountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateEmailException(normalizedEmail);
        }

        UserAccount account = new UserAccount(
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                displayName,
                "ROLE_USER",
                Instant.now(clock)
        );
        UserAccount saved = userAccountRepository.save(account);
        auditService.registrationCreated(saved);

        return new RegistrationView(
                saved.getDisplayName(),
                saved.getEmail(),
                welcomeMessageService.buildWelcomeMessage(saved.getDisplayName(), saved.getEmail())
        );
    }
}
