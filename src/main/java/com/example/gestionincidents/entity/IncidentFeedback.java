package com.example.gestionincidents.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class IncidentFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "incident_id")
    private Incident incident;

    @ManyToOne(optional = false)
    @JoinColumn(name = "citoyen_id")
    private Utilisateur citoyen;

    @Column(length = 1000, nullable = false)
    private String commentaire;

    // ✅ true si le citoyen a demandé la clôture
    private boolean cloturer;

    private LocalDateTime dateFeedback;

    // getters/setters

    public Incident getIncident() {
        return incident;
    }

    public Utilisateur getCitoyen() {
        return citoyen;
    }

    public void setCitoyen(Utilisateur citoyen) {
        this.citoyen = citoyen;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public boolean isCloturer() {
        return cloturer;
    }

    public void setCloturer(boolean cloturer) {
        this.cloturer = cloturer;
    }

    public LocalDateTime getDateFeedback() {
        return dateFeedback;
    }

    public void setDateFeedback(LocalDateTime dateFeedback) {
        this.dateFeedback = dateFeedback;
    }

    public void setIncident(Incident incident) {
        this.incident = incident;
    }
}
