package com.example.demo.repository.vente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.vente.Vente;

public interface VenteRepository extends JpaRepository<Vente, Integer> {
        public List<Vente> findAllByOrderByDateVenteDesc();

        public List<Vente> findAllByOrderByDateVenteAsc();

        public Page<Vente> findAllByOrderByDateVenteDesc(Pageable pageable);

        public Page<Vente> findAllByOrderByDateVenteAsc(Pageable pageable);

        @Query("SELECT COALESCE(SUM(v.quantite), 0) FROM Vente v WHERE v.dateVente = :date AND v.produit.code = 'LAIT'")
        BigDecimal getTotalVenduByDate(@Param("date") LocalDate date);

        @Query("SELECT COALESCE(SUM(v.quantite * v.prixUnitaire), 0) FROM Vente v")
        BigDecimal getTotalRevenus();

        @Query("""
                        SELECT COALESCE(SUM(v.quantite * v.prixUnitaire), 0)
                        FROM Vente v
                        WHERE v.dateVente >= COALESCE(:dateDe, v.dateVente)
                          AND v.dateVente <= COALESCE(:dateA, v.dateVente)
                        """)
        BigDecimal getTotalRevenusBetweenDates(@Param("dateDe") LocalDate dateDe,
                        @Param("dateA") LocalDate dateA);

        @Query("SELECT new map(MONTH(v.dateVente) as month, YEAR(v.dateVente) as year, SUM(v.quantite * v.prixUnitaire) as total) FROM Vente v GROUP BY YEAR(v.dateVente), MONTH(v.dateVente) ORDER BY year, month")
        List<java.util.Map<String, Object>> getRevenusMensuels();

        @Query("""
                            select v
                            from Vente v
                            where (:min IS NULL or (v.prixUnitaire * v.quantite) >= :min)
                              and (:max IS NULL or (v.prixUnitaire * v.quantite) <= :max)
                        """)
        Page<Vente> findByPrixTotalBetweenDateVenteDesc(
                        @Param("min") BigDecimal min,
                        @Param("max") BigDecimal max,
                        Pageable pageable);
}
