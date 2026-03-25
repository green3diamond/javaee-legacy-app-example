package com.example.controller;

import java.security.Principal;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.service.DuplicateEmailException;
import com.example.service.RegistrationService;
import com.example.web.RegistrationRequest;

@Controller
public class PageController {

    private final RegistrationService registrationService;

    public PageController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/")
    String home() {
        return "index";
    }

    @GetMapping("/register")
    String registerForm(Model model) {
        if (!model.containsAttribute("registrationRequest")) {
            model.addAttribute("registrationRequest", new RegistrationRequest("", "", ""));
        }
        return "register";
    }

    @PostMapping("/register")
    String register(
            @Valid @ModelAttribute RegistrationRequest registrationRequest,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            model.addAttribute("registrationView", registrationService.register(registrationRequest));
            return "register_confirmation";
        } catch (DuplicateEmailException | IllegalArgumentException ex) {
            model.addAttribute("registrationError", ex.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    String login() {
        return "jaas/login";
    }

    @GetMapping("/secure")
    String secure(Model model, Principal principal) {
        model.addAttribute("username", resolveUsername(principal));
        return "secure/index";
    }

    private String resolveUsername(Principal principal) {
        return switch (principal) {
            case Authentication authentication
                    when authentication.getPrincipal() instanceof UserDetails userDetails -> userDetails.getUsername();
            case Principal namedPrincipal -> namedPrincipal.getName();
            case null -> "guest";
        };
    }
}
