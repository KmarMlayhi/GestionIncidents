package com.example.gestionincidents.web;

import com.example.gestionincidents.entity.CategorieIncident;
import com.example.gestionincidents.entity.Priorite;

public class IncidentForm {

    private String titre;
    private String description;
    private CategorieIncident categorie;
    private Priorite priorite;

    // géolocalisation
    private String adresse;
    private Double latitude;
    private Double longitude;
    private String quartierNom;


    // Getters / setters
    public String getQuartierNom() {
        return quartierNom;
    }

    public void setQuartierNom(String quartierNom) {
        this.quartierNom = quartierNom;
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

    public Priorite getPriorite() {
        return priorite;
    }

    public void setPriorite(Priorite priorite) {
        this.priorite = priorite;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
