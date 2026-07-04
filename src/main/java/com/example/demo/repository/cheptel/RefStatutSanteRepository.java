package com.example.demo.repository.cheptel;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.cheptel.RefStatutSante;

public interface RefStatutSanteRepository extends JpaRepository<RefStatutSante, Integer> {
    Optional<RefStatutSante> findByLibelle(String libelle);
}
