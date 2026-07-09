package com.example.demo.repository.sante;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.sante.Medicament;

@Repository
public interface MedicamentRepository extends JpaRepository<Medicament, Long> {

    Optional<Medicament> findByNomIgnoreCase(String nom);

    @Query(value = "SELECT m.* FROM medicament m " +
            "JOIN maladie_medicament mm ON mm.medicament_id = m.id " +
            "WHERE mm.maladie_id = :maladieId", nativeQuery = true)
    List<Medicament> findByMaladieId(@Param("maladieId") Long maladieId);
}