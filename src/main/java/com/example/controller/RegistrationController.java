package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.model.RegistrationRequest;
import com.example.service.RegistrationService;

import jakarta.validation.Valid;

/**
 * Replaces Struts 1.1 {@code RegisterAction} + struts-config.xml routing.
 * <p>
 * Original flow: register.jsp → POST /register.do → RegisterAction.execute()
 *   → JNDI lookup → RegistrationHome.create() → RegistrationEJB.register()
 *   → forward to register_confirmation.jsp
 * <p>
 * Now: register.html → POST /register → this controller → RegistrationService
 *   → redirect to confirmation view
 */
@Controller
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationRequest", new RegistrationRequest("", ""));
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute RegistrationRequest registrationRequest,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            registrationService.register(registrationRequest.email(), registrationRequest.password());
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }

        return "register_confirmation";
    }
}
