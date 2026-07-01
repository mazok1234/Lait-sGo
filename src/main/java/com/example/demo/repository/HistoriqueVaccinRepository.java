package com.example.demo.repository;

import com.example.demo.entity.HistoriqueVaccin;
import com.example.demo.entity.Vache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface HistoriqueVaccinRepository extends JpaRepository<HistoriqueVaccin, Integer> {

    List<HistoriqueVaccin> findByVache(Vache vache);

    @Query("""
        SELECT h FROM HistoriqueVaccin h
        WHERE h.dateVaccination = (
            SELECT MAX(h2.dateVaccination)
            FROM HistoriqueVaccin h2
            WHERE h2.protocoleVaccin = h.protocoleVaccin
        )
        """)
    List<HistoriqueVaccin> findLastByVaccin();
}