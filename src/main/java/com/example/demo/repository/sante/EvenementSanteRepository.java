package com.example.demo.repository.sante;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.sante.EvenementSante;

public interface EvenementSanteRepository extends JpaRepository<EvenementSante, Long> {
    List<EvenementSante> findAllByOrderByDateEvenementDesc();
}