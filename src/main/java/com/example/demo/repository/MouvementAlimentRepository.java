package com.example.demo.repository;

import com.example.demo.entity.MouvementAliment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MouvementAlimentRepository extends JpaRepository<MouvementAliment, Long> {

    List<MouvementAliment> findByAlimentId(Long alimentId);

    List<MouvementAliment> findByTypeMouvement(String typeMouvement);

    List<MouvementAliment> findByAlimentIdAndTypeMouvement(Long alimentId, String typeMouvement);
    @Query("""
        SELECT COALESCE(SUM(CASE WHEN m.typeMouvement = 'entree' THEN m.quantiteKg ELSE -m.quantiteKg END), 0)
        FROM MouvementAliment m WHERE m.aliment.id = :alimentId
    """)
    BigDecimal calculerStockActuel(@Param("alimentId") Long alimentId);

    @Query("""
        SELECT MIN(m.dateMouvement)
        FROM MouvementAliment m
        WHERE m.aliment.id = :alimentId AND LOWER(m.typeMouvement) = 'entree'
    """)
    LocalDate findPremiereDateEntreeByAlimentId(@Param("alimentId") Long alimentId);
}