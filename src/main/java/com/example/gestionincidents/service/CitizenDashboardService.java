package com.example.gestionincidents.service;

import com.example.gestionincidents.entity.*;
import com.example.gestionincidents.repository.IncidentRepository;
import com.example.gestionincidents.repository.UtilisateurRepository;
import com.example.gestionincidents.DTO.CitizenDashboardDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CitizenDashboardService {

    private final IncidentRepository incidentRepository;
    private final UtilisateurRepository utilisateurRepository;

    public CitizenDashboardService(IncidentRepository incidentRepository,
                                   UtilisateurRepository utilisateurRepository) {
        this.incidentRepository = incidentRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Transactional(readOnly = true)
    public CitizenDashboardDTO buildDashboard(String citoyenEmail) {

        Utilisateur citoyen = utilisateurRepository.findByEmail(citoyenEmail)
                .orElseThrow(() -> new IllegalStateException("Citoyen introuvable : " + citoyenEmail));

        if (citoyen.getRole() != UserRole.CITOYEN) {
            throw new SecurityException("Accès refusé : rôle CITOYEN requis.");
        }

        List<Incident> incidents = incidentRepository.findByCitoyen(citoyen);

        CitizenDashboardDTO data = new CitizenDashboardDTO();

        data.setTotal(incidents.size());
        data.setClotures(incidents.stream().filter(i -> i.getEtat() == EtatIncident.CLOTURE).count());
        data.setResolus(incidents.stream().filter(i -> i.getEtat() == EtatIncident.RESOLUE).count());

        long enCours = incidents.stream().filter(i ->
                i.getEtat() == EtatIncident.NOUVEAU ||
                        i.getEtat() == EtatIncident.PRISE_EN_CHARGE ||
                        i.getEtat() == EtatIncident.EN_RESOLUTION
        ).count();
        data.setEnCours(enCours);

        // délai moyen de clôture (jours)
        List<Long> days = incidents.stream()
                .filter(i -> i.getEtat() == EtatIncident.CLOTURE
                        && i.getDateCreation() != null
                        && i.getDateCloture() != null)
                .map(i -> Duration.between(i.getDateCreation(), i.getDateCloture()).toDays())
                .toList();

        data.setDelaiMoyenJours(days.isEmpty() ? 0.0 : days.stream().mapToLong(x -> x).average().orElse(0.0));

        // par mois (6 derniers mois)
        Map<String, Long> months = new LinkedHashMap<>();
        YearMonth now = YearMonth.now();
        for (int k = 5; k >= 0; k--) {
            months.put(now.minusMonths(k).toString(), 0L);
        }

        Map<String, Long> grouped = incidents.stream()
                .filter(i -> i.getDateCreation() != null)
                .collect(Collectors.groupingBy(i -> YearMonth.from(i.getDateCreation()).toString(),
                        Collectors.counting()));

        for (String key : months.keySet()) {
            months.put(key, grouped.getOrDefault(key, 0L));
        }
        data.setByMonth(months);
        Map<String, Long> byCat = incidents.stream()
                .filter(i -> i.getCategorie() != null)
                .collect(Collectors.groupingBy(
                        i -> i.getCategorie().name(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        data.setByCategorie(byCat);
        // 5 derniers incidents
        List<Incident> last = incidentRepository.findTop5ByCitoyenOrderByDateCreationDesc(citoyen);
        data.setLastIncidents(last);

        return data;
    }
}
