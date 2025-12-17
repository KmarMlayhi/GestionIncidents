package com.example.gestionincidents.controller;

import com.example.gestionincidents.entity.UserRole;
import com.example.gestionincidents.entity.Utilisateur;
import com.example.gestionincidents.repository.UtilisateurRepository;
import com.example.gestionincidents.service.ConnectedUserInfoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
@Controller
@RequestMapping
public class StaffProfileController {

    private final ConnectedUserInfoService connectedUserInfoService;
    private final UtilisateurRepository utilisateurRepository;

    public StaffProfileController(ConnectedUserInfoService connectedUserInfoService,
                                  UtilisateurRepository utilisateurRepository) {
        this.connectedUserInfoService = connectedUserInfoService;
        this.utilisateurRepository = utilisateurRepository;

    }

    @GetMapping({"/agent/profil", "/admin/profil"})
    public String staffProfile(Model model, Authentication authentication) {

        connectedUserInfoService.addConnectedUserInfo(model, authentication);

        String email = authentication.getName();
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable : " + email));

        if (user.getRole() != UserRole.AGENT && user.getRole() != UserRole.ADMIN) {
            throw new SecurityException("Accès refusé.");
        }

        model.addAttribute("user", user);

        // pour l’agent : afficher admin responsable (si existe)
        model.addAttribute("admin", user.getAdministrateur());

        // ✅ ici on choisit la vue selon le rôle
        if (user.getRole() == UserRole.ADMIN) {
            return "admin-profile";   // templates/admin-profile.html
        }
        return "agent-profile";       // templates/agent-profile.html
    }
}

