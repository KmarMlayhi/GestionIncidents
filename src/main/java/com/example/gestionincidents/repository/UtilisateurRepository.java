package com.example.gestionincidents.repository;

import com.example.gestionincidents.entity.UserRole;
import com.example.gestionincidents.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
    // Tous les utilisateurs d’un rôle donné
    List<Utilisateur> findByRole(UserRole role);

    // Tous les agents qui n’ont encore aucun admin responsable
    List<Utilisateur> findByRoleAndAdministrateurIsNull(UserRole role);

    @Query("""
   select u from Utilisateur u
   where u.role = com.example.gestionincidents.entity.UserRole.AGENT
     and u.administrateur.id = :adminId
   order by u.prenom, u.nom
""")
    List<Utilisateur> findAgentsByAdministrateur(@Param("adminId") Long adminId);

}
