package com.example.gestionincidents.service;

import com.example.gestionincidents.entity.UserRole;
import com.example.gestionincidents.entity.Utilisateur;
import com.example.gestionincidents.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserAssignmentService {

    private final UtilisateurRepository utilisateurRepository;

    public UserAssignmentService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    // Récupérer tous les admins
    public List<Utilisateur> getAllAdmins() {
        return utilisateurRepository.findByRole(UserRole.ADMIN);
    }

    //  Récupérer tous les agents sans admin (à affecter)
    public List<Utilisateur> getAgentsWithoutAdmin() {
        return utilisateurRepository.findByRoleAndAdministrateurIsNull(UserRole.AGENT);
    }

    //  Récupérer tous les agents
    public List<Utilisateur> getAllAgents() {
        return utilisateurRepository.findByRole(UserRole.AGENT);
    }

    //  Affecter une liste d’agents à un admin
    @Transactional
    public void assignAgentsToAdmin(Long adminId, List<Long> agentIds) {
        Utilisateur admin = utilisateurRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin introuvable : " + adminId));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("L'utilisateur choisi n'est pas un administrateur.");
        }

        List<Utilisateur> agents = utilisateurRepository.findAllById(agentIds);

        for (Utilisateur agent : agents) {
            if (agent.getRole() != UserRole.AGENT) {
                throw new IllegalArgumentException(
                        "L'utilisateur " + agent.getEmail() + " n'est pas un agent."
                );
            }
            agent.setAdministrateur(admin);
        }

        utilisateurRepository.saveAll(agents);
    }
}
