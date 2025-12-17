package com.example.gestionincidents.service;

import com.example.gestionincidents.DTO.SuperAdminDashboardDTO;
import com.example.gestionincidents.entity.Departement;
import com.example.gestionincidents.entity.UserRole;
import com.example.gestionincidents.entity.Utilisateur;
import com.example.gestionincidents.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SuperAdminAnalyticsService {

    private final UtilisateurRepository utilisateurRepository;

    public SuperAdminAnalyticsService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Transactional(readOnly = true)
    public SuperAdminDashboardDTO buildDashboard() {

        // Admins = rôle ADMIN (si tu veux inclure SUPER_ADMIN aussi, dis-moi)
        List<Utilisateur> admins = utilisateurRepository.findByRole(UserRole.ADMIN);
        List<Utilisateur> agents = utilisateurRepository.findByRole(UserRole.AGENT);

        // Group agents by adminId (évite de faire findAgentsByAdministrateur dans une boucle)
        Map<Long, List<Utilisateur>> agentsByAdminId = agents.stream()
                .filter(a -> a.getAdministrateur() != null)
                .collect(Collectors.groupingBy(a -> a.getAdministrateur().getId()));

        List<SuperAdminDashboardDTO.DepartementBlock> blocks = new ArrayList<>();

        for (Departement dep : Departement.values()) {

            List<Utilisateur> adminsDep = admins.stream()
                    .filter(a -> a.getDepartement() == dep)
                    .sorted(Comparator.comparing(Utilisateur::getPrenom, Comparator.nullsLast(String::compareToIgnoreCase))
                            .thenComparing(Utilisateur::getNom, Comparator.nullsLast(String::compareToIgnoreCase)))
                    .toList();

            long nbAgentsDep = agents.stream()
                    .filter(a -> a.getDepartement() == dep)
                    .count();

            List<SuperAdminDashboardDTO.AdminBlock> adminBlocks = new ArrayList<>();

            for (Utilisateur ad : adminsDep) {
                List<Utilisateur> listAgents = agentsByAdminId.getOrDefault(ad.getId(), List.of());

                List<SuperAdminDashboardDTO.PersonVM> agentsVm = listAgents.stream()
                        .sorted(Comparator.comparing(Utilisateur::getPrenom, Comparator.nullsLast(String::compareToIgnoreCase))
                                .thenComparing(Utilisateur::getNom, Comparator.nullsLast(String::compareToIgnoreCase)))
                        .map(this::toPerson)
                        .toList();

                adminBlocks.add(SuperAdminDashboardDTO.AdminBlock.builder()
                        .admin(toPerson(ad))
                        .nbAgents(agentsVm.size())
                        .agents(agentsVm)
                        .build());
            }

            blocks.add(SuperAdminDashboardDTO.DepartementBlock.builder()
                    .departement(dep)
                    .nbAdmins(adminBlocks.size())
                    .nbAgents(nbAgentsDep)
                    .admins(adminBlocks)
                    .build());
        }

        return SuperAdminDashboardDTO.builder()
                .totalAdmins(admins.size())
                .totalAgents(agents.size())
                .departements(blocks)
                .build();
    }

    private SuperAdminDashboardDTO.PersonVM toPerson(Utilisateur u) {
        String nomComplet = ((u.getPrenom() == null ? "" : u.getPrenom() + " ")
                + (u.getNom() == null ? "" : u.getNom())).trim();
        if (nomComplet.isEmpty()) nomComplet = "—";

        String email = (u.getEmail() == null || u.getEmail().isBlank()) ? "—" : u.getEmail();

        // si ton getter n'est pas getPhone(), remplace par le bon (getTelephone(), etc.)
        String phone = (u.getPhone() == null || u.getPhone().isBlank()) ? "—" : u.getPhone();

        return SuperAdminDashboardDTO.PersonVM.builder()
                .id(u.getId())
                .nomComplet(nomComplet)
                .email(email)
                .phone(phone)
                .build();
    }
}
