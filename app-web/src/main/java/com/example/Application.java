package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot Application main class.
 *
 * Migration notes:
 * - Replaces JBoss 4.0.5 application server deployment
 * - Eliminates EAR/WAR deployment descriptors (application.xml, web.xml, etc.)
 * - Provides embedded Tomcat container
 * - Enables Spring Boot auto-configuration
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
