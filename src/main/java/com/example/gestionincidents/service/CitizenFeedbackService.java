package com.example.gestionincidents.service;

import com.example.gestionincidents.entity.*;
import com.example.gestionincidents.repository.IncidentFeedbackRepository;
import com.example.gestionincidents.repository.IncidentRepository;
import com.example.gestionincidents.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CitizenFeedbackService {

    private final IncidentRepository incidentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final IncidentFeedbackRepository feedbackRepository;
    private final MailService mailService;

    public CitizenFeedbackService(IncidentRepository incidentRepository,
                                  UtilisateurRepository utilisateurRepository,
                                  IncidentFeedbackRepository feedbackRepository, MailService mailService) {
        this.incidentRepository = incidentRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.feedbackRepository = feedbackRepository;
        this.mailService = mailService;
    }

    @Transactional
    public void envoyerFeedback(Long incidentId, String commentaire, boolean cloturer, String citoyenEmail) {

        Utilisateur citoyen = utilisateurRepository.findByEmail(citoyenEmail)
                .orElseThrow(() -> new IllegalStateException("Citoyen introuvable : " + citoyenEmail));

        if (citoyen.getRole() != UserRole.CITOYEN) {
            throw new SecurityException("Accès refusé : rôle CITOYEN requis.");
        }

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalStateException("Incident introuvable."));

        // seul le propriétaire
        if (incident.getCitoyen() == null || !incident.getCitoyen().getId().equals(citoyen.getId())) {
            throw new SecurityException("Accès refusé : cet incident ne vous appartient pas.");
        }

        //  feedback seulement si RESOLUE
        if (incident.getEtat() != EtatIncident.RESOLUE) {
            throw new IllegalStateException("Feedback autorisé uniquement quand l'incident est RESOLUE.");
        }

        //  commentaire obligatoire
        if (commentaire == null || commentaire.trim().isEmpty()) {
            throw new IllegalStateException("Commentaire obligatoire.");
        }

        // 1) enregistrer feedback (historique)
        IncidentFeedback fb = new IncidentFeedback();
        fb.setIncident(incident);
        fb.setCitoyen(citoyen);
        fb.setCommentaire(commentaire.trim());
        fb.setCloturer(cloturer);
        fb.setDateFeedback(LocalDateTime.now());
        feedbackRepository.save(fb);
        // notifier l'agent assigné
        try {
            Utilisateur agent = incident.getAgentAssigne();
            if (agent != null && agent.getEmail() != null && !agent.getEmail().isBlank()) {

                String agentName = agent.getPrenom() + " " + agent.getNom();
                String citoyenName = citoyen.getPrenom() + " " + citoyen.getNom();

                if (cloturer) {
                    mailService.sendFeedbackClotureToAgent(
                            agent.getEmail(),
                            agentName,
                            incident.getId(),
                            incident.getTitre(),
                            commentaire.trim(),
                            citoyenName
                    );
                } else {
                    mailService.sendFeedbackNonClotureToAgent(
                            agent.getEmail(),
                            agentName,
                            incident.getId(),
                            incident.getTitre(),
                            commentaire.trim(),
                            citoyenName
                    );
                }
            }
        } catch (Exception ignored) {}

        // 2) clôture seulement si demandé
        if (cloturer) {
            incident.setEtat(EtatIncident.CLOTURE);
            incident.setDateCloture(LocalDateTime.now());
            incidentRepository.save(incident);
        }
        // sinon: reste RESOLUE
    }
}
