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

    //welcome mail to citizens
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
    public void send(String to, String subject, String text) {
        if (to == null || to.isBlank()) return;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(from);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    //envoyer mail au agent lorsqu'on lui assigne un incident
    public void sendIncidentAssignedToAgent(String to,
                                            String agentFullName,
                                            Long incidentId,
                                            String titre,
                                            String categorie,
                                            String quartier,
                                            String priorite) {

        String subject = " Nouvel incident assigné -  " + priorite;

        String text = "Bonjour " + agentFullName + ",\n\n"
                + "Un nouvel incident vient de vous être assigné.\n\n"
                + "Détails :\n"
                + " • Titre : " + (titre != null ? titre : "—") + "\n"
                + " • Catégorie : " + (categorie != null ? categorie : "—") + "\n"
                + " • Quartier : " + (quartier != null ? quartier : "—") + "\n"
                + " • Priorité : " + (priorite != null ? priorite : "NON DÉFINIE") + "\n\n"
                + "Connectez-vous à votre espace agent pour le prendre en charge.\n\n"
                + "Cordialement,\n"
                + "Plateforme de gestion des incidents";

        send(to, subject, text);
    }

    //envoyer mail au citoyen pour lui confirmer que son incident est prise en charge c bn
    public void sendIncidentTakenInChargeToCitizen(String to,
                                                   String citizenFullName,
                                                   Long incidentId,
                                                   String titre,
                                                   String agentFullName) {

        String subject = "Votre incident -  " + titre + " est pris en charge";

        String text = "Bonjour " + citizenFullName + ",\n\n"
                + "Votre incident a bien été pris en charge par nos services.\n\n"
                + " • Titre : " + (titre != null ? titre : "—") + "\n"
                + " • Agent assigné : " + (agentFullName != null ? agentFullName : "—") + "\n\n"
                + "Vous serez notifié des prochaines étapes.\n\n"
                + "Cordialement,\n"
                + "Plateforme de gestion des incidents";

        send(to, subject, text);
    }
    //mails aux citoyen pour passer de prise en charge en En reoslution et apres en RESOLUE
    public void sendInterventionEnCoursToCitizen(String to,
                                                 String citizenFullName,
                                                 Long incidentId,
                                                 String titre,
                                                 String agentFullName) {

        String subject = "Intervention en cours - Incident " + titre;

        String text = "Bonjour " + citizenFullName + ",\n\n"
                + "Nous vous informons que l’intervention est en cours pour votre incident.\n\n"
                + " • Titre : " + (titre != null ? titre : "—") + "\n"
                + " • Agent : " + (agentFullName != null ? agentFullName : "—") + "\n\n"
                + "Vous serez notifié dès que l’incident sera marqué comme résolu.\n\n"
                + "Cordialement,\n"
                + "Plateforme de gestion des incidents";

        send(to, subject, text);
    }

    public void sendIncidentResoluDemandeFeedbackToCitizen(String to,
                                                           String citizenFullName,
                                                           Long incidentId,
                                                           String titre) {

        String subject = " Incident résolu - Merci de donner votre feedback (#" + titre + ")";

        String text = "Bonjour " + citizenFullName + ",\n\n"
                + "Votre incident a été marqué comme RÉSOLU.\n\n"
                + " • Titre : " + (titre != null ? titre : "—") + "\n\n"
                + " • Veuillez consulter la liste de vos incidents pour voir la photo de l’intervention.\n"
                + "Ensuite, merci d’écrire votre feedback :\n"
                + " - Si tout est OK, cochez “Clôturer”, ecrivez votre commentaire et envoyez.\n"
                + " - Sinon, envoyez votre commentaire sans clôturer.\n\n"
                + "Cordialement,\n"
                + "Plateforme de gestion des incidents";

        send(to, subject, text);
    }

    //mails envoyer aus agents selon feedback du citoyen
    public void sendFeedbackClotureToAgent(String to,
                                           String agentFullName,
                                           Long incidentId,
                                           String titre,
                                           String commentaire,
                                           String citoyenFullName) {

        String subject = "Incident clôturé par le citoyen - (" + titre + ")";

        String text = "Bonjour " + agentFullName + ",\n\n"
                + "Le citoyen a confirmé la résolution et a clôturé l’incident.\n\n"
                + " • Titre : " + (titre != null ? titre : "—") + "\n"
                + " • Citoyen : " + (citoyenFullName != null ? citoyenFullName : "—") + "\n\n"
                + "Feedback :\n"
                + commentaire + "\n\n"
                + "État final : CLOTURE ! \n\n"
                + "Cordialement,\n"
                + "Plateforme de gestion des incidents";

        send(to, subject, text);
    }

    public void sendFeedbackNonClotureToAgent(String to,
                                              String agentFullName,
                                              Long incidentId,
                                              String titre,
                                              String commentaire,
                                              String citoyenFullName) {

        String subject = " Feedback négatif - Reprise demandée pour (" + titre + ")";

        String text = "Bonjour " + agentFullName + ",\n\n"
                + "Le citoyen n’a pas validé la résolution. L’incident n’est pas clôturé.\n\n"
                + " • Titre : " + (titre != null ? titre : "—") + "\n"
                + " • Citoyen : " + (citoyenFullName != null ? citoyenFullName : "—") + "\n\n"
                + "Feedback :\n"
                + commentaire + "\n\n"
                + "État actuel : RESOLUE (non clôturé)\n"
                + "Action attendue : Reprendre et refaire l’intervention (retour EN_RESOLUTION).\n\n"
                + "Cordialement,\n"
                + "Plateforme de gestion des incidents";

        send(to, subject, text);
    }






}
