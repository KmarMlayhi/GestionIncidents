package com.example.gestionincidents.service;

import com.example.gestionincidents.entity.UserRole;
import com.example.gestionincidents.entity.Utilisateur;
import com.example.gestionincidents.repository.UtilisateurRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.JdbcUserDetailsManager;   // 👈 IMPORTANT
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final JdbcUserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;
    private final UtilisateurRepository utilisateurRepository;

    public AccountService(JdbcUserDetailsManager userDetailsManager,
                          PasswordEncoder passwordEncoder,
                          UtilisateurRepository utilisateurRepository) {
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
        this.utilisateurRepository = utilisateurRepository;
    }

    public void registerCitizen(String nom, String prenom, String email, String rawPassword) {
        if (userDetailsManager.userExists(email)) {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email");
        }

        String encoded = passwordEncoder.encode(rawPassword);

        // Spring Security (tables users + authorities)
        UserDetails securityUser = User.withUsername(email)
                .password(encoded)
                .roles("CITOYEN")
                .build();

        userDetailsManager.createUser(securityUser);

        // Métier (table utilisateur)
        Utilisateur u = new Utilisateur();
        u.setNom(nom);
        u.setPrenom(prenom);
        u.setEmail(email);
        u.setMotDePasse(encoded);
        u.setRole(UserRole.CITOYEN);

        utilisateurRepository.save(u);
    }

    public void createAgent(String nom, String prenom, String email, String rawPassword) {
        if (userDetailsManager.userExists(email)) {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email");
        }

        String encoded = passwordEncoder.encode(rawPassword);

        UserDetails securityUser = User.withUsername(email)
                .password(encoded)
                .roles("AGENT")
                .build();

        userDetailsManager.createUser(securityUser);

        Utilisateur u = new Utilisateur();
        u.setNom(nom);
        u.setPrenom(prenom);
        u.setEmail(email);
        u.setMotDePasse(encoded);
        u.setRole(UserRole.AGENT);

        utilisateurRepository.save(u);
    }
}
