package com.example.gestionincidents.DTO;

import lombok.Data;
import com.example.gestionincidents.entity.Incident;

import java.util.List;
import java.util.Map;
@Data
public class CitizenDashboardDTO {

    private long total;
    private long enCours;
    private long resolus;
    private long clotures;
    private double delaiMoyenJours;

    private Map<String, Long> byCategorie;
    private Map<String, Long> byMonth;
    private List<Incident> lastIncidents;
}
