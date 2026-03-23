package com.example.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/jaas/login", "/jaas/login_error").permitAll()
				.anyRequest().authenticated()
			)
			.formLogin(form -> form
				.loginPage("/jaas/login")
				.loginProcessingUrl("/login")
				.defaultSuccessUrl("/", true)
				.failureUrl("/jaas/login_error")
				.permitAll()
			)
			.logout(logout -> logout
				.logoutUrl("/jaas/logoff")
				.logoutSuccessUrl("/jaas/login?loggedOut")
				.invalidateHttpSession(true)
				.clearAuthentication(true)
				.deleteCookies("JSESSIONID")
			);

		// CSRF защита остава по подразбиране, за да не се премахва важен слой срещу CSRF.
		return http.build();
	}
}

