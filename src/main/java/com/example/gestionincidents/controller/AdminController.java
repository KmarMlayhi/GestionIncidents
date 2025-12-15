package com.example.gestionincidents.controller;

import com.example.gestionincidents.entity.*;
import com.example.gestionincidents.repository.IncidentRepository;
import com.example.gestionincidents.repository.UtilisateurRepository;
import com.example.gestionincidents.service.ConnectedUserInfoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ConnectedUserInfoService connectedUserInfoService;
    private final UtilisateurRepository utilisateurRepository;
    private final IncidentRepository incidentRepository;

    public AdminController(ConnectedUserInfoService connectedUserInfoService,
                           UtilisateurRepository utilisateurRepository,
                           IncidentRepository incidentRepository) {
        this.connectedUserInfoService = connectedUserInfoService;
        this.utilisateurRepository = utilisateurRepository;
        this.incidentRepository = incidentRepository;
    }

    // Dashboard simple
    @GetMapping
    public String adminHome(Model model, Authentication authentication) {
        connectedUserInfoService.addConnectedUserInfo(model, authentication);
        model.addAttribute("pageTitle", "Tableau de bord");
        model.addAttribute("activeMenu", "dashboard");

        return "admin-dashboard";
    }

    // ✅ Page : incidents du département de l’admin
    @GetMapping("/incidents")
    public String adminIncidents(Model model, Authentication authentication) {

        // Infos pour le header (nom, rôle, initials…)
        connectedUserInfoService.addConnectedUserInfo(model, authentication);

        // 1) Récupérer l'admin connecté
        String email = authentication.getName();
        Utilisateur admin = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Administrateur introuvable : " + email));

        Departement dep = admin.getDepartement();

        if (dep == null) {
            model.addAttribute("errorMessage",
                    "Aucun département n’est associé à votre compte administrateur.");
            model.addAttribute("incidents", Collections.emptyList());
            return "admin-incidents";
        }

        // 2) Mapper Departement -> CategorieIncident
        CategorieIncident categorie;
        try {
            categorie = CategorieIncident.valueOf(dep.name());
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage",
                    "Impossible de faire correspondre le département " + dep +
                            " avec une catégorie d’incident.");
            model.addAttribute("incidents", Collections.emptyList());
            return "admin-incidents";
        }

        // 3) Récupérer TOUS les incidents de cette catégorie (assignés ou non)
        List<Incident> incidents =
                incidentRepository.findByCategorieOrderByDateCreationDesc(categorie);

        model.addAttribute("departement", dep);
        model.addAttribute("incidents", incidents);
        model.addAttribute("pageTitle", "Incidents");
        model.addAttribute("activeMenu", "incidents");

        return "admin-incidents";
    }
}
