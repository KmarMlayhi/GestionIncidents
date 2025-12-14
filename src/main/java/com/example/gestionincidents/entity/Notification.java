package com.example.gestionincidents.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @Column(length = 1000)
    private String message;

    private LocalDateTime dateEnvoi;

    private boolean lu;

    @Enumerated(EnumType.STRING)
    private EventType typeEvenement;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur destinataire;

    @ManyToOne
    @JoinColumn(name = "incident_id")
    private Incident incident;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getDateEnvoi() {
        return dateEnvoi;
    }

    public void setDateEnvoi(LocalDateTime dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public boolean isLu() {
        return lu;
    }

    public void setLu(boolean lu) {
        this.lu = lu;
    }

    public EventType getTypeEvenement() {
        return typeEvenement;
    }

    public void setTypeEvenement(EventType typeEvenement) {
        this.typeEvenement = typeEvenement;
    }

    public Utilisateur getDestinataire() {
        return destinataire;
    }

    public void setDestinataire(Utilisateur destinataire) {
        this.destinataire = destinataire;
    }

    public Incident getIncident() {
        return incident;
    }

    public void setIncident(Incident incident) {
        this.incident = incident;
    }
}

