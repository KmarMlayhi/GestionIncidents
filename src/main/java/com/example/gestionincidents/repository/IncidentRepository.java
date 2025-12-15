package com.example.gestionincidents.repository;

import com.example.gestionincidents.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    // Tous les incidents d'une catégorie
    List<Incident> findByCategorie(CategorieIncident categorie);

    // Tous les incidents d'une catégorie, triés du plus récent au plus ancien
    List<Incident> findByCategorieOrderByDateCreationDesc(CategorieIncident categorie);

    List<Incident> findByEtat(EtatIncident etat);

    List<Incident> findByQuartier(Quartier quartier);

    // Tous les incidents d'un citoyen
    List<Incident> findByCitoyen(Utilisateur citoyen);
}
