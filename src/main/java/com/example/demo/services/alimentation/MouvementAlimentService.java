package com.example.demo.services.alimentation;

import com.example.demo.entity.alimentation.MouvementAliment;
import com.example.demo.repository.alimentation.AlimentRepository;       // ← ajouté
import com.example.demo.repository.alimentation.MouvementAlimentRepository;
import com.example.demo.services.alerte.AlerteService;                   // ← ajouté
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MouvementAlimentService {

    private final MouvementAlimentRepository mouvementRepo;
    private final AlerteService              alerteService;   // ← ajouté
    private final AlimentRepository          alimentRepository; // ← ajouté

    public MouvementAlimentService(MouvementAlimentRepository mouvementRepo,
                                    AlerteService alerteService,
                                    AlimentRepository alimentRepository) { // ← ajouté
        this.mouvementRepo     = mouvementRepo;
        this.alerteService     = alerteService;
        this.alimentRepository = alimentRepository;
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
        if (mouvement.getCreatedAt() == null) mouvement.setCreatedAt(LocalDateTime.now());
        if (mouvement.getCreatedBy() == null)  mouvement.setCreatedBy(null);

        MouvementAliment saved = mouvementRepo.save(mouvement);

        // ← INJECTION ALERTE — vérification du seuil après chaque mouvement
        if (mouvement.getAliment() != null && mouvement.getAliment().getId() != null) {
            Long alimentId = mouvement.getAliment().getId();
            BigDecimal stockActuel = getStockActuel(alimentId);

            alimentRepository.findById(alimentId).ifPresent(aliment -> {
                if (aliment.getSeuilAlerteKg() != null
                        && aliment.getSeuilAlerteKg().compareTo(BigDecimal.ZERO) > 0) {

                        if ("sortie".equalsIgnoreCase(mouvement.getTypeMouvement())
                            && stockActuel.compareTo(aliment.getSeuilAlerteKg()) <= 0) {

                        // Stock sous le seuil → alerte urgent (code spécifique par aliment)
                        String typeAlerte = "stock_aliment_bas_" + aliment.getId();
                        alerteService.envoyerAlerte(
                            typeAlerte,
                            "urgent",
                            "Stock insuffisant — " + aliment.getNom(),
                            "Stock actuel : " + stockActuel + " kg"
                                + ", seuil configuré : " + aliment.getSeuilAlerteKg() + " kg.",
                            null
                        );

                    } else if ("entree".equalsIgnoreCase(mouvement.getTypeMouvement())
                            && stockActuel.compareTo(aliment.getSeuilAlerteKg()) > 0) {

                        // Stock reconstitué → acquittement automatique (sur le préfixe + id)
                        String typePrefix = "stock_aliment_bas_" + aliment.getId();
                        alerteService.acquitterAutomatiquement(typePrefix, null);
                    }
                }
            });
        }
        // ← FIN INJECTION

        return saved;
    }

    public void deleteById(Long id) {
        mouvementRepo.deleteById(id);
    }

    public BigDecimal getStockActuel(Long alimentId) {
        BigDecimal stock = mouvementRepo.findStockActuelDepuisVue(alimentId);
        return stock != null ? stock : BigDecimal.ZERO;
    }

    public String getErreurDateSortie(MouvementAliment mouvement) {
        if (mouvement == null || mouvement.getTypeMouvement() == null
                || mouvement.getDateMouvement() == null) return null;
        if (!"sortie".equalsIgnoreCase(mouvement.getTypeMouvement())) return null;
        if (mouvement.getAliment() == null || mouvement.getAliment().getId() == null) return null;

        LocalDate premiereDateEntree = mouvementRepo
                .findPremiereDateEntreeByAlimentId(mouvement.getAliment().getId());
        if (premiereDateEntree == null) {
            return "Impossible de faire une sortie sans entree de stock";
        }
        if (mouvement.getDateMouvement().isBefore(premiereDateEntree)) {
            return "La date d'une sortie ne peut pas etre inferieure a la premiere date d'entree du stock";
        }
        return null;
    }
}