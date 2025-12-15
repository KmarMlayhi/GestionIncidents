package com.example.gestionincidents.service;

import com.example.gestionincidents.entity.Departement;
import com.example.gestionincidents.entity.UserRole;
import com.example.gestionincidents.entity.Utilisateur;
import com.example.gestionincidents.repository.UtilisateurRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

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

    // Génération d'un mot de passe aléatoire
    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#$%&";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public boolean userExists(String email) {
        return userDetailsManager.userExists(email);
    }

    //  Création définitive d’un citoyen après vérification du code
    public void registerCitizen(String nom, String prenom, String email, String phone, String rawPassword) {
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
        u.setPhone(phone);
        u.setMotDePasse(encoded);
        u.setRole(UserRole.CITOYEN);

        utilisateurRepository.save(u);
    }

    // Création d’un AGENT avec mot de passe généré (renvoie le mdp en clair)
    public String createAgentWithGeneratedPassword(String nom,
                                                   String prenom,
                                                   String email,
                                                   String phone,
                                                   Departement departement) {
        if (userDetailsManager.userExists(email)) {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email");
        }

        // mot de passe en clair
        String rawPassword = generateRandomPassword(10);
        // version encodée pour la base
        String encoded = passwordEncoder.encode(rawPassword);

        // Spring Security
        UserDetails securityUser = User.withUsername(email)
                .password(encoded)
                .roles("AGENT")
                .build();
        userDetailsManager.createUser(securityUser);

        // Métier
        Utilisateur u = new Utilisateur();
        u.setNom(nom);
        u.setPrenom(prenom);
        u.setEmail(email);
        u.setPhone(phone);
        u.setMotDePasse(encoded);
        u.setRole(UserRole.AGENT);
        u.setDepartement(departement);

        utilisateurRepository.save(u);

        //  On renvoie le mot de passe en clair pour l'email
        return rawPassword;
    }

    // Création d’un ADMIN avec mot de passe généré (renvoie le mdp en clair)
    public String createAdminWithGeneratedPassword(String nom,
                                                   String prenom,
                                                   String email,
                                                   String phone,
                                                   Departement departement) {
        if (userDetailsManager.userExists(email)) {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email");
        }

        String rawPassword = generateRandomPassword(10);
        String encoded = passwordEncoder.encode(rawPassword);

        UserDetails securityUser = User.withUsername(email)
                .password(encoded)
                .roles("ADMIN")   // => ROLE_ADMIN
                .build();
        userDetailsManager.createUser(securityUser);

        Utilisateur u = new Utilisateur();
        u.setNom(nom);
        u.setPrenom(prenom);
        u.setEmail(email);
        u.setPhone(phone);
        u.setMotDePasse(encoded);
        u.setRole(UserRole.ADMIN);
        u.setDepartement(departement);

        utilisateurRepository.save(u);

        //  On renvoie le mot de passe en clair pour l'email
        return rawPassword;
    }

}
