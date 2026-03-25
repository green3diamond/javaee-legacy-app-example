package com.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.domain.UserAccount;
import com.example.repository.UserAccountRepository;
import com.example.security.CustomUserDetailsService;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Test
    void loadsExistingUsersFromTheRepository() {
        UserAccount account = new UserAccount("user@example.com", "hash", "Jamie", "ROLE_USER", Instant.now());
        when(userAccountRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(account));

        var detailsService = new CustomUserDetailsService(userAccountRepository);
        var userDetails = detailsService.loadUserByUsername("user@example.com");

        assertThat(userDetails.getUsername()).isEqualTo("user@example.com");
        assertThat(userDetails.getAuthorities()).extracting("authority").contains("ROLE_USER");
    }

    @Test
    void throwsWhenNoUserExists() {
        when(userAccountRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        var detailsService = new CustomUserDetailsService(userAccountRepository);

        assertThrows(UsernameNotFoundException.class, () -> detailsService.loadUserByUsername("missing@example.com"));
    }
}
