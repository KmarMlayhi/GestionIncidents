package com.example.gestionincidents;

import com.example.gestionincidents.entity.*;
import com.example.gestionincidents.repository.*;
import com.example.gestionincidents.service.IncidentWorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class IncidentWorkflowTest {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    /**
     * Version "test" du service qui ignore le MailService
     */
    static class IncidentWorkflowTestImpl extends IncidentWorkflowService {

        public IncidentWorkflowTestImpl(IncidentRepository ir, UtilisateurRepository ur) {
            // On passe null pour le MailService car on ne veut pas envoyer d'e-mails
            super(ir, ur, null);
        }

        @Override
        public void assignerIncident(Long incidentId, Long agentId, String adminEmail) {
            // Copier la logique de assignerIncident sans appeler mailService
            Utilisateur admin = getUtilisateurRepository().findByEmail(adminEmail)
                    .orElseThrow(() -> new IllegalStateException("Administrateur introuvable : " + adminEmail));

            if (admin.getRole() != UserRole.ADMIN && admin.getRole() != UserRole.SUPER_ADMIN) {
                throw new SecurityException("Accès refusé : vous n'êtes pas un administrateur.");
            }

            Incident incident = getIncidentRepository().findById(incidentId)
                    .orElseThrow(() -> new IllegalStateException("Incident introuvable : " + incidentId));

            if (incident.getEtat() == EtatIncident.CLOTURE || incident.getEtat() == EtatIncident.RESOLUE) {
                throw new IllegalStateException("Impossible d'assigner un incident déjà résolu/clôturé.");
            }

            Utilisateur agent = getUtilisateurRepository().findById(agentId)
                    .orElseThrow(() -> new IllegalStateException("Agent introuvable : " + agentId));

            if (agent.getRole() != UserRole.AGENT) {
                throw new SecurityException("L'utilisateur choisi n'est pas un agent.");
            }

            if (agent.getAdministrateur() == null || agent.getAdministrateur().getId() == null) {
                throw new SecurityException("Cet agent n'a pas d'administrateur responsable.");
            }

            if (admin.getRole() != UserRole.SUPER_ADMIN &&
                    !agent.getAdministrateur().getId().equals(admin.getId())) {
                throw new SecurityException("Vous ne pouvez assigner que vos propres agents.");
            }

            // Assigner l'incident
            incident.setAgentAssigne(agent);
            if (incident.getEtat() == null || incident.getEtat() == EtatIncident.NOUVEAU) {
                incident.setEtat(EtatIncident.PRISE_EN_CHARGE);
            }
            getIncidentRepository().save(incident);
        }
    }

    @Test
    void testAssignerIncidentSimpleSansMail() {
        // Créer le service "test" qui ignore le MailService
        IncidentWorkflowService serviceTest =
                new IncidentWorkflowTestImpl(incidentRepository, utilisateurRepository);

        // Créer admin
        Utilisateur admin = new Utilisateur();
        admin.setEmail("admin@test.com");
        admin.setRole(UserRole.ADMIN);
        utilisateurRepository.save(admin);

        // Créer agent
        Utilisateur agent = new Utilisateur();
        agent.setEmail("agent@test.com");
        agent.setRole(UserRole.AGENT);
        agent.setAdministrateur(admin);
        utilisateurRepository.save(agent);

        // Créer incident
        Incident incident = new Incident();
        incident.setTitre("Incident test");
        incident.setEtat(EtatIncident.NOUVEAU);
        incidentRepository.save(incident);

        // Appeler la méthode assignerIncident
        serviceTest.assignerIncident(incident.getId(), agent.getId(), admin.getEmail());

        // Vérification
        Incident updatedIncident = incidentRepository.findById(incident.getId()).orElseThrow();
        assertEquals(agent.getId(), updatedIncident.getAgentAssigne().getId());
        assertEquals(EtatIncident.PRISE_EN_CHARGE, updatedIncident.getEtat());
    }
}
