package com.example.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC Configuration for the web module.
 *
 * Migration notes:
 * - Replaces struts-config.xml with annotation-based configuration
 * - Enables Spring MVC instead of Struts framework
 * - Configures view controllers for simple redirects
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.example.controller")
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configure simple view controllers (replaces some struts-config.xml mappings).
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Map root path to index page
        registry.addViewController("/").setViewName("index");

        // Map /secure to secure index page (replaces SecureAction)
        registry.addViewController("/secure").setViewName("secure/index");
    }
}
