package com.example.demo.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Production;
import com.example.demo.entity.Vente;
import com.example.demo.repository.ProductionRepository;
import com.example.demo.repository.UtilisateurRepository;
import com.example.demo.repository.VenteRepository;

@Service
@Transactional
public class VenteService {

    private final ProductionRepository productionRepository;
    private final VenteRepository venteRepository;
    private final UtilisateurRepository utilisateurRepository;

    public VenteService(
            ProductionRepository productionRepository,
            VenteRepository venteRepository,
            UtilisateurRepository utilisateurRepository) {

        this.productionRepository = productionRepository;
        this.venteRepository = venteRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    public void effectuerVente(BigDecimal quantite,BigDecimal prixUnitaire, LocalDate dateVente) {



        BigDecimal stock = productionRepository.getRemainingStock();

        if (stock.compareTo(quantite) < 0) {
            throw new RuntimeException(
                    "Stock insuffisant. Stock actuel : " + stock + " L");
        }

        List<Production> productions = productionRepository.findByQuantiteRestanteGreaterThanOrderByDateProductionAsc(BigDecimal.ZERO);

        BigDecimal restant = quantite;

        List<Production> modified = new ArrayList<>();

        for (Production production : productions) {

            if (restant.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal disponible = production.getQuantiteRestante();

            if (disponible.compareTo(restant) >= 0) {

                production.setQuantiteRestante(disponible.subtract(restant));

                restant = BigDecimal.ZERO;

            } else {

                restant = restant.subtract(disponible);

                production.setQuantiteRestante(BigDecimal.ZERO);
            }

            modified.add(production);
        }

        productionRepository.saveAll(modified);

        Vente vente = new Vente();

        vente.setDateVente(dateVente);
        vente.setQuantiteLait(quantite);
        vente.setPrixUnitaire(prixUnitaire);
        vente.setCreatedAt(LocalDateTime.now());

        // Temporary
        vente.setCreatedBy( utilisateurRepository.findById(1L).orElseThrow());

        venteRepository.save(vente);
    }

}