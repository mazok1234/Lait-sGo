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
}
