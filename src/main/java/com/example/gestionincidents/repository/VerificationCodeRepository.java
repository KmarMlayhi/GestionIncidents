package com.example.gestionincidents.repository;


import com.example.gestionincidents.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    Optional<VerificationCode> findByEmail(String email);
    @Transactional
    void deleteByEmail(String email);
}
