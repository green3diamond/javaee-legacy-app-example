package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Authentication Controller for login/logout pages.
 *
 * Migration notes:
 * - Replaces JAAS form-based authentication (login.jsp, login_error.jsp, logoff.jsp)
 * - Uses Spring Security instead of JBoss JAAS
 * - Provides proper login/logout endpoints
 */
@Controller
public class AuthController {

    /**
     * Display the login form.
     * Replaces: /jaas/login.jsp
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Display the logout confirmation.
     * Replaces: /jaas/logoff.jsp
     */
    @GetMapping("/logout-success")
    public String logoutSuccess() {
        return "logout_success";
    }
}
