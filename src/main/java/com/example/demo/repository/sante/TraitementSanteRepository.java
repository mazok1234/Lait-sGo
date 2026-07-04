package com.example.demo.repository.sante;

import com.example.demo.entity.sante.TraitementSante;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TraitementSanteRepository extends JpaRepository<TraitementSante, Long> {
    @EntityGraph(attributePaths = {"evenementSante", "evenementSante.vache", "evenementSante.maladie", "medicament"})
    List<TraitementSante> findAllByOrderByDateDebutDesc();
}
