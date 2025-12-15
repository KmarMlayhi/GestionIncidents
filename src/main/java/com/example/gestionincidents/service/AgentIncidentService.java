package com.example.gestionincidents.service;

import com.example.gestionincidents.entity.*;
import com.example.gestionincidents.repository.IncidentRepository;
import com.example.gestionincidents.repository.PhotoRepository;
import com.example.gestionincidents.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AgentIncidentService {

    private final IncidentRepository incidentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PhotoRepository photoRepository;

    public AgentIncidentService(IncidentRepository incidentRepository,
                                UtilisateurRepository utilisateurRepository, PhotoRepository photoRepository) {
        this.incidentRepository = incidentRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.photoRepository = photoRepository;
    }

    @Transactional(readOnly = true)
    public List<Incident> getIncidentsAssignes(String agentEmail) {
        Utilisateur agent = utilisateurRepository.findByEmail(agentEmail)
                .orElseThrow(() -> new IllegalStateException("Agent introuvable : " + agentEmail));

        if (agent.getRole() != UserRole.AGENT) {
            throw new SecurityException("Accès refusé : rôle AGENT requis.");
        }

        return incidentRepository.findAssignedToAgentOrdered(agent.getId());
    }

    @Transactional
    public void marquerResolu(Long incidentId, MultipartFile photo, String agentEmail) {
        Utilisateur agent = getAgentOrThrow(agentEmail);

        Incident incident = incidentRepository.findByIdAndAgent(incidentId, agent.getId());
        if (incident == null) throw new SecurityException("Incident non assigné à cet agent.");

        EtatIncident etat = incident.getEtat();
        if (etat == null) etat = EtatIncident.NOUVEAU;

        if (etat == EtatIncident.CLOTURE) {
            throw new IllegalStateException("Impossible : incident déjà clôturé.");
        }

        if (etat != EtatIncident.EN_RESOLUTION) {
            throw new IllegalStateException("Transition refusée : l'incident doit être 'EN_RESOLUTION'.");
        }

        // ✅ photo obligatoire
        if (photo == null || photo.isEmpty()) {
            throw new IllegalStateException("Veuillez ajouter une photo de preuve avant de passer à 'RESOLUE'.");
        }

        String contentType = photo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalStateException("Format invalide : veuillez uploader une image.");
        }

        // ✅ Enregistrer le fichier dans static/uploads/incidents (projet de classe)
        Path uploadDir = Paths.get("src", "main", "resources", "static", "uploads", "incidents");
        try {
            Files.createDirectories(uploadDir);

            String original = photo.getOriginalFilename();
            String ext = (original != null && original.contains("."))
                    ? original.substring(original.lastIndexOf("."))
                    : ".jpg";

            String fileName = UUID.randomUUID() + ext;
            Path target = uploadDir.resolve(fileName);

            Files.copy(photo.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            // ✅ Insert dans la table Photo
            Photo p = new Photo();
            p.setNomPhoto(fileName);
            p.setType(contentType);
            p.setChemin(uploadDir.toString());     // ex: src/main/resources/static/uploads/incidents
            p.setDateUpload(LocalDateTime.now());
            p.setIncident(incident);

            photoRepository.save(p);

            // (optionnel mais propre: garder la liste sync en mémoire)
            incident.getPhotos().add(p);

            // ✅ Changer l'état
            incident.setEtat(EtatIncident.RESOLUE);
            incidentRepository.save(incident);

        } catch (IOException e) {
            throw new IllegalStateException("Erreur lors de l'enregistrement de la photo.");
        }
    }

    @Transactional
    public void passerEnResolution(Long incidentId, String agentEmail) {
        Utilisateur agent = getAgentOrThrow(agentEmail);

        Incident incident = incidentRepository.findByIdAndAgent(incidentId, agent.getId());
        if (incident == null) throw new SecurityException("Incident non assigné à cet agent.");

        EtatIncident etat = incident.getEtat();
        if (etat == null) etat = EtatIncident.NOUVEAU;

        if (etat == EtatIncident.CLOTURE || etat == EtatIncident.RESOLUE) {
            throw new IllegalStateException("Impossible : incident déjà résolu/clôturé.");
        }

        // ✅ autorisé seulement si PRISE_EN_CHARGE
        if (etat != EtatIncident.PRISE_EN_CHARGE) {
            throw new IllegalStateException("Transition refusée : l'incident doit être 'PRISE_EN_CHARGE'.");
        }

        incident.setEtat(EtatIncident.EN_RESOLUTION);
        incidentRepository.save(incident);
    }

    private Utilisateur getAgentOrThrow(String email) {
        Utilisateur agent = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Agent introuvable : " + email));
        if (agent.getRole() != UserRole.AGENT) {
            throw new SecurityException("Accès refusé : rôle AGENT requis.");
        }
        return agent;
    }
}
