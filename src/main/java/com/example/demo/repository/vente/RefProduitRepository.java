package com.example.demo.repository.vente;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.vente.RefProduit;

public interface RefProduitRepository extends JpaRepository<RefProduit, Integer> {
    Optional<RefProduit> findByCode(String code);

    List<RefProduit> findAllByOrderByLibelleAsc();
}