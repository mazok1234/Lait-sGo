package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Vache;
import java.util.Optional;

public interface VacheRepository extends JpaRepository<Vache, Long> {
    Optional<Vache> findByNumeroBoucle(String numeroBoucle);

    long countByStatut_Code(String code);
}