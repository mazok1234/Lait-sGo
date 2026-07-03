package com.example.demo.repository;

import com.example.demo.entity.VReproductionSuivi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VReproductionSuiviRepository extends JpaRepository<VReproductionSuivi, Long> {

    @Query("SELECT v FROM VReproductionSuivi v WHERE " +
           "(:vacheId IS NULL OR v.vache.id = :vacheId) AND " +
           "(v.dateIa BETWEEN :start AND :end OR v.dateVelagePrevu BETWEEN :start AND :end)")
    List<VReproductionSuivi> findEventsByPeriod(
            @Param("vacheId") Long vacheId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT COUNT(v) FROM VReproductionSuivi v WHERE v.gestationConfirmee IS NULL")
    long countEnAttenteGestation();

    @Query("SELECT COUNT(v) FROM VReproductionSuivi v WHERE v.gestationConfirmee = true")
    long countGestationsReussies();

    @Query("SELECT COUNT(v) FROM VReproductionSuivi v")
    long countTotalIA();
}