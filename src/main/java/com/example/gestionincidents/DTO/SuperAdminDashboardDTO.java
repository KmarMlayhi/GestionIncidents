package com.example.gestionincidents.DTO;

import com.example.gestionincidents.entity.Departement;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SuperAdminDashboardDTO {


    private long totalAdmins;
    private long totalAgents;

    @Builder.Default
    private List<DepartementBlock> departements = new ArrayList<>();

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class DepartementBlock {
        private Departement departement;
        private long nbAdmins;
        private long nbAgents;

        @Builder.Default
        private List<AdminBlock> admins = new ArrayList<>();
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class AdminBlock {
        private PersonVM admin;
        private long nbAgents;

        @Builder.Default
        private List<PersonVM> agents = new ArrayList<>();
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class PersonVM {
        private Long id;
        private String nomComplet;
        private String email;
        private String phone;
    }
}
