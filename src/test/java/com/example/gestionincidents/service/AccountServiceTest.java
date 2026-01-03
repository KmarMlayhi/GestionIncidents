package com.example.gestionincidents.service;

import com.example.gestionincidents.entity.Departement;
import com.example.gestionincidents.entity.UserRole;
import com.example.gestionincidents.entity.Utilisateur;
import com.example.gestionincidents.repository.UtilisateurRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private JdbcUserDetailsManager userDetailsManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAgent_shouldGeneratePasswordAndSaveAgent() {

        // ===== GIVEN =====
        when(userDetailsManager.userExists("agent@mail.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("ENCODED_PASSWORD");

        ArgumentCaptor<Utilisateur> utilisateurCaptor =
                ArgumentCaptor.forClass(Utilisateur.class);

        // ===== WHEN =====
        String rawPassword = accountService.createAgentWithGeneratedPassword(
                "Nom",
                "Prenom",
                "agent@mail.com",
                "9999",
                null   // departement
        );

        // ===== THEN =====
        assertNotNull(rawPassword);
        assertTrue(rawPassword.length() >= 8);

        verify(userDetailsManager).createUser(any(UserDetails.class));
        verify(utilisateurRepository).save(utilisateurCaptor.capture());

        Utilisateur savedUser = utilisateurCaptor.getValue();

        assertEquals("Nom", savedUser.getNom());
        assertEquals("Prenom", savedUser.getPrenom());
        assertEquals("agent@mail.com", savedUser.getEmail());
        assertEquals("9999", savedUser.getPhone());
        assertEquals(UserRole.AGENT, savedUser.getRole());
        assertEquals("ENCODED_PASSWORD", savedUser.getMotDePasse());
        assertNull(savedUser.getDepartement());
    }

    @Test
    void createAgent_shouldThrowException_ifUserAlreadyExists() {

        // ===== GIVEN =====
        when(userDetailsManager.userExists("agent@mail.com")).thenReturn(true);

        // ===== WHEN + THEN =====
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.createAgentWithGeneratedPassword(
                        "Nom",
                        "Prenom",
                        "agent@mail.com",
                        "9999",
                        null
                )
        );

        assertEquals("Un compte existe déjà avec cet email", exception.getMessage());

        verify(userDetailsManager, never()).createUser(any());
        verify(utilisateurRepository, never()).save(any());
    }
}
