package com.example.demo.repository.alimentation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.alimentation.Ration;

@Repository
public interface RationRepository extends JpaRepository<Ration, Long> {
    boolean existsByIdPhaseLactation(Integer idPhaseLactation);

    @Query(value = """
            SELECT COUNT(*) > 0
            FROM ration r
            WHERE r.id_phase_lactation = :phaseId
                AND (:rationId IS NULL OR r.id <> :rationId)
    """, nativeQuery = true)
    boolean existsByPhaseForOtherRation(@Param("phaseId") Integer phaseId,
                                                                                @Param("rationId") Long rationId);

    @Query(value = """
        SELECT r.id AS id, r.nom AS nom, COALESCE(sp.libelle, 'Toutes phases') AS stade
        FROM ration r
        LEFT JOIN ref_phase_lactation sp ON sp.id = r.id_phase_lactation
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
        SELECT COUNT(DISTINCT ar.vache_id)
        FROM affectation_ration_vache ar
        WHERE ar.ration_id = :rationId AND ar.actif = TRUE
    """, nativeQuery = true)
    long countAffectationsForRation(@Param("rationId") Long rationId);

    @Query(value = """
        SELECT sp.id AS id, sp.libelle AS libelle, sp.jour_min AS jourMin, sp.jour_max AS jourMax
        FROM ref_phase_lactation sp
        ORDER BY sp.id
    """, nativeQuery = true)
    List<StadeProjection> findAllStades();

    @Query(value = """
        SELECT sp.id AS id, sp.libelle AS libelle, sp.jour_min AS jourMin, sp.jour_max AS jourMax
        FROM ref_phase_lactation sp
        LEFT JOIN ration r ON r.id_phase_lactation = sp.id
        WHERE r.id IS NULL
        ORDER BY sp.id
    """, nativeQuery = true)
    List<StadeProjection> findAvailableStades();

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

    @Query(value = """
        SELECT ar.vache_id AS vacheId,
               v.numero_boucle AS numeroBoucle,
               NULL AS joursEnLait,
               'Affectation manuelle' AS phaseActuelle,
               ar.ration_id AS rationId,
               r.nom AS rationRecommandee
        FROM affectation_ration_vache ar
        JOIN vache v ON v.id = ar.vache_id
        JOIN ration r ON r.id = ar.ration_id
        WHERE ar.actif = TRUE
          AND r.id_phase_lactation IS NULL
        ORDER BY v.numero_boucle
    """, nativeQuery = true)
    List<RationActiveVacheProjection> findAffectationsManuelles();

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


    @Query(value = """
        SELECT vache_id AS vacheId,
               numero_boucle AS numeroBoucle,
               score_bcs AS scoreBcs,
               score_locomotion AS scoreLocomotion,
               jours_en_lait AS joursEnLait,
               phase_actuelle AS phaseActuelle,
               phase_id AS phaseId,
               production_moyenne_7j AS productionMoyenne7j,
               statut_sante_id AS statutSanteId,
               statut_sante_libelle AS statutSanteLibelle,
               ration_actuelle_id AS rationActuelleId,
               ration_actuelle_nom AS rationActuelleNom,
               ration_suggeree_id AS rationSuggereeId,
               ration_suggeree_nom AS rationSuggereeNom,
               priorite_suggestion AS prioriteSuggestion,
               raison_suggestion AS raisonSuggestion
        FROM v_suggestion_ration
        ORDER BY priorite_suggestion DESC NULLS LAST, numero_boucle
    """, nativeQuery = true)
    List<SuggestionProjection> findAllSuggestions();

    interface SuggestionProjection {
        Long getVacheId();
        String getNumeroBoucle();
        java.math.BigDecimal getScoreBcs();
        Short getScoreLocomotion();
        Integer getJoursEnLait();
        String getPhaseActuelle();
        Integer getPhaseId();
        java.math.BigDecimal getProductionMoyenne7j();
        Integer getStatutSanteId();
        String getStatutSanteLibelle();
        Long getRationActuelleId();
        String getRationActuelleNom();
        Long getRationSuggereeId();
        String getRationSuggereeNom();
        Integer getPrioriteSuggestion();
        String getRaisonSuggestion();
    }
}
