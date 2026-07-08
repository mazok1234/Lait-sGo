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

@Service
@Transactional
public class ProductionService {
    private static final String STATUT_LACTATION_ACTIVE = "active";

    private final ProductionRepository productionRepository;
    private final LactationRepository lactationRepository;
    private final RefStatutLactationRepository statutLactationRepository;

    public ProductionService(ProductionRepository productionRepository, LactationRepository lactationRepository,
            RefStatutLactationRepository statutLactationRepository) {
        this.productionRepository = productionRepository;
        this.lactationRepository = lactationRepository;
        this.statutLactationRepository = statutLactationRepository;
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
        productionRepository.save(production);
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

        productionRepository.save(existing);
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
}