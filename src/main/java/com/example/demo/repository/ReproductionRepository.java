package com.example.demo.repository;

import com.example.demo.entity.Reproduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReproductionRepository extends JpaRepository<Reproduction, Long> {

    List<Reproduction> findByGestationConfirmeeTrueAndDateVelageReelIsNull();
}
