package com.example.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Service replacement for EJB 2.1 RegistrationBean.
 * 
 * Migration notes:
 * - Replaces com.example.RegistrationBean (EJB 2.1 Stateless Session Bean)
 * - Removes all EJB lifecycle methods (ejbCreate, ejbActivate, ejbPassivate, etc.)
 * - Uses Spring's @Service and @Transactional for dependency injection and transaction management
 * - No longer requires Home/Remote interfaces or JNDI lookup
 */
@Service("registrationService")
@Transactional
public class RegistrationService {
    
    /**
     * Register a user with the provided credentials.
     * 
     * @param username The user's username
     * @param password The user's password
     * @return Registration result message
     */
    public String register(String username, String password) {
        // TODO (Task 7): Replace hardcoded logic with real persistence layer
        // For now, maintain compatibility with original behavior
        return "Hello";
    }
    
}
