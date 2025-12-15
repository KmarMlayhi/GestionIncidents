package com.example.gestionincidents.controller;

import com.example.gestionincidents.entity.*;
import com.example.gestionincidents.repository.IncidentRepository;
import com.example.gestionincidents.repository.PhotoRepository;
import com.example.gestionincidents.repository.UtilisateurRepository;
import com.example.gestionincidents.service.ConnectedUserInfoService;
import com.example.gestionincidents.web.IncidentForm;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.gestionincidents.repository.QuartierRepository;


import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/citoyen/incidents")
public class CitizenIncidentController {

    private final IncidentRepository incidentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PhotoRepository photoRepository;
    private final ConnectedUserInfoService connectedUserInfoService;
    private final QuartierRepository quartierRepository;


    public CitizenIncidentController(IncidentRepository incidentRepository,
                                     UtilisateurRepository utilisateurRepository,
                                     PhotoRepository photoRepository,
                                     ConnectedUserInfoService connectedUserInfoService,
                                        QuartierRepository quartierRepository){
        this.incidentRepository = incidentRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.photoRepository = photoRepository;
        this.quartierRepository = quartierRepository;
        this.connectedUserInfoService = connectedUserInfoService;
    }

    // 🟦 1) PAGE "MES INCIDENTS"  => GET /citoyen/incidents
    @GetMapping
    public String listIncidents(Model model, Authentication authentication) {

        // Infos pour le header (nom, rôle, etc.)
        connectedUserInfoService.addConnectedUserInfo(model, authentication);

        // Récupérer le citoyen connecté
        String email = authentication.getName();
        Utilisateur citoyen = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Citoyen introuvable : " + email));

        // Charger ses incidents
        List<Incident> incidents = incidentRepository.findByCitoyen(citoyen);
        model.addAttribute("incidents", incidents);

        // Vue à créer : templates/citizen-incidents-list.html
        return "citizen-incidents-list";
    }

    // 🟦 2) Formulaire "nouvel incident" => GET /citoyen/incidents/nouveau
    @GetMapping("/nouveau")
    public String showNewIncidentForm(Model model, Authentication authentication) {

        connectedUserInfoService.addConnectedUserInfo(model, authentication);

        model.addAttribute("incidentForm", new IncidentForm());
        model.addAttribute("categories", CategorieIncident.values());
        // Si tu ne veux pas que le citoyen choisisse la priorité, commente la ligne ci-dessous :
        // model.addAttribute("priorites", Priorite.values());

        return "citoyen-incident-form";   // template du formulaire
    }

    // 🟦 3) Traitement du formulaire => POST /citoyen/incidents
    @PostMapping
    public String createIncident(@ModelAttribute("incidentForm") IncidentForm form,
                                 @RequestParam(name = "photos", required = false) List<MultipartFile> photos,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {

        try {
            // 1) Récupérer le citoyen connecté
            String email = authentication.getName();
            Utilisateur citoyen = utilisateurRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException("Citoyen introuvable : " + email));

            // 2) Construire l'incident
            Incident incident = new Incident();
            incident.setTitre(form.getTitre());
            incident.setDescription(form.getDescription());
            incident.setCategorie(form.getCategorie());

            // Si la priorité ne doit PAS être choisie par le citoyen, laisse null :
            // incident.setPriorite(null);
            incident.setPriorite(form.getPriorite());

            incident.setDateSignalement(LocalDateTime.now());
            incident.setDateCreation(LocalDateTime.now());
            incident.setEtat(EtatIncident.NOUVEAU); // valeur qui existe bien dans ton enum SQL
            incident.setLatitude(form.getLatitude());
            incident.setLongitude(form.getLongitude());
            incident.setCitoyen(citoyen);

            // =====================
            // GESTION DU QUARTIER
            // =====================
            String quartierNom = form.getQuartierNom();

            if (quartierNom != null && !quartierNom.trim().isEmpty()) {

                String nomNettoye = quartierNom.trim();

                Quartier quartier = quartierRepository
                        .findByNomIgnoreCase(nomNettoye)
                        .orElseGet(() -> {
                            Quartier q = new Quartier();
                            q.setNom(nomNettoye);
                            return quartierRepository.save(q);
                        });

                incident.setQuartier(quartier);
            }
            // Sauvegarde de l'incident
            Incident saved = incidentRepository.save(incident);

            // 3) Sauvegarder les photos si présentes
            if (photos != null && !photos.isEmpty()) {
                savePhotosForIncident(saved, photos);
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    "Incident déclaré avec succès. Merci pour votre contribution !");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Erreur lors de la déclaration de l'incident : " + e.getMessage());
        }

        // 👉 Après le POST, on redirige vers le GET ci-dessus (/citoyen/incidents)
        return "redirect:/citoyen/incidents";
    }

    // 🟦 4) Upload des photos en local
    private void savePhotosForIncident(Incident incident, List<MultipartFile> files) throws IOException {
        Path uploadDir = Paths.get("src/main/resources/static/uploads/incidents");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String extension = "";
            String original = file.getOriginalFilename();
            if (original != null && original.contains(".")) {
                extension = original.substring(original.lastIndexOf("."));
            }

            String uniqueName = UUID.randomUUID() + extension;
            Path destination = uploadDir.resolve(uniqueName);

            // Copie physique du fichier
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            // Enregistrement en base
            Photo photo = new Photo();
            photo.setNomPhoto(uniqueName);
            photo.setChemin(uploadDir.toString());           // ex: "uploads/incidents"
            photo.setType(file.getContentType());            // ex: "image/jpeg"
            photo.setDateUpload(LocalDateTime.now());
            photo.setIncident(incident);

            photoRepository.save(photo);
        }
    }
}
