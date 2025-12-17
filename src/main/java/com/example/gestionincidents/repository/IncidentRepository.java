package com.example.gestionincidents.repository;

import com.example.gestionincidents.entity.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;


import java.time.LocalDateTime;
import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    // Tous les incidents d'une catégorie
    List<Incident> findByCategorie(CategorieIncident categorie);

    // Tous les incidents d'une catégorie, triés du plus récent au plus ancien
    List<Incident> findByCategorieOrderByDateCreationDesc(CategorieIncident categorie);

    List<Incident> findByEtat(EtatIncident etat);

    List<Incident> findByQuartier(Quartier quartier);
    List<Incident> findByCitoyen(Utilisateur citoyen);

    // Tous les incidents d'un citoyen
    Page<Incident> findByCitoyen(Utilisateur citoyen, Pageable pageable);

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

    List<Incident> findTop5ByCitoyenOrderByDateCreationDesc(Utilisateur citoyen);

    // incidents pour une liste d'agents (périmètre admin)
    List<Incident> findByAgentAssigneIn(List<Utilisateur> agents);

    // Catégories (count)
    @Query("""
        select i.categorie, count(i)
        from Incident i
        where i.agentAssigne in :agents
        group by i.categorie
        order by count(i) desc
    """)
    List<Object[]> countByCategorieForAgents(@Param("agents") List<Utilisateur> agents);

    // Quartiers (count)
    @Query("""
        select q.nom, count(i)
        from Incident i
        join i.quartier q
        where i.agentAssigne in :agents
        group by q.nom
        order by count(i) desc
    """)
    List<Object[]> countByQuartierForAgents(@Param("agents") List<Utilisateur> agents);

    // Incidents par mois (count) sur une période (ex: 12 derniers mois)
    @Query("""
        select function('date_format', i.dateCreation, '%Y-%m'), count(i)
        from Incident i
        where i.agentAssigne in :agents
          and i.dateCreation is not null
          and i.dateCreation >= :from
        group by function('date_format', i.dateCreation, '%Y-%m')
        order by function('date_format', i.dateCreation, '%Y-%m') asc
    """)
    List<Object[]> countByMonthForAgents(@Param("agents") List<Utilisateur> agents,
                                         @Param("from") LocalDateTime from);
}
