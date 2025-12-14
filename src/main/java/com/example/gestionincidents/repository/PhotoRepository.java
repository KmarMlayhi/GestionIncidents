package com.example.gestionincidents.repository;

import com.example.gestionincidents.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
}
