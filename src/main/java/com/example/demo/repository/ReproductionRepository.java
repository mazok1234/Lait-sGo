package com.example.demo.repository;

import com.example.demo.entity.Reproduction;
import com.example.demo.entity.Vache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReproductionRepository extends JpaRepository<Reproduction, Long> {

    // Récupérer toutes les IA d'une vache
    List<Reproduction> findByVacheOrderByDateIADesc(Vache vache);

    // Récupérer les IA d'une vache dans une plage de dates
    List<Reproduction> findByVacheAndDateIABetweenOrderByDateIADesc(Vache vache, LocalDate dateDebut, LocalDate dateFin);

    // Récupérer les IA par statut
    List<Reproduction> findByStatutIA(String statutIA);

    // Récupérer les IA d'une vache avec un statut spécifique
    List<Reproduction> findByVacheAndStatutIAOrderByDateIADesc(Vache vache, String statutIA);

    // Compter les IA en attente
    long countByStatutIA(String statutIA);

}
