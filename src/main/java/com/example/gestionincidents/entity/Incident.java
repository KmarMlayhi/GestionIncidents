package com.example.gestionincidents.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private CategorieIncident categorie;

    @Enumerated(EnumType.STRING)
    private EtatIncident etat;

    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    private Priorite priorite;

    private LocalDateTime dateSignalement;
    private LocalDateTime dateCreation;
    private LocalDateTime dateCloture;

    // Relations

    // citoyen qui a signalé l'incident
    @ManyToOne
    @JoinColumn(name = "citoyen_id")
    private Utilisateur citoyen;

    // agent municipal assigné
    @ManyToOne
    @JoinColumn(name = "agent_id")
    private Utilisateur agentAssigne;

    @ManyToOne
    @JoinColumn(name = "quartier_id")
    private Quartier quartier;


    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL)
    @OrderBy("dateUpload DESC")
    private List<Photo> photos = new ArrayList<>();

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL)
    private List<Notification> notifications = new ArrayList<>();

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CategorieIncident getCategorie() {
        return categorie;
    }

    public void setCategorie(CategorieIncident categorie) {
        this.categorie = categorie;
    }

    public LocalDateTime getDateCloture() {
        return dateCloture;
    }

    public void setDateCloture(LocalDateTime dateCloture) {
        this.dateCloture = dateCloture;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    public Quartier getQuartier() {
        return quartier;
    }

    public void setQuartier(Quartier quartier) {
        this.quartier = quartier;
    }

    public Utilisateur getAgentAssigne() {
        return agentAssigne;
    }

    public void setAgentAssigne(Utilisateur agentAssigne) {
        this.agentAssigne = agentAssigne;
    }

    public Utilisateur getCitoyen() {
        return citoyen;
    }

    public void setCitoyen(Utilisateur citoyen) {
        this.citoyen = citoyen;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateSignalement() {
        return dateSignalement;
    }

    public void setDateSignalement(LocalDateTime dateSignalement) {
        this.dateSignalement = dateSignalement;
    }

    public Priorite getPriorite() {
        return priorite;
    }

    public void setPriorite(Priorite priorite) {
        this.priorite = priorite;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public EtatIncident getEtat() {
        return etat;
    }

    public void setEtat(EtatIncident etat) {
        this.etat = etat;
    }
}

