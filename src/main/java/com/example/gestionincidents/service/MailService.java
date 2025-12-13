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

    public void sendVerificationCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(from);
        message.setSubject("Code de vérification - Gestion des incidents");
        message.setText("Bonjour,\n\nVotre code de vérification est : " + code +
                "\n\nSi vous n'êtes pas à l'origine de cette demande, ignorez ce message.");
        mailSender.send(message);
    }
}
