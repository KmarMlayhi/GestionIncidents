package com.example.gestionincidents.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.gestionincidents.entity.*;
import com.example.gestionincidents.repository.*;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AgentIncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private IncidentFeedbackRepository feedbackRepository;

    @Mock
    private MailService mailService;

    @InjectMocks
    private AgentIncidentService agentIncidentService;

    @Test
    void getIncidentsAssignes_shouldReturnIncidentsForAgent() {

        // ===== GIVEN =====
        Utilisateur agent = new Utilisateur();
        agent.setId(1L);
        agent.setEmail("agent@test.com");
        agent.setRole(UserRole.AGENT);

        Incident incident1 = new Incident();
        incident1.setId(10L);

        Incident incident2 = new Incident();
        incident2.setId(11L);

        when(utilisateurRepository.findByEmail("agent@test.com"))
                .thenReturn(Optional.of(agent));

        when(incidentRepository.findAssignedToAgentOrdered(1L))
                .thenReturn(List.of(incident1, incident2));

        // ===== WHEN =====
        List<Incident> result =
                agentIncidentService.getIncidentsAssignes("agent@test.com");

        // ===== THEN =====
        assertEquals(2, result.size());
        assertTrue(result.contains(incident1));
        assertTrue(result.contains(incident2));

        verify(utilisateurRepository).findByEmail("agent@test.com");
        verify(incidentRepository).findAssignedToAgentOrdered(1L);
    }
}
