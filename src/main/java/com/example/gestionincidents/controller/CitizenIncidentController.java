package com.example.gestionincidents.controller;

import com.example.gestionincidents.entity.*;
import com.example.gestionincidents.repository.*;
import com.example.gestionincidents.service.CitizenFeedbackService;
import com.example.gestionincidents.service.ConnectedUserInfoService;
import com.example.gestionincidents.web.IncidentForm;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Map;



@Controller
@RequestMapping("/citoyen/incidents")
public class CitizenIncidentController {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png"
    );

    private final IncidentRepository incidentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PhotoRepository photoRepository;
    private final ConnectedUserInfoService connectedUserInfoService;
    private final QuartierRepository quartierRepository;
    private final CitizenFeedbackService citizenFeedbackService;
    private final IncidentFeedbackRepository feedbackRepository;


    public CitizenIncidentController(IncidentRepository incidentRepository,
                                     UtilisateurRepository utilisateurRepository,
                                     PhotoRepository photoRepository,
                                     ConnectedUserInfoService connectedUserInfoService,
                                     QuartierRepository quartierRepository,
                                     CitizenFeedbackService citizenFeedbackService, IncidentFeedbackRepository feedbackRepository){
        this.incidentRepository = incidentRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.photoRepository = photoRepository;
        this.quartierRepository = quartierRepository;
        this.connectedUserInfoService = connectedUserInfoService;
        this.citizenFeedbackService = citizenFeedbackService;
        this.feedbackRepository = feedbackRepository;
    }

    // 1) PAGE "MES INCIDENTS"
    @GetMapping
    public String listIncidents(@RequestParam(defaultValue = "0") int page,
                                Model model,
                                Authentication authentication) {

        connectedUserInfoService.addConnectedUserInfo(model, authentication);

        String email = authentication.getName();
        Utilisateur citoyen = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Citoyen introuvable : " + email));

        Pageable pageable = PageRequest.of(page, 5, Sort.by(Sort.Direction.DESC, "dateCreation"));
        Page<Incident> incidentsPage = incidentRepository.findByCitoyen(citoyen, pageable);

        List<Incident> incidents = incidentsPage.getContent();
        model.addAttribute("incidents", incidents);


        Map<Long, List<IncidentFeedback>> feedbacksByIncident = new HashMap<>();
        for (Incident inc : incidents) {
            feedbacksByIncident.put(
                    inc.getId(),
                    feedbackRepository.findByIncidentIdOrderByDateFeedbackDesc(inc.getId())
            );
        }
        model.addAttribute("feedbacksByIncident", feedbacksByIncident);

        // pagination
        model.addAttribute("currentPage", incidentsPage.getNumber());
        model.addAttribute("totalPages", incidentsPage.getTotalPages());
        model.addAttribute("totalItems", incidentsPage.getTotalElements());

        return "citizen-incidents-list";
    }

    // 2) Formulaire "nouvel incident"
    @GetMapping("/nouveau")
    public String showNewIncidentForm(Model model, Authentication authentication) {

        connectedUserInfoService.addConnectedUserInfo(model, authentication);

        model.addAttribute("incidentForm", new IncidentForm());
        model.addAttribute("categories", CategorieIncident.values());

        return "citoyen-incident-form";
    }

    // 3) Traitement du formulaire
    @PostMapping
    public String createIncident(@ModelAttribute("incidentForm") IncidentForm form,
                                 @RequestParam(name = "photos", required = false) List<MultipartFile> photos,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {

        String email = authentication.getName();
        Utilisateur citoyen = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Citoyen introuvable : " + email));

        try {
            // Valider les photos AVANT de créer/sauvegarder l'incident
            validatePhotosOrThrow(photos);

            // 1) Construire l'incident
            Incident incident = new Incident();
            incident.setTitre(form.getTitre());
            incident.setDescription(form.getDescription());
            incident.setCategorie(form.getCategorie());
            incident.setPriorite(form.getPriorite());
            incident.setDateSignalement(LocalDateTime.now());
            incident.setDateCreation(LocalDateTime.now());
            incident.setEtat(EtatIncident.NOUVEAU);
            incident.setLatitude(form.getLatitude());
            incident.setLongitude(form.getLongitude());
            incident.setCitoyen(citoyen);

            // Quartier
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

            // 2) Sauvegarde incident seulement si photos OK
            Incident saved = incidentRepository.save(incident);

            // 3) Sauvegarder photos
            if (photos != null && !photos.isEmpty()) {
                savePhotosForIncident(saved, photos);
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    "Incident déclaré avec succès. Merci pour votre contribution !");
            return "redirect:/citoyen/incidents";

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/citoyen/incidents/nouveau";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Erreur lors de la déclaration de l'incident.");
            return "redirect:/citoyen/incidents/nouveau";
        }
    }

    private void validatePhotosOrThrow(List<MultipartFile> files) {
        if (files == null) return;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("Image trop volumineuse (max 5 MB).");
            }

            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
                throw new IllegalArgumentException("Format non autorisé. Seuls JPG/PNG sont acceptés.");
            }

            String original = file.getOriginalFilename();
            if (original == null || !original.matches("(?i).+\\.(jpg|jpeg|png)$")) {
                throw new IllegalArgumentException("Extension invalide. Seuls .jpg/.jpeg/.png sont acceptés.");
            }

            try {
                BufferedImage image = ImageIO.read(file.getInputStream());
                if (image == null) {
                    throw new IllegalArgumentException("Le fichier uploadé n'est pas une image valide.");
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Impossible de lire l'image uploadée.");
            }
        }
    }

    // 4) Upload des photos en local
    private void savePhotosForIncident(Incident incident, List<MultipartFile> files) throws IOException {

        Path uploadDir = Paths.get("src/main/resources/static/uploads/incidents");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        for (MultipartFile file : files) {

            //  Fichier vide
            if (file.isEmpty()) {
                continue;
            }

            //  Taille maximale
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("Image trop volumineuse (max 5 MB)");
            }

            //  Type MIME non autorisé
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
                throw new IllegalArgumentException("Type de fichier non autorisé");
            }

            //  Extension invalide
            String original = file.getOriginalFilename();
            if (original == null || !original.matches("(?i).+\\.(jpg|jpeg|png)$")) {
                throw new IllegalArgumentException("Extension de fichier invalide");
            }

            //  Vérification réelle de l’image (anti-fichier déguisé)
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new IllegalArgumentException("Le fichier n'est pas une image valide");
            }

            //  Génération d’un nom sécurisé
            String extension = original.substring(original.lastIndexOf("."));
            String uniqueName = UUID.randomUUID() + extension;

            Path destination = uploadDir.resolve(uniqueName);

            // Copie sécurisée
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            //  Enregistrement en base
            Photo photo = new Photo();
            photo.setNomPhoto(uniqueName);
            photo.setChemin("uploads/incidents");
            photo.setType(contentType);
            photo.setDateUpload(LocalDateTime.now());
            photo.setIncident(incident);

            photoRepository.save(photo);
        }
    }

    @PostMapping("/{id}/feedback")
    public String envoyerFeedback(@PathVariable Long id,
                                  @RequestParam("commentaire") String commentaire,
                                  @RequestParam(value = "cloturer", defaultValue = "false") boolean cloturer,
                                  Authentication authentication,
                                  RedirectAttributes ra) {
        try {
            citizenFeedbackService.envoyerFeedback(id, commentaire, cloturer, authentication.getName());
            ra.addFlashAttribute("successMessage",
                    cloturer ? "Merci ! Incident clôturé." : "Merci ! Votre feedback a été enregistré.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/citoyen/incidents";
    }

}
