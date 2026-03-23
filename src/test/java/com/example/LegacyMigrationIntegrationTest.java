package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LegacyMigrationIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testFullRegistrationAndLoginCycle() {
        String baseUrl = "http://localhost:" + port;
        
        // 1. Register a new user
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> regMap = new LinkedMultiValueMap<>();
        regMap.add("username", "intuser");
        regMap.add("password", "intpass");
        HttpEntity<MultiValueMap<String, String>> regRequest = new HttpEntity<>(regMap, headers);
        
        ResponseEntity<String> regResponse = restTemplate.postForEntity(baseUrl + "/register", regRequest, String.class);
        assertThat(regResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(regResponse.getBody()).contains("You were registered!");
        
        // 2. Login with the new user using standard form login
        MultiValueMap<String, String> loginMap = new LinkedMultiValueMap<>();
        loginMap.add("username", "intuser");
        loginMap.add("password", "intpass");
        HttpEntity<MultiValueMap<String, String>> loginRequest = new HttpEntity<>(loginMap, headers);
        
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(baseUrl + "/login", loginRequest, String.class);
        assertThat(loginResponse.getStatusCode()).isIn(HttpStatus.FOUND, HttpStatus.OK); 
    }
}
