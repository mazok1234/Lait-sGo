package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Reproduction;
import com.example.demo.entity.Vache;

@Repository
public interface ReproductionRepository extends JpaRepository<Reproduction, Long> {

    List<Reproduction> findByGestationConfirmeeTrueAndDateVelageReelIsNull();

    List<Reproduction> findByVacheOrderByDateIADesc(Vache vache);

    List<Reproduction> findByVacheAndDateIABetweenOrderByDateIADesc(Vache vache, LocalDate dateDebut, LocalDate dateFin);

    List<Reproduction> findByStatutIA(String statutIA);

    List<Reproduction> findByVacheAndStatutIAOrderByDateIADesc(Vache vache, String statutIA);

    long countByStatutIA(String statutIA);

}
