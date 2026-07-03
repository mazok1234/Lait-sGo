package com.example.demo.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entity.Production;

public interface ProductionRepository extends JpaRepository<Production, Long> {

    @Query("SELECT COALESCE(SUM(p.quantiteRestante), 0) FROM Production p")
    BigDecimal getRemainingStock();

    List<Production> findByQuantiteRestanteGreaterThanOrderByDateProductionAsc(BigDecimal quantite);
}