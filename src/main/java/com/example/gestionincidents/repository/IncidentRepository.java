package com.example.gestionincidents.repository;

import com.example.gestionincidents.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByCategorie(CategorieIncident categorie);

    List<Incident> findByEtat(EtatIncident etat);

    List<Incident> findByQuartier(Quartier quartier);
}
