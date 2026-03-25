package com.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.domain.UserAccount;
import com.example.repository.UserAccountRepository;
import com.example.web.RegistrationRequest;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordPolicyService passwordPolicyService;

    @Mock
    private WelcomeMessageService welcomeMessageService;

    @Mock
    private AuditService auditService;

    private final Clock clock = Clock.fixed(Instant.parse("2026-03-25T10:15:30Z"), ZoneOffset.UTC);

    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        registrationService = new RegistrationService(
                userAccountRepository,
                passwordEncoder,
                passwordPolicyService,
                welcomeMessageService,
                auditService,
                clock
        );
    }

    @Test
    void registersANewUserAndAuditsTheEvent() {
        RegistrationRequest request = new RegistrationRequest("USER@Example.com", "Strong123", "Jamie");

        when(userAccountRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Strong123")).thenReturn("encoded-password");
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(welcomeMessageService.buildWelcomeMessage("Jamie", "user@example.com")).thenReturn("welcome");
        when(auditService.registrationCreated(any(UserAccount.class))).thenReturn(CompletableFuture.completedFuture("ok"));

        var result = registrationService.register(request);

        ArgumentCaptor<UserAccount> accountCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(accountCaptor.capture());
        verify(auditService).registrationCreated(accountCaptor.getValue());

        assertThat(accountCaptor.getValue().getEmail()).isEqualTo("user@example.com");
        assertThat(accountCaptor.getValue().getPasswordHash()).isEqualTo("encoded-password");
        assertThat(accountCaptor.getValue().getCreatedAt()).isEqualTo(Instant.parse("2026-03-25T10:15:30Z"));
        assertThat(result.email()).isEqualTo("user@example.com");
        assertThat(result.welcomeMessage()).isEqualTo("welcome");
    }

    @Test
    void rejectsDuplicateEmails() {
        when(userAccountRepository.existsByEmailIgnoreCase("existing@example.com")).thenReturn(true);

        assertThrows(
                DuplicateEmailException.class,
                () -> registrationService.register(new RegistrationRequest("existing@example.com", "Strong123", "Jamie"))
        );
    }
}
