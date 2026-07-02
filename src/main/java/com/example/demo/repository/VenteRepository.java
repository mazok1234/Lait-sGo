package com.example.demo.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Vente;

public interface VenteRepository extends JpaRepository<Vente, Integer> {

    public List<Vente> findAllByOrderByDateVenteDesc();
    public List<Vente> findAllByOrderByDateVenteAsc();
    public Page<Vente> findAllByOrderByDateVenteDesc(Pageable pageable);
    public Page<Vente> findAllByOrderByDateVenteAsc(Pageable pageable);


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