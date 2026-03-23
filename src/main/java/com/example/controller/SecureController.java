package com.example.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Replaces Struts 1.1 {@code SecureAction} and the /secure/index.jsp scriptlet.
 * Original relied on container-managed JAAS auth + request.getUserPrincipal().
 * Now uses Spring Security's Principal injection.
 */
@Controller
@RequestMapping("/secure")
public class SecureController {

    @GetMapping("/")
    public String securePage(Principal principal, Model model) {
        model.addAttribute("username", principal.getName());
        return "secure";
    }
}
