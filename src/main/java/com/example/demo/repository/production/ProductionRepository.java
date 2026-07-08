package com.example.demo.repository.production;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.production.Production;

public interface ProductionRepository extends JpaRepository<Production, Long> {
    @Query("SELECT COALESCE(SUM(p.quantiteRestante), 0) FROM Production p")
    BigDecimal getRemainingStock();

    List<Production> findByQuantiteRestanteGreaterThanOrderByDateProductionAsc(BigDecimal quantite);

    @Query("SELECT COALESCE(SUM(p.quantiteLitres), 0) FROM Production p WHERE p.dateProduction = :date")
    BigDecimal getTotalProductionByDate(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(p.quantiteLitres), 0) FROM Production p")
    BigDecimal getTotalProduction();

        @Query("""
                        SELECT COALESCE(SUM(p.quantiteLitres), 0)
                        FROM Production p
                        WHERE (:dateDe IS NULL OR p.dateProduction >= :dateDe)
                            AND (:dateA IS NULL OR p.dateProduction <= :dateA)
                        """)
        BigDecimal getTotalProductionBetweenDates(@Param("dateDe") LocalDate dateDe,
                                                                                            @Param("dateA") LocalDate dateA);

    @Query("SELECT new map(MONTH(p.dateProduction) as month, YEAR(p.dateProduction) as year, SUM(p.quantiteLitres) as total) FROM Production p GROUP BY YEAR(p.dateProduction), MONTH(p.dateProduction) ORDER BY year, month")
    List<java.util.Map<String, Object>> getProductionMensuelle();
    
    @Query("""
        SELECT new map(r.libelle as race, SUM(p.quantiteLitres) as totalProduction, COUNT(DISTINCT v.id) as nombreVaches, 
               SUM(p.quantiteLitres)/COUNT(DISTINCT v.id) as productionParVache)
        FROM Production p
        JOIN p.vache v
        JOIN v.race r
        GROUP BY r.libelle
        ORDER BY totalProduction DESC
    """)
    List<java.util.Map<String, Object>> getProductionParRace();
}