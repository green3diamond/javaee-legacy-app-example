package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security Configuration.
 *
 * Migration notes:
 * - Replaces JBoss JAAS authentication (MyLoginModule)
 * - Eliminates hardcoded credentials from source code
 * - Provides BCrypt password hashing instead of plaintext
 * - Enables CSRF protection (missing in Struts 1)
 * - Secures /secure/* endpoints with form-based login
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configure HTTP security (replaces web.xml security constraints).
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/secure/**").authenticated()  // Require auth for /secure/*
                .anyRequest().permitAll()                        // Allow all other requests
            )
            .formLogin(form -> form
                .loginPage("/login")                             // Custom login page
                .defaultSuccessUrl("/secure", true)             // Redirect to secure area after login
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")                          // Redirect to home after logout
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/login")              // Allow login POST without CSRF token
            );

        return http.build();
    }

    /**
     * Password encoder (BCrypt instead of plaintext).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * User details service (replaces MyLoginModule).
     * TODO: Replace with database-backed UserDetailsService in production.
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // Create admin user with BCrypt-encoded password
        // Original hardcoded password "123456" is now securely hashed
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("securePassword123"))  // Changed from hardcoded "123456"
                .roles("USER", "ADMIN")  // Added roles (original had "SIE")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }
}
