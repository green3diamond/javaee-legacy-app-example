package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private AuditService auditService;
    
    @InjectMocks
    private RegistrationService registrationService;

    @Test
    public void testRegisterNewUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(null);
        
        String result = registrationService.register("testuser", "testpass");
        
        assertEquals("Hello testuser", result);
        verify(auditService).logRegistrationAttempt("testuser");
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    public void testRegisterExistingUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(new User("testuser", "pass"));
        
        String result = registrationService.register("testuser", "testpass");
        
        assertEquals("User already exists!", result);
        verify(auditService).logRegistrationAttempt("testuser");
        verify(userRepository, never()).save(any(User.class));
    }
}
