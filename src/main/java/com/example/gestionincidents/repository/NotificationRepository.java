package com.example.gestionincidents.repository;

import com.example.gestionincidents.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
