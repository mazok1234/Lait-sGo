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

    @Query("SELECT COALESCE(SUM(v.quantiteLait), 0) FROM Vente v WHERE v.dateVente = :date")
    BigDecimal getTotalVenduByDate(@Param("date") LocalDate date);

@Query("""
    select v
    from Vente v
    where (:min IS NULL or (v.prixUnitaire * v.quantiteLait) >= :min)
      and (:max IS NULL or (v.prixUnitaire * v.quantiteLait) <= :max)
""")
Page<Vente> findByPrixTotalBetweenDateDesc(
        @Param("min") BigDecimal min,
        @Param("max") BigDecimal max,
        Pageable pageable
);
}
