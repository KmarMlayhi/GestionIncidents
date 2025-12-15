package com.example.gestionincidents.service;

import com.example.gestionincidents.entity.*;
import com.example.gestionincidents.repository.IncidentRepository;
import com.example.gestionincidents.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentWorkflowService {

    private final IncidentRepository incidentRepository;
    private final UtilisateurRepository utilisateurRepository;

    public IncidentWorkflowService(IncidentRepository incidentRepository,
                                   UtilisateurRepository utilisateurRepository) {
        this.incidentRepository = incidentRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Transactional
    public void assignerIncident(Long incidentId, Long agentId, String adminEmail) {

        // 1) Charger l'admin connecté
        Utilisateur admin = utilisateurRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalStateException("Administrateur introuvable : " + adminEmail));

        if (admin.getRole() != UserRole.ADMIN && admin.getRole() != UserRole.SUPER_ADMIN) {
            throw new SecurityException("Accès refusé : vous n'êtes pas un administrateur.");
        }

        // 2) Charger incident
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalStateException("Incident introuvable : " + incidentId));

        // interdit si déjà clôturé / résolu (tu peux ajuster)
        if (incident.getEtat() == EtatIncident.CLOTURE || incident.getEtat() == EtatIncident.RESOLUE) {
            throw new IllegalStateException("Impossible d'assigner un incident déjà résolu/clôturé.");
        }

        // 3) Charger agent
        Utilisateur agent = utilisateurRepository.findById(agentId)
                .orElseThrow(() -> new IllegalStateException("Agent introuvable : " + agentId));

        if (agent.getRole() != UserRole.AGENT) {
            throw new SecurityException("L'utilisateur choisi n'est pas un agent.");
        }

        // 4) Vérifier que l'agent appartient à cet admin
        // (agent.administrateur doit être l'admin)
        if (agent.getAdministrateur() == null || agent.getAdministrateur().getId() == null) {
            throw new SecurityException("Cet agent n'a pas d'administrateur responsable.");
        }

        // ⚠️ si SUPER_ADMIN : tu peux autoriser tout (au choix)
        if (admin.getRole() != UserRole.SUPER_ADMIN) {
            if (!agent.getAdministrateur().getId().equals(admin.getId())) {
                throw new SecurityException("Vous ne pouvez assigner que vos propres agents.");
            }
        }

        // 5) Assigner
        incident.setAgentAssigne(agent);

        // ✅ Transition automatique : NOUVEAU -> PRISE_EN_CHARGE
        if (incident.getEtat() == null || incident.getEtat() == EtatIncident.NOUVEAU) {
            incident.setEtat(EtatIncident.PRISE_EN_CHARGE);
        }

        incidentRepository.save(incident);
    }

    @Transactional
    public void desassignerIncident(Long incidentId, String adminEmail) {

        Utilisateur admin = utilisateurRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalStateException("Administrateur introuvable : " + adminEmail));

        if (admin.getRole() != UserRole.ADMIN && admin.getRole() != UserRole.SUPER_ADMIN) {
            throw new SecurityException("Accès refusé : vous n'êtes pas un administrateur.");
        }

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalStateException("Incident introuvable : " + incidentId));

        // Option : empêcher désassignation si clôturé
        if (incident.getEtat() == EtatIncident.CLOTURE) {
            throw new IllegalStateException("Impossible de désassigner un incident clôturé.");
        }

        // Vérif simple : si admin normal, incident doit être dans son périmètre (département)
        // (si tu veux strict, on peut vérifier categorie vs departement de l'admin)
        // Ici on laisse simple.

        incident.setAgentAssigne(null);
        incidentRepository.save(incident);
    }
}
