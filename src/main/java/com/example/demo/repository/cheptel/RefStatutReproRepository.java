package com.example.demo.repository.cheptel;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.cheptel.RefStatutRepro;

public interface RefStatutReproRepository extends JpaRepository<RefStatutRepro, Integer> {
    Optional<RefStatutRepro> findByLibelle(String libelle);
}
