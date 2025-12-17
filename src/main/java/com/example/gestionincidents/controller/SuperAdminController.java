package com.example.gestionincidents.controller;

import com.example.gestionincidents.entity.Departement;
import com.example.gestionincidents.entity.Utilisateur;
import com.example.gestionincidents.repository.UtilisateurRepository;
import com.example.gestionincidents.service.AccountService;
import com.example.gestionincidents.service.ConnectedUserInfoService;
import com.example.gestionincidents.service.MailService;
import com.example.gestionincidents.service.UserAssignmentService;
import com.example.gestionincidents.web.AdminAgentForm;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.gestionincidents.DTO.SuperAdminDashboardDTO;
import com.example.gestionincidents.service.SuperAdminAnalyticsService;


import java.util.List;

@Controller
@RequestMapping("/superadmin")
public class SuperAdminController {

    private final AccountService accountService;
    private final MailService mailService;
    private final UtilisateurRepository utilisateurRepository;
    private final ConnectedUserInfoService connectedUserInfoService;
    private final UserAssignmentService userAssignmentService;
    private final SuperAdminAnalyticsService superAdminAnalyticsService;


    public SuperAdminController(AccountService accountService,
                                MailService mailService,
                                ConnectedUserInfoService connectedUserInfoService,
                                UtilisateurRepository utilisateurRepository,
                                UserAssignmentService userAssignmentService, SuperAdminAnalyticsService superAdminAnalyticsService) {
        this.accountService = accountService;
        this.mailService = mailService;
        this.utilisateurRepository = utilisateurRepository;
        this.connectedUserInfoService = connectedUserInfoService;
        this.userAssignmentService = userAssignmentService;
        this.superAdminAnalyticsService = superAdminAnalyticsService;
    }

    // 1) Dashboard super admin
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        SuperAdminDashboardDTO data = superAdminAnalyticsService.buildDashboard();
        model.addAttribute("data", data);
        connectedUserInfoService.addConnectedUserInfo(model, authentication);

        return "superadmin-dashboard";
    }

    // 2) Page "Créer un compte" (formulaire ADMIN / AGENT)
    @GetMapping("/create-user")
    public String showCreateUserForm(Model model,
                                     Authentication authentication,
                                     @RequestParam(value = "success", required = false) String success,
                                     @RequestParam(value = "error", required = false) String error) {

        connectedUserInfoService.addConnectedUserInfo(model, authentication);


        model.addAttribute("form", new AdminAgentForm());


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

        // Messages d'erreur
        if (error != null) {
            String msg = switch (error) {
                case "duplicateEmail" -> "Un compte existe déjà avec cet email.";
                case "creationFailed" -> "Une erreur est survenue lors de la création du compte.";
                case "missingDepartment" -> "Veuillez sélectionner un département pour l’agent municipal.";
                default -> null;
            };
            if (msg != null) {
                model.addAttribute("errorMessage", msg);
            }
        }

        return "superadmin-create-user";
    }

    // 3) Traitement du formulaire pour créer ADMIN ou AGENT
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
                Departement departement = Departement.valueOf(form.getDepartement());
                rawPassword = accountService.createAdminWithGeneratedPassword(
                        form.getNom(),
                        form.getPrenom(),
                        form.getEmail(),
                        form.getPhone(),
                        departement
                );
                roleLabel = "Administrateur";
                successCode = "adminCreated";

            } else if ("AGENT".equalsIgnoreCase(form.getRole())) {
                if (form.getDepartement() == null || form.getDepartement().isBlank()) {
                    return "redirect:/superadmin/create-user?error=missingDepartment";
                }
                Departement departement = Departement.valueOf(form.getDepartement());
                rawPassword = accountService.createAgentWithGeneratedPassword(
                        form.getNom(),
                        form.getPrenom(),
                        form.getEmail(),
                        form.getPhone(),
                        departement
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
    //  Page d'affectation Admin Agents
    @GetMapping("/affectations")
    public String showAffectationPage(Model model,
                                      Authentication authentication,
                                      @RequestParam(value = "success", required = false) String success,
                                      @RequestParam(value = "error", required = false) String error) {

        connectedUserInfoService.addConnectedUserInfo(model, authentication);
        List<Utilisateur> admins = userAssignmentService.getAllAdmins();
        List<Utilisateur> agents = userAssignmentService.getAllAgents();

        model.addAttribute("admins", admins);
        model.addAttribute("agents", agents);

        if (success != null) {
            model.addAttribute("successMessage", success);
        }
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }

        return "superadmin-affectations";
    }

    //  Traitement de l'affectation
    @PostMapping("/affectations")
    public String assignAgents(@RequestParam("adminId") Long adminId,
                               @RequestParam(name = "agentIds", required = false) List<Long> agentIds,
                               RedirectAttributes redirectAttributes) {

        if (agentIds == null || agentIds.isEmpty()) {
            redirectAttributes.addAttribute("error", "Veuillez sélectionner au moins un agent.");
            return "redirect:/superadmin/affectations";
        }

        try {
            userAssignmentService.assignAgentsToAdmin(adminId, agentIds);
            redirectAttributes.addAttribute("success", "Agents affectés avec succès à l'administrateur.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addAttribute("error", "Erreur lors de l'affectation : " + e.getMessage());
        }

        return "redirect:/superadmin/affectations";
    }
}
