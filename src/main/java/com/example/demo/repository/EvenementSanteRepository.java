package com.example.demo.repository;

import com.example.demo.entity.EvenementSante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvenementSanteRepository extends JpaRepository<EvenementSante, Long> {
}