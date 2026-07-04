package com.example.demo.services.vente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.production.Production;
import com.example.demo.entity.vente.Vente;
import com.example.demo.repository.production.ProductionRepository;
import com.example.demo.repository.auth.UtilisateurRepository;
import com.example.demo.repository.vente.VenteRepository;

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

    public Vente effectuerVente(BigDecimal quantite,BigDecimal prixUnitaire, LocalDate dateVente) {
        if (prixUnitaire == null || prixUnitaire.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Le prix unitaire doit être supérieur à 0.");
        }

        BigDecimal maxPrix = new BigDecimal("9999.99");
        if (prixUnitaire.compareTo(maxPrix) > 0) {
            throw new RuntimeException("Le prix unitaire ne peut pas dépasser 9 999,99 Ar/L (limite de la base de données).");
        }

        if (quantite == null || quantite.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("La quantité doit être supérieure à 0.");
        }

        if (dateVente == null || dateVente.isAfter(LocalDate.now())) {
            throw new RuntimeException("La date de vente ne peut pas être dans le futur.");
        }

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

        vente.setCreatedBy( utilisateurRepository.findById(1L).orElseThrow());

        venteRepository.save(vente);
        return vente;
    }

    public Page<Vente> getMostRecentVentes(int page, int size){
        return venteRepository.findAllByOrderByDateVenteDesc(Pageable.ofSize(size).withPage(page));
    }

    public Page<Vente> getOldestVentes(int page, int size){
        return venteRepository.findAllByOrderByDateVenteAsc(Pageable.ofSize(size).withPage(page));
    }

    public Page<Vente> findByPrixTotalBetweenDateDesc( BigDecimal min, BigDecimal max, int page , int size){
        return venteRepository.findByPrixTotalBetweenDateDesc(min, max, Pageable.ofSize(size).withPage(page));
    }
    public Vente findById(Integer id) {
        return venteRepository.findById(id).orElseThrow(() -> new RuntimeException("Vente not found"));
    }
}
