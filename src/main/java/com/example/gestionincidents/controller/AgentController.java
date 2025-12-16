package com.example.gestionincidents.controller;

import com.example.gestionincidents.entity.Incident;
import com.example.gestionincidents.entity.IncidentFeedback;
import com.example.gestionincidents.service.AgentIncidentService;
import com.example.gestionincidents.service.ConnectedUserInfoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/agent")
public class AgentController {

    private final ConnectedUserInfoService connectedUserInfoService;
    private final AgentIncidentService agentIncidentService;

    public AgentController(ConnectedUserInfoService connectedUserInfoService,
                           AgentIncidentService agentIncidentService) {
        this.connectedUserInfoService = connectedUserInfoService;
        this.agentIncidentService = agentIncidentService;
    }

    @GetMapping("/dashboard")
    public String agentDashboard(Model model, Authentication authentication) {
        connectedUserInfoService.addConnectedUserInfo(model, authentication);
        return "agent-dashboard";
    }

    @GetMapping("/incidents")
    public String agentIncidents(Model model, Authentication authentication) {
        connectedUserInfoService.addConnectedUserInfo(model, authentication);

        String email = authentication.getName();
        List<Incident> incidents = agentIncidentService.getIncidentsAssignes(email);
        // ✅ ajouter lastFeedback dans le model
        Map<Long, IncidentFeedback> lastFeedback = new HashMap<>();
        for (Incident inc : incidents) {
            IncidentFeedback fb = agentIncidentService.getDernierFeedback(inc.getId(), email);
            if (fb != null) lastFeedback.put(inc.getId(), fb);
        }

        model.addAttribute("incidents", incidents);
        model.addAttribute("lastFeedback", lastFeedback); // ✅ dispo dans agent-incidents.html


        model.addAttribute("incidents", incidents);
        model.addAttribute("pageTitle", "Mes incidents");
        model.addAttribute("activeMenu", "incidents");

        return "agent-incidents";
    }

    @PostMapping("/incidents/{id}/en-resolution")
    public String passerEnResolution(@PathVariable Long id,
                                     Authentication authentication,
                                     RedirectAttributes ra) {
        try {
            agentIncidentService.passerEnResolution(id, authentication.getName()); // ✅
            ra.addFlashAttribute("successMessage", "Incident passé en résolution.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/agent/incidents";
    }


    // ✅ EN_RESOLUTION -> RESOLUE avec upload photo
    @PostMapping("/incidents/{id}/resolu")
    public String marquerResolu(@PathVariable Long id,
                                @RequestParam("photo") MultipartFile photo,
                                Authentication authentication,
                                RedirectAttributes ra) {
        try {
            agentIncidentService.marquerResolu(id, photo, authentication.getName());
            ra.addFlashAttribute("successMessage", "Incident marqué comme résolu (preuve enregistrée).");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/agent/incidents";
    }
    //repasser du RESOLUE en EN RESOLUTION ( si feedback negatif )
    @PostMapping("/incidents/{id}/reprendre")
    public String reprendre(@PathVariable Long id,
                            Authentication authentication,
                            RedirectAttributes ra) {
        try {
            agentIncidentService.reprendreEnResolution(id, authentication.getName());
            ra.addFlashAttribute("successMessage", "Incident repris : retour en 'EN_RESOLUTION'.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/agent/incidents";
    }

}
