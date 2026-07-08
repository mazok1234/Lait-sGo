package com.example.demo.repository.sante;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.sante.TraitementSante;

public interface TraitementSanteRepository extends JpaRepository<TraitementSante, Long> {
    List<TraitementSante> findAllByOrderByDateDebutDesc();
    List<TraitementSante> findByEvenementSanteId(Long evenementSanteId);
}