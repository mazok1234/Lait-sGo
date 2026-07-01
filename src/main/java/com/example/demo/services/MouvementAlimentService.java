package com.example.demo.services;

import com.example.demo.entity.MouvementAliment;
import com.example.demo.repository.MouvementAlimentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MouvementAlimentService {

    private final MouvementAlimentRepository mouvementRepo;

    public MouvementAlimentService(MouvementAlimentRepository mouvementRepo) {
        this.mouvementRepo = mouvementRepo;
    }

    public List<MouvementAliment> findAll() {
        return mouvementRepo.findAll();
    }

    public List<MouvementAliment> findByAlimentId(Long alimentId) {
        return mouvementRepo.findByAlimentId(alimentId);
    }

    public MouvementAliment findById(Long id) {
        return mouvementRepo.findById(id).orElse(null);
    }

    public MouvementAliment save(MouvementAliment mouvement) {
        if (mouvement.getId() != null) {
            MouvementAliment existant = findById(mouvement.getId());
            if (existant != null) {
                mouvement.setCreatedAt(existant.getCreatedAt());
                mouvement.setCreatedBy(existant.getCreatedBy());
            }
        }

        if (mouvement.getCreatedAt() == null) {
            mouvement.setCreatedAt(LocalDateTime.now());
        }
        if (mouvement.getCreatedBy() == null) {
            mouvement.setCreatedBy(null); //mila manao anty zay manao anle login
        }
        return mouvementRepo.save(mouvement);
    }

    public void deleteById(Long id) {
        mouvementRepo.deleteById(id);
    }

    public BigDecimal getStockActuel(Long alimentId) {
        BigDecimal stock = mouvementRepo.calculerStockActuel(alimentId);
        return stock != null ? stock : BigDecimal.ZERO;
    }

    public String getErreurDateSortie(MouvementAliment mouvement) {
        if (mouvement == null || mouvement.getTypeMouvement() == null || mouvement.getDateMouvement() == null) {
            return null;
        }

        if (!"sortie".equalsIgnoreCase(mouvement.getTypeMouvement())) {
            return null;
        }

        if (mouvement.getAliment() == null || mouvement.getAliment().getId() == null) {
            return null;
        }

        LocalDate premiereDateEntree = mouvementRepo.findPremiereDateEntreeByAlimentId(mouvement.getAliment().getId());
        if (premiereDateEntree == null) {
            return "Impossible de faire une sortie sans entree de stock";
        }

        if (mouvement.getDateMouvement().isBefore(premiereDateEntree)) {
            return "La date d'une sortie ne peut pas etre inferieure a la premiere date d'entree du stock";
        }

        return null;
    }
}
