package com.example.gestionincidents.repository;

import com.example.gestionincidents.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    @Query("""
        select i from Incident i
        where i.agentAssigne.id = :agentId
        order by
          case
            when i.priorite = com.example.gestionincidents.entity.Priorite.HAUTE then 3
            when i.priorite = com.example.gestionincidents.entity.Priorite.MOYENNE then 2
            when i.priorite = com.example.gestionincidents.entity.Priorite.BASSE then 1
            else 0
          end desc,
          i.dateCreation desc
    """)

    List<Incident> findAssignedToAgentOrdered(@Param("agentId") Long agentId);

    @Query("""
        select i from Incident i
        where i.id = :incidentId and i.agentAssigne.id = :agentId
    """)
    Incident findByIdAndAgent(@Param("incidentId") Long incidentId,
                              @Param("agentId") Long agentId);
}
