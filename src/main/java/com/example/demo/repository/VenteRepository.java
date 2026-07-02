package com.example.demo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Vente;

public interface VenteRepository extends JpaRepository<Vente, Integer> {

    public List<Vente> findAllByOrderByDateVenteDesc();
    public List<Vente> findAllByOrderByDateVenteAsc();
    public Page<Vente> findAllByOrderByDateVenteDesc(Pageable pageable);
    public Page<Vente> findAllByOrderByDateVenteAsc(Pageable pageable);
}