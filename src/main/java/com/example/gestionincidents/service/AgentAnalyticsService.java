package com.example.gestionincidents.service;

import com.example.gestionincidents.DTO.AgentDashboardDTO;
import com.example.gestionincidents.entity.*;
import com.example.gestionincidents.repository.IncidentRepository;
import com.example.gestionincidents.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AgentAnalyticsService {

    private final UtilisateurRepository utilisateurRepository;
    private final IncidentRepository incidentRepository;

    public AgentAnalyticsService(UtilisateurRepository utilisateurRepository,
                                 IncidentRepository incidentRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.incidentRepository = incidentRepository;
    }

    @Transactional(readOnly = true)
    public AgentDashboardDTO buildDashboard(String agentEmail) {

        Utilisateur agent = utilisateurRepository.findByEmail(agentEmail)
                .orElseThrow(() -> new IllegalStateException("Agent introuvable: " + agentEmail));

        if (agent.getRole() != UserRole.AGENT) {
            throw new SecurityException("Accès refusé : rôle AGENT requis.");
        }

        // incidents assignés à cet agent (déjà triés par priorité puis date)
        List<Incident> assigned = incidentRepository.findAssignedToAgentOrdered(agent.getId());

        if (assigned.isEmpty()) {
            return withAdminInfo(agent, AgentDashboardDTO.empty());
        }

        long total = assigned.size();

        long nouveaux = assigned.stream().filter(i -> i.getEtat() == EtatIncident.NOUVEAU).count();
        long prise = assigned.stream().filter(i -> i.getEtat() == EtatIncident.PRISE_EN_CHARGE).count();
        long enRes = assigned.stream().filter(i -> i.getEtat() == EtatIncident.EN_RESOLUTION).count();
        long resolues = assigned.stream().filter(i -> i.getEtat() == EtatIncident.RESOLUE).count();
        long clotures = assigned.stream().filter(i -> i.getEtat() == EtatIncident.CLOTURE).count();

        long pHaute = assigned.stream().filter(i -> i.getPriorite() == Priorite.HAUTE).count();
        long pMoy = assigned.stream().filter(i -> i.getPriorite() == Priorite.MOYENNE).count();
        long pBasse = assigned.stream().filter(i -> i.getPriorite() == Priorite.BASSE).count();

        // “Derniers” (ici: top 8 dans l’ordre existant)
        List<Incident> last = assigned.stream().limit(8).toList();

        AgentDashboardDTO data = AgentDashboardDTO.builder()
                .totalAssignes(total)
                .nouveaux(nouveaux)
                .priseEnCharge(prise)
                .enResolution(enRes)
                .resolues(resolues)
                .clotures(clotures)
                .prioHaute(pHaute)
                .prioMoyenne(pMoy)
                .prioBasse(pBasse)
                .lastIncidents(last)
                .build();

        return withAdminInfo(agent, data);
    }

    private AgentDashboardDTO withAdminInfo(Utilisateur agent, AgentDashboardDTO data) {
        Utilisateur admin = agent.getAdministrateur();

        if (admin == null) {
            data.setAdmin(AgentDashboardDTO.AdminDTO.builder()
                    .nomComplet("Non affecté")
                    .email("—")
                    .phone("—")
                    .build());
            return data;
        }

        String nom = ((admin.getPrenom() == null ? "" : admin.getPrenom() + " ")
                + (admin.getNom() == null ? "" : admin.getNom())).trim();
        if (nom.isEmpty()) nom = "—";

        String email = (admin.getEmail() == null || admin.getEmail().isBlank()) ? "—" : admin.getEmail();


        String phone = (admin.getPhone() == null || admin.getPhone().isBlank()) ? "—" : admin.getPhone();

        data.setAdmin(AgentDashboardDTO.AdminDTO.builder()
                .nomComplet(nom)
                .email(email)
                .phone(phone)
                .build());

        return data;
    }
}
