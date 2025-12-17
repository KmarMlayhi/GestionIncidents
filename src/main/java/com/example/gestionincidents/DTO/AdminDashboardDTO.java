package com.example.gestionincidents.DTO;

import com.example.gestionincidents.entity.Incident;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AdminDashboardDTO {
    private long total;
    private long nouveaux;
    private long priseEnCharge;
    private long enResolution;
    private long resolues;
    private long clotures;

    private double delaiMoyenHeures;

    private List<String> catLabels;
    private List<Long> catValues;

    private List<String> quartierLabels;
    private List<Long> quartierValues;

    private List<String> monthLabels;
    private List<Long> monthValues;

    private List<Incident> lastIncidents;

    public static AdminDashboardDTO empty() {
        return AdminDashboardDTO.builder()
                .catLabels(new ArrayList<>()).catValues(new ArrayList<>())
                .quartierLabels(new ArrayList<>()).quartierValues(new ArrayList<>())
                .monthLabels(new ArrayList<>()).monthValues(new ArrayList<>())
                .lastIncidents(new ArrayList<>())
                .build();
    }
}
