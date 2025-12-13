package com.example.gestionincidents.controller;

import com.example.gestionincidents.service.AccountService;
import com.example.gestionincidents.service.MailService;
import com.example.gestionincidents.web.AdminAgentForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/superadmin")
public class SuperAdminController {

    private final AccountService accountService;
    private final MailService mailService;

    public SuperAdminController(AccountService accountService,
                                MailService mailService) {
        this.accountService = accountService;
        this.mailService = mailService;
    }

    // ✅ Affichage du tableau de bord + formulaire de création
    @GetMapping("/dashboard")
    public String dashboard(Model model,
                            @RequestParam(value = "success", required = false) String success,
                            @RequestParam(value = "error", required = false) String error) {

        model.addAttribute("form", new AdminAgentForm());

        // Messages de succès (codes -> texte)
        if (success != null) {
            String msg = switch (success) {
                case "adminCreated" -> "Administrateur créé avec succès.";
                case "agentCreated" -> "Agent municipal créé avec succès.";
                default -> null;
            };
            if (msg != null) {
                model.addAttribute("successMessage", msg);
            }
        }

        // Messages d'erreur (codes -> texte)
        if (error != null) {
            String msg = switch (error) {
                case "duplicateEmail" -> "Un compte existe déjà avec cet email.";
                case "creationFailed" -> "Une erreur est survenue lors de la création du compte.";
                default -> null;
            };
            if (msg != null) {
                model.addAttribute("errorMessage", msg);
            }
        }

        return "superadmin-dashboard";
    }

    // Traitement du formulaire pour créer ADMIN ou AGENT
    @PostMapping("/create-user")
    public String createUser(@ModelAttribute("form") AdminAgentForm form) {

        // 1) Vérifier si l'utilisateur existe déjà
        if (accountService.userExists(form.getEmail())) {
            return "redirect:/superadmin/dashboard?error=duplicateEmail";
        }

        try {
            // Nom complet pour l’email
            String fullName = (form.getPrenom() != null ? form.getPrenom() + " " : "")
                    + (form.getNom() != null ? form.getNom() : "");

            String rawPassword;  // mot de passe généré
            String roleLabel;    // "Administrateur" ou "Agent municipal"
            String successCode;  // "adminCreated" ou "agentCreated"

            // 2) Choix du rôle : ADMIN ou AGENT
            if ("ADMIN".equalsIgnoreCase(form.getRole())) {

                // crée l’ADMIN + génère un mdp + le renvoie
                rawPassword = accountService.createAdminWithGeneratedPassword(
                        form.getNom(),
                        form.getPrenom(),
                        form.getEmail(),
                        form.getPhone()
                );
                roleLabel = "Administrateur";
                successCode = "adminCreated";

            } else if ("AGENT".equalsIgnoreCase(form.getRole())) {

                // crée l’AGENT + génère un mdp + le renvoie
                rawPassword = accountService.createAgentWithGeneratedPassword(
                        form.getNom(),
                        form.getPrenom(),
                        form.getEmail(),
                        form.getPhone()
                );
                roleLabel = "Agent municipal";
                successCode = "agentCreated";

            } else {
                // rôle inconnu
                return "redirect:/superadmin/dashboard?error=creationFailed";
            }

            //  Envoi de l’email avec identifiant + mot de passe provisoire
            try {
                mailService.sendStaffAccountEmail(
                        form.getEmail(),   // destinataire
                        fullName.trim(),   // nom complet
                        roleLabel,         // "Administrateur" / "Agent municipal"
                        rawPassword        // mot de passe généré en clair
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Redirection avec un code de succès
            return "redirect:/superadmin/dashboard?success=" + successCode;

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/superadmin/dashboard?error=creationFailed";
        }
    }
}
