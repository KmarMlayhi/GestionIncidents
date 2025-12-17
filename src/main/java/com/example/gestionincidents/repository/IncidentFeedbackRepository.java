package com.example.gestionincidents.repository;

import com.example.gestionincidents.entity.IncidentFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentFeedbackRepository extends JpaRepository<IncidentFeedback, Long> {
    List<IncidentFeedback> findByIncidentIdOrderByDateFeedbackDesc(Long incidentId);
    IncidentFeedback findTopByIncidentIdOrderByDateFeedbackDesc(Long incidentId);
}
