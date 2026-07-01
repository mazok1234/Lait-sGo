package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Vente;

public interface VenteRepository extends JpaRepository<Vente, Integer> {

}