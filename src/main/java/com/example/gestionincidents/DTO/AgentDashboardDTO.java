package com.example.gestionincidents.DTO;

import com.example.gestionincidents.entity.Incident;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AgentDashboardDTO {

    private long totalAssignes;

    private long nouveaux;
    private long priseEnCharge;
    private long enResolution;
    private long resolues;
    private long clotures;

    private long prioHaute;
    private long prioMoyenne;
    private long prioBasse;

    private AdminDTO admin; // admin responsable de cet agent (si affecté)

    @Builder.Default
    private List<Incident> lastIncidents = new ArrayList<>();

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class AdminDTO {
        private String nomComplet;
        private String email;
        private String phone;
    }

    public static AgentDashboardDTO empty() {
        return AgentDashboardDTO.builder()
                .admin(AdminDTO.builder()
                        .nomComplet("—")
                        .email("—")
                        .phone("—")
                        .build())
                .lastIncidents(new ArrayList<>())
                .build();
    }
}
