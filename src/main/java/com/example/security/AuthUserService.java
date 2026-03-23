package com.example.security;

import java.util.List;
import java.util.Set;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * In-memory user directory for the demo app.
 * Replaces the legacy JAAS login module.
 */
@Service
public class AuthUserService implements UserDetailsService {
	private final PasswordEncoder passwordEncoder;

	public AuthUserService(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		if (!"admin".equals(username)) {
			throw new UsernameNotFoundException("Unknown user: " + username);
		}

		Set<String> roles = Set.of("SIE");
		List<String> roleList = List.copyOf(roles);

		// Spring stores roles without the "ROLE_" prefix in the "roles(...)" builder.
		return User.withUsername("admin")
			.password(passwordEncoder.encode("123456"))
			.roles(roleList.toArray(new String[0]))
			.build();
	}
}

