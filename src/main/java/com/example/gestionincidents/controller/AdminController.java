package com.example.gestionincidents.controller;

import com.example.gestionincidents.entity.UserRole;
import com.example.gestionincidents.entity.Utilisateur;
import com.example.gestionincidents.repository.UtilisateurRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UtilisateurRepository utilisateurRepository;

    public AdminController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @GetMapping
    public String adminHome(Model model, Authentication authentication) {

        // 1) Récupérer l'email de l'utilisateur connecté (username = email)
        String email = authentication.getName();

        // 2) Charger l'utilisateur métier depuis la base
        Utilisateur u = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + email));

        // 3) Construire le nom complet
        String fullName = (u.getPrenom() != null ? u.getPrenom() + " " : "")
                + (u.getNom() != null ? u.getNom() : "");

        // 4) Construire les initiales (ex : "AD")
        String initials = "";
        if (u.getPrenom() != null && !u.getPrenom().isEmpty()) {
            initials += u.getPrenom().charAt(0);
        }
        if (u.getNom() != null && !u.getNom().isEmpty()) {
            initials += u.getNom().charAt(0);
        }
        initials = initials.toUpperCase();

        // 5) Traduire le rôle enum -> label lisible
        String roleLabel;
        if (u.getRole() == UserRole.SUPER_ADMIN) {
            roleLabel = "Super administrateur";
        } else if (u.getRole() == UserRole.ADMIN) {
            roleLabel = "Administrateur";
        } else if (u.getRole() == UserRole.AGENT) {
            roleLabel = "Agent municipal";
        } else if (u.getRole() == UserRole.CITOYEN) {
            roleLabel = "Citoyen";
        } else {
            roleLabel = u.getRole().name();
        }

        // 6) Envoyer au template Thymeleaf
        model.addAttribute("fullName", fullName);
        model.addAttribute("initials", initials);
        model.addAttribute("roleLabel", roleLabel);

        return "admin-dashboard";
    }
}
