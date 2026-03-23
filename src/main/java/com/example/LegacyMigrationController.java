package com.example;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LegacyMigrationController {
    
    private final RegistrationService registrationService;

    public LegacyMigrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam("username") String username, 
                             @RequestParam("password") String password, 
                             Model model) {
        String message = registrationService.register(username, password);
        System.out.println(message);
        model.addAttribute("message", message);
        return "register_confirmation";
    }

    @GetMapping("/secure/")
    public String secureIndex() {
        return "secure/index"; // maps to secure/index.html
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
