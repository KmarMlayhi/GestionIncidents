package com.example.gestionincidents.repository;

import com.example.gestionincidents.entity.Quartier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuartierRepository extends JpaRepository<Quartier, Long> {

    Optional<Quartier> findByNomIgnoreCase(String nom);
}
