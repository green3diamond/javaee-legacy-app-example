package com.example.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.model.User;

/**
 * Spring Data JPA repository replacing manual JNDI lookups and EJB home interfaces.
 * RegistrationHome / RegistrationEJB remote interface → this single interface.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
