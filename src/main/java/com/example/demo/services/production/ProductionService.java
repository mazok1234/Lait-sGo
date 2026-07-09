package com.example.demo.services.production;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.production.Lactation;
import com.example.demo.entity.production.Production;
import com.example.demo.entity.production.RefStatutLactation;
import com.example.demo.entity.cheptel.Vache;
import com.example.demo.repository.production.LactationRepository;
import com.example.demo.repository.production.ProductionRepository;
import com.example.demo.repository.production.RefStatutLactationRepository;
import com.example.demo.services.alerte.AlerteService;

@Service
@Transactional
public class ProductionService {
    private static final String STATUT_LACTATION_ACTIVE = "active";

    private final ProductionRepository productionRepository;
    private final LactationRepository lactationRepository;
    private final RefStatutLactationRepository statutLactationRepository;
    private final AlerteService alerteService;

    public ProductionService(ProductionRepository productionRepository, LactationRepository lactationRepository,
            RefStatutLactationRepository statutLactationRepository, AlerteService alerteService) {
        this.productionRepository = productionRepository;
        this.lactationRepository = lactationRepository;
        this.statutLactationRepository = statutLactationRepository;
        this.alerteService = alerteService;
    }

    public List<Production> getAllProductions() {
        return productionRepository.findAll();
    }

    public Production getById(Long id) {
        return productionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Production introuvable: " + id));
    }

    public void saveProduction(Production production) {
        BigDecimal matin = production.getQuantiteMatin() != null ? production.getQuantiteMatin() : BigDecimal.ZERO;
        BigDecimal soir = production.getQuantiteSoir() != null ? production.getQuantiteSoir() : BigDecimal.ZERO;
        production.setQuantiteLitres(matin.add(soir));
        production.setQuantiteRestante(production.getQuantiteLitres());
        production.setLactation(resolveLactationActive(production.getVache()));
        
        productionRepository.saveAndFlush(production);
        
        this.checkBaisseCritique(production);
    }

    public void updateProduction(Long id, Production form) {
        Production existing = getById(id);
        existing.setVache(form.getVache());
        existing.setDateProduction(form.getDateProduction());
        existing.setQuantiteMatin(form.getQuantiteMatin());
        existing.setQuantiteSoir(form.getQuantiteSoir());

        BigDecimal matin = existing.getQuantiteMatin() != null ? existing.getQuantiteMatin() : BigDecimal.ZERO;
        BigDecimal soir = existing.getQuantiteSoir() != null ? existing.getQuantiteSoir() : BigDecimal.ZERO;
        existing.setQuantiteLitres(matin.add(soir));
        existing.setLactation(resolveLactationActive(existing.getVache()));

        productionRepository.saveAndFlush(existing);
        
        this.checkBaisseCritique(existing);
    }

    public void delete(Long id) {
        productionRepository.deleteById(id);
    }

    public BigDecimal getTotalProductionDuJour() {
        return productionRepository.getTotalProductionByDate(LocalDate.now());
    }

    public BigDecimal getStockRestant() {
        return productionRepository.getRemainingStock();
    }

    private Lactation resolveLactationActive(Vache vache) {
        return lactationRepository
                .findFirstByVache_IdAndStatut_CodeOrderByDateDebutDesc(vache.getId(), STATUT_LACTATION_ACTIVE)
                .orElseGet(() -> creerLactationActive(vache));
    }

    private Lactation creerLactationActive(Vache vache) {
        RefStatutLactation statutActive = statutLactationRepository.findByCode(STATUT_LACTATION_ACTIVE)
                .orElseThrow(() -> new IllegalStateException("Statut de lactation introuvable: " + STATUT_LACTATION_ACTIVE));

        long lactationsExistantes = lactationRepository.countByVache_Id(vache.getId());

        Lactation lactation = new Lactation();
        lactation.setVache(vache);
        lactation.setNumeroLactation((short) (lactationsExistantes + 1));
        lactation.setDateDebut(LocalDate.now());
        lactation.setStatut(statutActive);
        return lactationRepository.save(lactation);
    }
    
    public List<java.util.Map<String, Object>> getProductionParRace() {
        return productionRepository.getProductionParRace();
    }

    public boolean checkBaisseCritique(Production p) {
        if (p == null || p.getVache() == null || p.getQuantiteLitres() == null) return false;
        
        List<Production> historique = productionRepository.findByVacheOrderByDateProductionDesc(p.getVache());
        if (historique == null || historique.isEmpty()) return false;

        BigDecimal somme = BigDecimal.ZERO;
        int count = 0;
        
        for (Production hist : historique) {
            // CORRECTION 2 : Si l'ID correspond à la ligne actuelle, ou si c'est la même date, on l'exclut du calcul de la moyenne
            if ((p.getId() != null && hist.getId().equals(p.getId())) || 
                (hist.getDateProduction() != null && p.getDateProduction() != null && !hist.getDateProduction().isBefore(p.getDateProduction()))) {
                continue;
            }
            if (hist.getQuantiteLitres() != null) {
                somme = somme.add(hist.getQuantiteLitres());
                count++;
            }
            if (count >= 7) break;
        }

        if (count > 0) {
            BigDecimal moyenne = somme.divide(BigDecimal.valueOf(count), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal seuilCritique = moyenne.multiply(BigDecimal.valueOf(0.80)); // -20%
            
            boolean estEnBaisse = p.getQuantiteLitres().compareTo(seuilCritique) < 0;

            if (estEnBaisse) {
                String description = "Baisse critique de production pour la vache " + p.getVache().getNumeroBoucle() 
                        + " : " + p.getQuantiteLitres() + "L saisis (Seuil critique à " + seuilCritique + "L).";
                
                alerteService.envoyerAlerte(
                    "baisse_production", 
                    "attention", 
                    "Baisse prod. Lait — " + p.getVache().getNumeroBoucle(), 
                    description, 
                    p.getVache().getId()
                );
            } else {
                alerteService.acquitterAutomatiquement("baisse_production", p.getVache().getId());
            }
            
            return estEnBaisse;
        }
        return false;
    }
}
