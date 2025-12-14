package com.example.gestionincidents.controller;

import com.example.gestionincidents.entity.UserRole;
import com.example.gestionincidents.entity.Utilisateur;
import com.example.gestionincidents.repository.UtilisateurRepository;
import com.example.gestionincidents.service.AccountService;
import com.example.gestionincidents.service.MailService;
import com.example.gestionincidents.web.AdminAgentForm;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/superadmin")
public class SuperAdminController {

    private final AccountService accountService;
    private final MailService mailService;
    private final UtilisateurRepository utilisateurRepository;

    public SuperAdminController(AccountService accountService,
                                MailService mailService,
                                UtilisateurRepository utilisateurRepository) {
        this.accountService = accountService;
        this.mailService = mailService;
        this.utilisateurRepository = utilisateurRepository;
    }

    /** Ajoute fullName / initials / roleLabel pour le super admin connecté */
    private void addConnectedUserInfo(Model model, Authentication authentication) {
        String email = authentication.getName();

        // On essaie de charger l'utilisateur métier
        Utilisateur u = utilisateurRepository.findByEmail(email)
                .orElseGet(() -> {
                    // S'il n'existe pas encore dans la table utilisateur,
                    // on le crée "à la volée" avec un rôle basé sur ses authorities
                    Utilisateur nouveau = new Utilisateur();
                    nouveau.setEmail(email);

                    // Petit mapping authorities -> UserRole
                    UserRole role = UserRole.CITOYEN; // valeur par défaut
                    String authority = authentication.getAuthorities().stream()
                            .findFirst()
                            .map(a -> a.getAuthority())      // "ROLE_SUPER_ADMIN"
                            .orElse("ROLE_CITOYEN");

                    if (authority.equals("ROLE_SUPER_ADMIN")) role = UserRole.SUPER_ADMIN;
                    else if (authority.equals("ROLE_ADMIN")) role = UserRole.ADMIN;
                    else if (authority.equals("ROLE_AGENT")) role = UserRole.AGENT;

                    nouveau.setRole(role);

                    nouveau.setNom("Super Admin");   // pour le cas superadmin, ça ira
                    nouveau.setPrenom(null);

                    return utilisateurRepository.save(nouveau);
                });

        // Ensuite, le reste de ton code (fullName, initials, roleLabel) reste pareil
        String fullName = (u.getPrenom() != null ? u.getPrenom() + " " : "")
                + (u.getNom() != null ? u.getNom() : "");

        String initials = "";
        if (u.getPrenom() != null && !u.getPrenom().isEmpty()) {
            initials += u.getPrenom().charAt(0);
        }
        if (u.getNom() != null && !u.getNom().isEmpty()) {
            initials += u.getNom().charAt(0);
        }
        initials = initials.toUpperCase();

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

        model.addAttribute("fullName", fullName);
        model.addAttribute("initials", initials);
        model.addAttribute("roleLabel", roleLabel);
    }


    // 🟦 1) Dashboard super admin (pas de formulaire ici)
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {

        addConnectedUserInfo(model, authentication);

        return "superadmin-dashboard";   // templates/superadmin-dashboard.html
    }

    // 🟦 2) Page "Créer un compte" (formulaire ADMIN / AGENT)
    @GetMapping("/create-user")
    public String showCreateUserForm(Model model,
                                     Authentication authentication,
                                     @RequestParam(value = "success", required = false) String success,
                                     @RequestParam(value = "error", required = false) String error) {

        addConnectedUserInfo(model, authentication);

        // Objet formulaire
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

        return "superadmin-create-user"; // templates/superadmin-create-user.html
    }

    // 🟦 3) Traitement du formulaire pour créer ADMIN ou AGENT
    @PostMapping("/create-user")
    public String createUser(@ModelAttribute("form") AdminAgentForm form) {

        // 1) Vérifier si l'utilisateur existe déjà
        if (accountService.userExists(form.getEmail())) {
            return "redirect:/superadmin/create-user?error=duplicateEmail";
        }

        try {
            String fullName = (form.getPrenom() != null ? form.getPrenom() + " " : "")
                    + (form.getNom() != null ? form.getNom() : "");

            String rawPassword;
            String roleLabel;
            String successCode;

            // 2) Choix du rôle : ADMIN ou AGENT
            if ("ADMIN".equalsIgnoreCase(form.getRole())) {

                rawPassword = accountService.createAdminWithGeneratedPassword(
                        form.getNom(),
                        form.getPrenom(),
                        form.getEmail(),
                        form.getPhone()
                );
                roleLabel = "Administrateur";
                successCode = "adminCreated";

            } else if ("AGENT".equalsIgnoreCase(form.getRole())) {

                rawPassword = accountService.createAgentWithGeneratedPassword(
                        form.getNom(),
                        form.getPrenom(),
                        form.getEmail(),
                        form.getPhone()
                );
                roleLabel = "Agent municipal";
                successCode = "agentCreated";

            } else {
                return "redirect:/superadmin/create-user?error=creationFailed";
            }

            // 3) Envoi de l’email (identifiant + mot de passe provisoire)
            try {
                mailService.sendStaffAccountEmail(
                        form.getEmail(),
                        fullName.trim(),
                        roleLabel,
                        rawPassword
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            return "redirect:/superadmin/create-user?success=" + successCode;

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/superadmin/create-user?error=creationFailed";
        }
    }
}
