package com.example.gestionincidents.service;

import com.example.gestionincidents.DTO.AdminDashboardDTO;
import com.example.gestionincidents.entity.*;
import com.example.gestionincidents.repository.IncidentRepository;
import com.example.gestionincidents.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminAnalyticsService {

    private final UtilisateurRepository utilisateurRepository;
    private final IncidentRepository incidentRepository;

    public AdminAnalyticsService(UtilisateurRepository utilisateurRepository,
                                 IncidentRepository incidentRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.incidentRepository = incidentRepository;
    }

    @Transactional(readOnly = true)
    public AdminDashboardDTO buildDashboard(String adminEmail) {
        Utilisateur admin = utilisateurRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalStateException("Admin introuvable: " + adminEmail));

        if (admin.getRole() != UserRole.ADMIN && admin.getRole() != UserRole.SUPER_ADMIN) {
            throw new SecurityException("Accès refusé : rôle ADMIN requis.");
        }

        // agents de cet admin
        List<Utilisateur> agents;
        if (admin.getRole() == UserRole.SUPER_ADMIN) {
            agents = utilisateurRepository.findByRole(UserRole.AGENT);
        } else {
            agents = utilisateurRepository.findAgentsByAdministrateur(admin.getId());
        }

        // si aucun agent, DTO  vide
        if (agents.isEmpty()) {
            return AdminDashboardDTO.empty();
        }

        List<Incident> all = incidentRepository.findByAgentAssigneIn(agents);

        long total = all.size();
        long nouveaux = all.stream().filter(i -> i.getEtat() == EtatIncident.NOUVEAU).count();
        long prise = all.stream().filter(i -> i.getEtat() == EtatIncident.PRISE_EN_CHARGE).count();
        long enRes = all.stream().filter(i -> i.getEtat() == EtatIncident.EN_RESOLUTION).count();
        long resolue = all.stream().filter(i -> i.getEtat() == EtatIncident.RESOLUE).count();
        long cloture = all.stream().filter(i -> i.getEtat() == EtatIncident.CLOTURE).count();

        // délai moyen de résolution ( incidents CLOTURE avec dateCloture)
        List<Incident> clotures = all.stream()
                .filter(i -> i.getEtat() == EtatIncident.CLOTURE && i.getDateCreation() != null && i.getDateCloture() != null)
                .toList();

        double delaiMoyenHeures = 0.0;
        if (!clotures.isEmpty()) {
            long sumMinutes = 0;
            for (Incident i : clotures) {
                sumMinutes += Duration.between(i.getDateCreation(), i.getDateCloture()).toMinutes();
            }
            delaiMoyenHeures = (sumMinutes / (double) clotures.size()) / 60.0;
        }

        // séries charts
        List<Object[]> catRaw = incidentRepository.countByCategorieForAgents(agents);
        List<String> catLabels = catRaw.stream().map(r -> String.valueOf(r[0])).toList();
        List<Long> catValues = catRaw.stream().map(r -> (Long) r[1]).toList();

        List<Object[]> qRaw = incidentRepository.countByQuartierForAgents(agents);
        List<String> quartierLabels = qRaw.stream().map(r -> String.valueOf(r[0])).toList();
        List<Long> quartierValues = qRaw.stream().map(r -> (Long) r[1]).toList();

        LocalDateTime from = LocalDateTime.now().minusMonths(12);
        List<Object[]> mRaw = incidentRepository.countByMonthForAgents(agents, from);
        List<String> monthLabels = mRaw.stream().map(r -> String.valueOf(r[0])).toList();
        List<Long> monthValues = mRaw.stream().map(r -> (Long) r[1]).toList();

        // top 5 récents
        List<Incident> last5 = all.stream()
                .sorted(Comparator.comparing(Incident::getDateCreation, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(5)
                .toList();

        return AdminDashboardDTO.builder()
                .total(total)
                .nouveaux(nouveaux)
                .priseEnCharge(prise)
                .enResolution(enRes)
                .resolues(resolue)
                .clotures(cloture)
                .delaiMoyenHeures(delaiMoyenHeures)
                .catLabels(catLabels)
                .catValues(catValues)
                .quartierLabels(quartierLabels)
                .quartierValues(quartierValues)
                .monthLabels(monthLabels)
                .monthValues(monthValues)
                .lastIncidents(last5)
                .build();
    }
}
