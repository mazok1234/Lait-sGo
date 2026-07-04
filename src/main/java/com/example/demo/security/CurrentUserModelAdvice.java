package com.example.demo.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.demo.repository.auth.UtilisateurRepository;

@ControllerAdvice
public class CurrentUserModelAdvice {
    private final UtilisateurRepository utilisateurRepository;

    public CurrentUserModelAdvice(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @ModelAttribute("currentUserNom")
    public String currentUserNom() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        return utilisateurRepository.findByEmail(authentication.getName())
                .map(u -> u.getNom())
                .orElse(authentication.getName());
    }
}
