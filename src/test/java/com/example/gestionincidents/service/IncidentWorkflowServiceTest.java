package com.example.gestionincidents.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.gestionincidents.entity.*;
import com.example.gestionincidents.repository.IncidentRepository;
import com.example.gestionincidents.repository.UtilisateurRepository;
import com.example.gestionincidents.service.MailService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class IncidentWorkflowServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private MailService mailService;

    @InjectMocks
    private IncidentWorkflowService incidentWorkflowService;

    @Test
    void assignerIncident_shouldAssignAgentAndSendEmails() {
        // ===== GIVEN =====

        // Admin
        Utilisateur admin = new Utilisateur();
        admin.setId(1L);
        admin.setEmail("admin@test.com");
        admin.setRole(UserRole.ADMIN);

        // Agent
        Utilisateur agent = new Utilisateur();
        agent.setId(2L);
        agent.setEmail("agent@test.com");
        agent.setRole(UserRole.AGENT);
        agent.setPrenom("Ali");
        agent.setNom("Ben Salah");
        agent.setAdministrateur(admin);

        // Citoyen
        Utilisateur citoyen = new Utilisateur();
        citoyen.setEmail("citoyen@test.com");
        citoyen.setPrenom("Sara");
        citoyen.setNom("Trabelsi");

        // Incident
        Incident incident = new Incident();
        incident.setId(10L);
        incident.setEtat(EtatIncident.NOUVEAU);
        incident.setCitoyen(citoyen);
        incident.setTitre("Panne éclairage");

        // Mock repositories
        when(utilisateurRepository.findByEmail("admin@test.com"))
                .thenReturn(Optional.of(admin));

        when(utilisateurRepository.findById(2L))
                .thenReturn(Optional.of(agent));

        when(incidentRepository.findById(10L))
                .thenReturn(Optional.of(incident));

        // ===== WHEN =====
        incidentWorkflowService.assignerIncident(10L, 2L, "admin@test.com");

        // ===== THEN =====
        assertEquals(agent, incident.getAgentAssigne());
        assertEquals(EtatIncident.PRISE_EN_CHARGE, incident.getEtat());

        verify(incidentRepository).save(incident);

        verify(mailService).sendIncidentAssignedToAgent(
                eq("agent@test.com"),
                anyString(),
                eq(10L),
                anyString(),
                any(),
                any(),
                any()
        );

        verify(mailService).sendIncidentTakenInChargeToCitizen(
                eq("citoyen@test.com"),
                anyString(),
                eq(10L),
                anyString(),
                anyString()
        );
    }
}