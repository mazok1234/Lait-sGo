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
}
