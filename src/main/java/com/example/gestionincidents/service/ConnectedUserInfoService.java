package com.example.gestionincidents.service;

import com.example.gestionincidents.entity.UserRole;
import com.example.gestionincidents.entity.Utilisateur;
import com.example.gestionincidents.repository.UtilisateurRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ConnectedUserInfoService {

    private final UtilisateurRepository utilisateurRepository;

    public ConnectedUserInfoService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    public void addConnectedUserInfo(Model model, Authentication authentication) {
        if (authentication == null) return;

        String email = authentication.getName();

        Utilisateur u = utilisateurRepository.findByEmail(email)
                .orElseGet(() -> {
                    // Cas du SUPER_ADMIN créé côté Spring Security uniquement
                    Utilisateur nouveau = new Utilisateur();
                    nouveau.setEmail(email);

                    // Déterminer le rôle à partir des authorities
                    UserRole role = UserRole.CITOYEN;
                    String authority = authentication.getAuthorities().stream()
                            .findFirst()
                            .map(a -> a.getAuthority()) // ex : "ROLE_SUPER_ADMIN"
                            .orElse("ROLE_CITOYEN");

                    if ("ROLE_SUPER_ADMIN".equals(authority)) role = UserRole.SUPER_ADMIN;
                    else if ("ROLE_ADMIN".equals(authority)) role = UserRole.ADMIN;
                    else if ("ROLE_AGENT".equals(authority)) role = UserRole.AGENT;

                    nouveau.setRole(role);
                    nouveau.setNom("Super Admin"); // pour toi ça passe
                    nouveau.setPrenom(null);

                    return utilisateurRepository.save(nouveau);
                });

        // Construire nom complet
        String fullName = (u.getPrenom() != null ? u.getPrenom() + " " : "")
                + (u.getNom() != null ? u.getNom() : "");

        // Construire initiales
        String initials = "";
        if (u.getPrenom() != null && !u.getPrenom().isEmpty()) {
            initials += u.getPrenom().charAt(0);
        }
        if (u.getNom() != null && !u.getNom().isEmpty()) {
            initials += u.getNom().charAt(0);
        }
        initials = initials.toUpperCase();

        // Rôle lisible
        String roleLabel;
        if (u.getRole() == UserRole.SUPER_ADMIN) roleLabel = "Super administrateur";
        else if (u.getRole() == UserRole.ADMIN) roleLabel = "Administrateur";
        else if (u.getRole() == UserRole.AGENT) roleLabel = "Agent municipal";
        else if (u.getRole() == UserRole.CITOYEN) roleLabel = "Citoyen";
        else roleLabel = u.getRole().name();

        // Ajouter au model
        model.addAttribute("fullName", fullName);
        model.addAttribute("initials", initials);
        model.addAttribute("roleLabel", roleLabel);
    }
}
