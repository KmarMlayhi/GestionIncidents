package com.example.gestionincidents.controller;

import com.example.gestionincidents.entity.VerificationCode;
import com.example.gestionincidents.service.AccountService;
import com.example.gestionincidents.service.MailService;
import com.example.gestionincidents.repository.VerificationCodeRepository;
import com.example.gestionincidents.web.CitizenRegistrationForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Controller
public class AuthController {

    private final AccountService accountService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final MailService mailService;

    public AuthController(AccountService accountService,
                          VerificationCodeRepository verificationCodeRepository,
                          MailService mailService) {
        this.accountService = accountService;
        this.verificationCodeRepository = verificationCodeRepository;
        this.mailService = mailService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // 1) Formulaire d'inscription
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("form", new CitizenRegistrationForm());
        return "register";
    }

    // 2) Soumission du formulaire : on envoie le code par email
    @PostMapping("/register")
    public String handleRegister(@ModelAttribute("form") CitizenRegistrationForm form,
                                 Model model) {
        try {
            // Vérifier si un compte existe déjà dans Spring Security
            if (accountService.userExists(form.getEmail())) {
                model.addAttribute("error", "Un compte existe déjà avec cet email.");
                return "register";
            }

            // Générer un code à 6 chiffres
            String code = String.format("%06d", new Random().nextInt(1_000_000));

            // Supprimer d'éventuelles anciennes demandes pour cet email
            verificationCodeRepository.deleteByEmail(form.getEmail());

            // Sauvegarder la demande d'inscription en attente
            VerificationCode vc = new VerificationCode();
            vc.setNom(form.getNom());
            vc.setPrenom(form.getPrenom());
            vc.setEmail(form.getEmail());
            vc.setPhone(form.getPhone());
            vc.setRawPassword(form.getPassword());
            vc.setCode(code);
            vc.setCreatedAt(LocalDateTime.now());

            verificationCodeRepository.save(vc);

            // Envoyer le mail
            mailService.sendVerificationCode(form.getEmail(), code);

            // Rediriger vers la page de saisie du code
            String encodedEmail = URLEncoder.encode(form.getEmail(), StandardCharsets.UTF_8);
            return "redirect:/verify?email=" + encodedEmail;

        } catch (Exception ex) {
            model.addAttribute("error", "Une erreur est survenue : " + ex.getMessage());
            return "register";
        }
    }

    // 3) Afficher la page pour saisir le code
    @GetMapping("/verify")
    public String showVerifyPage(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "verify";
    }

    // 4) Vérifier le code et créer définitivement le compte
    @PostMapping("/verify")
    public String handleVerify(@RequestParam("email") String email,
                               @RequestParam("code") String code,
                               Model model) {
        Optional<VerificationCode> opt = verificationCodeRepository.findByEmail(email);

        if (opt.isEmpty()) {
            model.addAttribute("error", "Aucune demande d'inscription trouvée pour cet email.");
            model.addAttribute("email", email);
            return "verify";
        }

        VerificationCode vc = opt.get();

        // vérifier le code
        if (!vc.getCode().equals(code)) {
            model.addAttribute("error", "Code incorrect.");
            model.addAttribute("email", email);
            return "verify";
        }

        // (optionnel) vérifier qu'il n'est pas trop vieux
        // if (vc.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(15))) { ... }

        // Créer vraiment le compte (users + authorities + utilisateur)
        accountService.registerCitizen(
                vc.getNom(),
                vc.getPrenom(),
                vc.getEmail(),
                vc.getPhone(),
                vc.getRawPassword()
        );

        // Supprimer la demande
        verificationCodeRepository.deleteByEmail(email);

        return "redirect:/login?verified";
    }
}
