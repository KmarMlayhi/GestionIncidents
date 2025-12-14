package com.example.gestionincidents.repository;

import com.example.gestionincidents.entity.Rapport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RapportRepository extends JpaRepository<Rapport, Long> {
}
