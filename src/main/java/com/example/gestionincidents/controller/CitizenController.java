package com.example.gestionincidents.controller;

import com.example.gestionincidents.entity.Utilisateur;
import com.example.gestionincidents.repository.UtilisateurRepository;
import com.example.gestionincidents.service.CitizenDashboardService;
import com.example.gestionincidents.service.ConnectedUserInfoService;
import com.example.gestionincidents.DTO.CitizenDashboardDTO;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/citoyen")
public class CitizenController {

    private final ConnectedUserInfoService connectedUserInfoService;
    private final UtilisateurRepository utilisateurRepository;
    private final CitizenDashboardService citizenDashboardService; //

    public CitizenController(ConnectedUserInfoService connectedUserInfoService,
                             UtilisateurRepository utilisateurRepository,
                             CitizenDashboardService citizenDashboardService) {
        this.connectedUserInfoService = connectedUserInfoService;
        this.utilisateurRepository = utilisateurRepository;
        this.citizenDashboardService = citizenDashboardService;
    }

    // Tableau de bord
    @GetMapping("/dashboard")
    public String citizenDashboard(Model model, Authentication authentication) {

        connectedUserInfoService.addConnectedUserInfo(model, authentication);

        CitizenDashboardDTO data = citizenDashboardService.buildDashboard(authentication.getName());
        model.addAttribute("data", data);

        model.addAttribute("catLabels", data.getByCategorie().keySet().stream().toList());
        model.addAttribute("catValues", data.getByCategorie().values().stream().toList());

        model.addAttribute("monthLabels", data.getByMonth().keySet().stream().toList());
        model.addAttribute("monthValues", data.getByMonth().values().stream().toList());

        return "citizen-dashboard";
    }

    // Page profil citoyen
    @GetMapping("/profil")
    public String citizenProfile(Model model, Authentication authentication) {

        // Infos pour la navbar (nom, initiales, rôle)
        connectedUserInfoService.addConnectedUserInfo(model, authentication);

        // Récupérer l’utilisateur connecté
        String email = authentication.getName();
        Utilisateur citoyen = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable : " + email));

        // Passer l’objet au template
        model.addAttribute("citoyen", citoyen);

        return "citizen-profile"; // => templates/citizen-profile.html
    }
}
