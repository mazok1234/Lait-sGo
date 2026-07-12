package com.example.demo.repository.sante;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entity.sante.TraitementSante;

public interface TraitementSanteRepository extends JpaRepository<TraitementSante, Long> {
    List<TraitementSante> findAllByOrderByDateDebutDesc();
    List<TraitementSante> findByEvenementSanteId(Long evenementSanteId);

    @Query("""
        SELECT COALESCE(SUM(t.nbrMedicament * COALESCE(t.medicamentFille.prixUnitaire, 0)), 0)
        FROM TraitementSante t
    """)
    BigDecimal getTotalDepensesMedicaments();

    @Query("""
        SELECT new map(
            MONTH(t.dateDebut) as month,
            YEAR(t.dateDebut) as year,
            SUM(t.nbrMedicament * COALESCE(t.medicamentFille.prixUnitaire, 0)) as total
        )
        FROM TraitementSante t
        GROUP BY YEAR(t.dateDebut), MONTH(t.dateDebut)
        ORDER BY YEAR(t.dateDebut), MONTH(t.dateDebut)
    """)
    List<Map<String, Object>> getDepensesMedicamentsMensuelles();
}