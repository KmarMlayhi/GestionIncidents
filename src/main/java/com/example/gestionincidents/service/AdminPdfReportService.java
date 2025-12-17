package com.example.gestionincidents.service;

import com.example.gestionincidents.DTO.AdminDashboardDTO;
import com.example.gestionincidents.entity.Incident;
import com.example.gestionincidents.entity.Utilisateur;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Service
public class AdminPdfReportService {

    public byte[] build(AdminDashboardDTO data,
                        List<Utilisateur> agents,
                        Map<Long, Long> nbIncidentsParAgent) {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            com.itextpdf.text.Document doc = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);
            com.itextpdf.text.pdf.PdfWriter.getInstance(doc, baos);

            doc.open();

            doc.add(new com.itextpdf.text.Paragraph("Rapport analytique - AdminPanel"));
            doc.add(new com.itextpdf.text.Paragraph("Généré le : " + java.time.LocalDate.now()));
            doc.add(com.itextpdf.text.Chunk.NEWLINE);

            // ---- KPIs
            com.itextpdf.text.pdf.PdfPTable kpi = new com.itextpdf.text.pdf.PdfPTable(2);
            kpi.setWidthPercentage(100);

            kpi.addCell("Total incidents");           kpi.addCell(String.valueOf(data.getTotal()));
            kpi.addCell("Nouveaux");                 kpi.addCell(String.valueOf(data.getNouveaux()));
            kpi.addCell("Prise en charge");          kpi.addCell(String.valueOf(data.getPriseEnCharge()));
            kpi.addCell("En résolution");            kpi.addCell(String.valueOf(data.getEnResolution()));
            kpi.addCell("Résolus");                  kpi.addCell(String.valueOf(data.getResolues()));
            kpi.addCell("Clôturés");                 kpi.addCell(String.valueOf(data.getClotures()));
            kpi.addCell("Délai moyen (heures)");     kpi.addCell(String.format(java.util.Locale.FRANCE, "%.2f", data.getDelaiMoyenHeures()));

            doc.add(new com.itextpdf.text.Paragraph("Indicateurs"));
            doc.add(kpi);
            doc.add(com.itextpdf.text.Chunk.NEWLINE);

            // ---- Agents
            doc.add(new com.itextpdf.text.Paragraph("Agents (nombre d'incidents)"));
            com.itextpdf.text.pdf.PdfPTable tAgents = new com.itextpdf.text.pdf.PdfPTable(4);
            tAgents.setWidthPercentage(100);

            tAgents.addCell("Nom");
            tAgents.addCell("Email");
            tAgents.addCell("Téléphone");
            tAgents.addCell("Nb incidents");

            for (Utilisateur a : agents) {
                String nom = (a.getPrenom() + " " + a.getNom()).trim();
                String tel = (a.getPhone() == null ? "-" : a.getPhone());

                long nb = nbIncidentsParAgent.getOrDefault(a.getId(), 0L);

                tAgents.addCell(nom);
                tAgents.addCell(a.getEmail());
                tAgents.addCell(tel);
                tAgents.addCell(String.valueOf(nb));
            }

            doc.add(tAgents);
            doc.add(com.itextpdf.text.Chunk.NEWLINE);

            // ---- Derniers incidents
            doc.add(new com.itextpdf.text.Paragraph("Derniers incidents"));
            com.itextpdf.text.pdf.PdfPTable tInc = new com.itextpdf.text.pdf.PdfPTable(5);
            tInc.setWidthPercentage(100);

            tInc.addCell("ID");
            tInc.addCell("Titre");
            tInc.addCell("État");
            tInc.addCell("Date");
            tInc.addCell("Agent");

            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (Incident i : data.getLastIncidents()) {
                tInc.addCell(String.valueOf(i.getId()));
                tInc.addCell(i.getTitre());
                tInc.addCell(i.getEtat() == null ? "-" : i.getEtat().name());
                tInc.addCell(i.getDateCreation() == null ? "-" : i.getDateCreation().format(fmt));
                tInc.addCell(i.getAgentAssigne() == null ? "-" : (i.getAgentAssigne().getPrenom() + " " + i.getAgentAssigne().getNom()));
            }

            doc.add(tInc);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Erreur génération PDF : " + e.getMessage(), e);
        }
    }
}
