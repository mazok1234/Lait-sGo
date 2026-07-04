package com.example.demo.repository.cheptel;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.cheptel.RefStatutVie;

public interface RefStatutVieRepository extends JpaRepository<RefStatutVie, Integer> {
    Optional<RefStatutVie> findByLibelle(String libelle);
}
