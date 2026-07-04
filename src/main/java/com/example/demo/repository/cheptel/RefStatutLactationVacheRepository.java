package com.example.demo.repository.cheptel;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.cheptel.RefStatutLactationVache;

public interface RefStatutLactationVacheRepository extends JpaRepository<RefStatutLactationVache, Integer> {
    Optional<RefStatutLactationVache> findByLibelle(String libelle);
}
