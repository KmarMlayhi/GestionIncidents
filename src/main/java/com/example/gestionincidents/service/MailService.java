package com.example.gestionincidents.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;   // adresse d'envoi (configurée dans application.properties)

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    //Code pour les citoyen
    public void sendVerificationCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(from);
        message.setSubject("Vérification de votre compte - Gestion des incidents");
        String texte = "Bonjour,\n\n"
                + "Vous avez demandé à créer un compte sur la plateforme de gestion des incidents de la ville.\n\n"
                + "👉 Votre code de vérification est : " + code + "\n\n"
                + "Veuillez le saisir sur la page de confirmation pour finaliser votre inscription.\n\n"
                + "Si vous n'êtes pas à l'origine de cette demande, vous pouvez ignorer ce message.\n\n"
                + "Cordialement,\n"
                + "L’équipe de la plateforme de gestion des incidents";

        message.setText(texte);
        mailSender.send(message);
    }

    public void sendWelcomeEmail(String to, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(from);
        message.setSubject("🎉 Bienvenue sur la plateforme de gestion des incidents");

        String texte = "Bonjour " + fullName + ",\n\n"
                + "Votre compte citoyen a été créé et activé avec succès.\n\n"
                + "Vous pouvez désormais :\n"
                + " • Déclarer des incidents dans votre quartier\n"
                + " • Suivre l’avancement de vos signalements\n"
                + " • Consulter l’historique de vos déclarations\n"
                + " • Recevoir des mises à jour sur le traitement de vos demandes\n\n"
                + "Merci de contribuer à l’amélioration de votre ville.\n\n"
                + "Cordialement,\n"
                + "L’équipe de la plateforme de gestion des incidents";

        message.setText(texte);
        mailSender.send(message);
    }
    // Mail pour ADMIN / AGENT créés par le super admin
    public void sendStaffAccountEmail(String to,
                                      String fullName,
                                      String roleLabel,
                                      String tempPassword) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(from);
        message.setSubject("Votre compte " + roleLabel + " - Platforme de gestion des incidents");

        String texte = "Bonjour " + fullName + ",\n\n"
                + "Un compte \"" + roleLabel + "\" a été créé pour vous sur la plateforme de gestion des incidents de la ville.\n\n"
                + "Vos informations de connexion sont :\n"
                + " • Identifiant (email) : " + to + "\n"
                + " • Mot de passe provisoire : " + tempPassword + "\n\n"
                + "⚠ Ce mot de passe est PROVISOIRE.\n"
                + "Merci de vous connecter dès que possible et de le modifier dès votre première connexion,\n"
                + "dans votre espace profil, afin de sécuriser votre compte.\n\n"
                + "Cordialement,\n"
                + "L’équipe de la plateforme de gestion des incidents";

        message.setText(texte);
        mailSender.send(message);
    }

}
