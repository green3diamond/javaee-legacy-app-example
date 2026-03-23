package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.service.RegistrationService;

/**
 * Spring MVC Controller replacement for Struts 1 RegisterAction.
 *
 * Migration notes:
 * - Replaces com.example.RegisterAction (Struts 1 Action)
 * - Uses Spring MVC @Controller instead of Struts Action
 * - Uses @RequestMapping instead of struts-config.xml routing
 * - Uses Thymeleaf templates instead of JSP + Struts tags
 * - Eliminates ActionForm/ActionForward pattern
 */
@Controller
public class RegisterController {

    @Autowired
    private RegistrationService registrationService;

    /**
     * Display the registration form.
     * Replaces: struts-config.xml action path="/register" (GET)
     */
    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register"; // Thymeleaf template: register.html
    }

    /**
     * Process the registration form submission.
     * Replaces: struts-config.xml action path="/register" (POST)
     */
    @PostMapping("/register")
    public String processRegistration(
            @RequestParam String username,
            @RequestParam String password,
            Model model) {

        // Call the business service
        String result = registrationService.register(username, password);

        // Add result to model for template rendering
        model.addAttribute("registrationResult", result);

        // Forward to success page (replaces ActionForward "success")
        return "register_confirmation"; // Thymeleaf template: register_confirmation.html
    }
}
