package com.example.demo.controller.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                         @RequestParam(required = false) String logout,
                         Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Email ou mot de passe incorrect.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Vous avez été déconnecté.");
        }
        return "login";
    }
}
