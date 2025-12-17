package com.example.gestionincidents.controller;

import com.example.gestionincidents.DTO.AdminDashboardDTO;
import com.example.gestionincidents.entity.*;
import com.example.gestionincidents.repository.IncidentRepository;
import com.example.gestionincidents.repository.RapportRepository;
import com.example.gestionincidents.repository.UtilisateurRepository;
import com.example.gestionincidents.service.AdminAnalyticsService;
import com.example.gestionincidents.service.AdminPdfReportService;
import com.example.gestionincidents.service.ConnectedUserInfoService;
import com.example.gestionincidents.service.IncidentWorkflowService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ConnectedUserInfoService connectedUserInfoService;
    private final UtilisateurRepository utilisateurRepository;
    private final IncidentRepository incidentRepository;
    private final AdminAnalyticsService adminAnalyticsService;
    private final AdminPdfReportService adminPdfReportService;
    private final RapportRepository rapportRepository;
    private final IncidentWorkflowService workflowService;

    public AdminController(ConnectedUserInfoService connectedUserInfoService,
                           UtilisateurRepository utilisateurRepository,
                           IncidentRepository incidentRepository, AdminAnalyticsService adminAnalyticsService, AdminPdfReportService adminPdfReportService, RapportRepository rapportRepository,
                           IncidentWorkflowService workflowService) {
        this.connectedUserInfoService = connectedUserInfoService;
        this.utilisateurRepository = utilisateurRepository;
        this.incidentRepository = incidentRepository;
        this.adminAnalyticsService = adminAnalyticsService;
        this.adminPdfReportService = adminPdfReportService;
        this.rapportRepository = rapportRepository;
        this.workflowService = workflowService;
    }

    @GetMapping
    public String adminHome(Model model, Authentication authentication) {
        connectedUserInfoService.addConnectedUserInfo(model, authentication);
        String email = authentication.getName();
        AdminDashboardDTO data = adminAnalyticsService.buildDashboard(email);
        model.addAttribute("data", data);

        model.addAttribute("quartierLabels", data.getQuartierLabels());
        model.addAttribute("quartierValues", data.getQuartierValues());

        model.addAttribute("monthLabels", data.getMonthLabels());
        model.addAttribute("monthValues", data.getMonthValues());
        Utilisateur admin = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Administrateur introuvable : " + email));

        List<Utilisateur> agentsList = (admin.getRole() == UserRole.SUPER_ADMIN)
                ? utilisateurRepository.findByRole(UserRole.AGENT)
                : utilisateurRepository.findAgentsByAdministrateur(admin.getId());

        model.addAttribute("agentsList", agentsList);

        Map<Long, Long> agentIncidentCounts = new HashMap<>();
        if (!agentsList.isEmpty()) {
            List<Incident> incs = incidentRepository.findByAgentAssigneIn(agentsList);

            agentIncidentCounts = incs.stream()
                    .filter(i -> i.getAgentAssigne() != null && i.getAgentAssigne().getId() != null)
                    .collect(Collectors.groupingBy(
                            i -> i.getAgentAssigne().getId(),
                            Collectors.counting()
                    ));
        }
        model.addAttribute("agentIncidentCounts", agentIncidentCounts);

        model.addAttribute("pageTitle", "Tableau de bord");
        model.addAttribute("activeMenu", "dashboard");
        return "admin-dashboard";
    }

    @GetMapping("/rapports/pdf")
    public ResponseEntity<byte[]> exportPdf(Authentication authentication) {

        String email = authentication.getName();

        // data (KPIs + lastIncidents)
        AdminDashboardDTO data = adminAnalyticsService.buildDashboard(email);

        // agents de cet admin
        Utilisateur admin = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Administrateur introuvable : " + email));

        List<Utilisateur> agents = (admin.getRole() == UserRole.SUPER_ADMIN)
                ? utilisateurRepository.findByRole(UserRole.AGENT)
                : utilisateurRepository.findAgentsByAdministrateur(admin.getId());

        // compter incidents par agent
        List<Incident> all = incidentRepository.findByAgentAssigneIn(agents);
        Map<Long, Long> nbParAgent = all.stream()
                .filter(i -> i.getAgentAssigne() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        i -> i.getAgentAssigne().getId(),
                        java.util.stream.Collectors.counting()
                ));

        byte[] pdf = adminPdfReportService.build(data, agents, nbParAgent);
        Rapport r = new Rapport();
        r.setAuteur(admin);
        r.setDateGeneration(java.time.LocalDate.now());


        r.setTexte("Rapport PDF généré. Total=" + data.getTotal()
                + ", Nouveaux=" + data.getNouveaux()
                + ", En résolution=" + data.getEnResolution()
                + ", Clôturés=" + data.getClotures());

        rapportRepository.save(r);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rapport-admin.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }


    // incidents du département + liste des agents de cet admin
    @GetMapping("/incidents")
    public String adminIncidents(Model model, Authentication authentication) {

        connectedUserInfoService.addConnectedUserInfo(model, authentication);

        String email = authentication.getName();
        Utilisateur admin = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Administrateur introuvable : " + email));

        Departement dep = admin.getDepartement();

        if (dep == null) {
            model.addAttribute("errorMessage", "Aucun département n’est associé à votre compte administrateur.");
            model.addAttribute("departement", "—");
            model.addAttribute("incidents", Collections.emptyList());
            model.addAttribute("agents", Collections.emptyList());
            return "admin-incidents";
        }

        // 1) Agents de CET admin
        List<Utilisateur> agents = utilisateurRepository.findAgentsByAdministrateur(admin.getId());
        model.addAttribute("agents", agents);

        // 2) Mapper Departement -> CategorieIncident
        CategorieIncident categorie;
        try {
            categorie = CategorieIncident.valueOf(dep.name());
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage",
                    "Impossible de faire correspondre le département " + dep + " avec une catégorie d’incident.");
            model.addAttribute("departement", dep);
            model.addAttribute("incidents", Collections.emptyList());
            return "admin-incidents";
        }

        // 3) Incidents de la catégorie
        List<Incident> incidents = incidentRepository.findByCategorieOrderByDateCreationDesc(categorie);

        model.addAttribute("departement", dep);
        model.addAttribute("incidents", incidents);
        model.addAttribute("pageTitle", "Incidents");
        model.addAttribute("activeMenu", "incidents");

        long total = incidents.size();
        long nouveaux = incidents.stream()
                .filter(i -> i.getEtat() == null || i.getEtat() == EtatIncident.NOUVEAU)
                .count();

        long enCours = incidents.stream()
                .filter(i -> i.getEtat() == EtatIncident.PRISE_EN_CHARGE
                        || i.getEtat() == EtatIncident.EN_RESOLUTION
                        || i.getEtat() == EtatIncident.EN_ATTENTE)
                .count();

        long resolus = incidents.stream()
                .filter(i -> i.getEtat() == EtatIncident.RESOLUE
                        || i.getEtat() == EtatIncident.CLOTURE)
                .count();

        model.addAttribute("countTotal", total);
        model.addAttribute("countNouveaux", nouveaux);
        model.addAttribute("countEnCours", enCours);
        model.addAttribute("countResolus", resolus);

        return "admin-incidents";
    }

    // Priorité
    @PostMapping("/incidents/{id}/priorite")
    public String updatePriorite(@PathVariable Long id,
                                 @RequestParam(name = "priorite", required = false) String priorite,
                                 RedirectAttributes ra) {

        try {
            Incident inc = incidentRepository.findById(id)
                    .orElseThrow(() -> new IllegalStateException("Incident introuvable : " + id));

            if (priorite == null || priorite.isBlank()) {
                inc.setPriorite(null);
            } else {
                inc.setPriorite(Priorite.valueOf(priorite));
            }

            incidentRepository.save(inc);
            ra.addFlashAttribute("successMessage", "Priorité mise à jour.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", "Priorité invalide.");
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/incidents";
    }

    // Assignation
    @PostMapping("/incidents/{incidentId}/assign-agent")
    public String assignAgent(@PathVariable Long incidentId,
                              @RequestParam(name = "agentId", required = false) Long agentId,
                              Authentication authentication,
                              RedirectAttributes ra) {

        try {
            String adminEmail = authentication.getName();

            if (agentId == null) {
                //  désassigner
                workflowService.desassignerIncident(incidentId, adminEmail);
                ra.addFlashAttribute("successMessage", "Incident désassigné.");
            } else {
                //  l’état à PRISE_EN_CHARGE
                workflowService.assignerIncident(incidentId, agentId, adminEmail);
                ra.addFlashAttribute("successMessage", "Agent assigné. Statut => PRISE_EN_CHARGE.");
            }

        } catch (SecurityException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", "Erreur: " + ex.getMessage());
        }

        return "redirect:/admin/incidents";
    }
}
