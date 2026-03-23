package com.example.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AuthUserServiceTest {
	@Test
	void loadUserByUsername_admin_returnsSIERole() {
		var encoder = new BCryptPasswordEncoder();
		var service = new AuthUserService(encoder);

		UserDetails details = service.loadUserByUsername("admin");

		assertEquals("admin", details.getUsername());
		Set<String> roles = details.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(java.util.stream.Collectors.toSet());
		assertTrue(roles.contains("ROLE_SIE"));
	}

	@Test
	void loadUserByUsername_unknown_throws() {
		var encoder = new BCryptPasswordEncoder();
		var service = new AuthUserService(encoder);

		assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("nobody"));
	}
}

