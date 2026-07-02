package com.example.demo.repository;

import com.example.demo.entity.Ration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RationRepository extends JpaRepository<Ration, Long> {

    @Query(value = """
        SELECT r.id AS id, r.nom AS nom, sp.libelle AS stade
        FROM ration r
        JOIN ref_stade_physiologique sp ON sp.id = r.id_stade_physiologique
        ORDER BY r.id
    """, nativeQuery = true)
    List<RationCardProjection> findAllCards();

    @Query(value = """
        SELECT COUNT(*)
        FROM v_ration_recommandee vr
        WHERE vr.ration_id = :rationId
    """, nativeQuery = true)
    long countVachesForRation(@Param("rationId") Long rationId);

    @Query(value = """
        SELECT sp.id AS id, sp.libelle AS libelle, sp.jour_min AS jourMin, sp.jour_max AS jourMax
        FROM ref_stade_physiologique sp
        ORDER BY sp.id
    """, nativeQuery = true)
    List<StadeProjection> findAllStades();

    @Query(value = """
        SELECT COALESCE(v.ufl_total, 0) AS uflTotal, COALESCE(v.cout_j_eur, 0) AS coutTotalAr
        FROM v_ration_nutrition v
        WHERE v.ration_id = :rationId
    """, nativeQuery = true)
    NutritionProjection findNutritionByRationId(@Param("rationId") Long rationId);

    @Query(value = """
        SELECT vr.vache_id AS vacheId,
               vr.numero_boucle AS numeroBoucle,
               vr.jours_en_lait AS joursEnLait,
               vr.phase_actuelle AS phaseActuelle,
               vr.ration_id AS rationId,
               vr.ration_recommandee AS rationRecommandee
        FROM v_ration_recommandee vr
        ORDER BY vr.numero_boucle
    """, nativeQuery = true)
    List<RationActiveVacheProjection> findRationsActivesVaches();

    interface RationCardProjection {
        Long getId();
        String getNom();
        String getStade();
    }

    interface StadeProjection {
        Integer getId();
        String getLibelle();
        Integer getJourMin();
        Integer getJourMax();
    }

    interface NutritionProjection {
        java.math.BigDecimal getUflTotal();
        java.math.BigDecimal getCoutTotalAr();
    }

    interface RationActiveVacheProjection {
        Long getVacheId();
        String getNumeroBoucle();
        Integer getJoursEnLait();
        String getPhaseActuelle();
        Long getRationId();
        String getRationRecommandee();
    }
}
