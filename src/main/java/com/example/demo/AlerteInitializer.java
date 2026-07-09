package com.example.demo;

import com.example.demo.services.alerte.AlerteService;
import com.example.demo.services.alimentation.AlimentService;
import com.example.demo.services.alimentation.MouvementAlimentService;
import com.example.demo.services.reproduction.ReproductionAlerteService;
import com.example.demo.services.sante.TraitementSanteService;
import com.example.demo.services.sante.VaccinService;
import com.example.demo.services.vente.VenteService;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AlerteInitializer {

    @Autowired
    private AlimentService alimentService;

    @Autowired
    private AlerteService alerteService;

    @Autowired
    private MouvementAlimentService mouvementAlimentService;

    @Autowired
    private VaccinService vaccinService;

    @Autowired
    private ReproductionAlerteService reproductionAlerteService;

    @Autowired
    private TraitementSanteService traitementSanteService;

    @Autowired
    private VenteService venteService;

    @EventListener(ApplicationReadyEvent.class)
    public void initAlertes() {
        runAlertesScan();
    }

    public void rescanAlertes() {
        runAlertesScan();
    }

    private void runAlertesScan() {
        try {
            verifierStockAlimentation();
            genererAlertesVaccination();
            genererAlertesReproduction();
            genererAlertesTraitements();
            genererAlertesStockLait();
            System.out.println("[AlerteInitializer] Initialisation des alertes terminée.");
        } catch (Exception e) {
            System.err.println("[AlerteInitializer] Erreur d'initialisation des alertes : " + e.getMessage());
        }
    }

    private void verifierStockAlimentation() {
        alimentService.findAll().forEach(aliment -> {
            BigDecimal stock = mouvementAlimentService.getStockActuel(aliment.getId());
            if (aliment.getSeuilAlerteKg() != null
                    && aliment.getSeuilAlerteKg().compareTo(BigDecimal.ZERO) > 0
                    && stock.compareTo(aliment.getSeuilAlerteKg()) <= 0) {
                alerteService.envoyerAlerte(
                        AlerteService.STOCK_ALIMENT_BAS_PREFIX + "_" + aliment.getId(),
                        "urgent",
                        "Stock insuffisant — " + aliment.getNom(),
                        "Stock actuel : " + stock + " kg, seuil : " + aliment.getSeuilAlerteKg() + " kg.",
                        null);
            }
        });
        System.out.println("[AlerteInitializer] Stocks aliments vérifiés.");
    }

    private void genererAlertesVaccination() {
        vaccinService.getVaccinsPrioritaires();
        System.out.println("[AlerteInitializer] Alertes vaccination générées.");
    }

    private void genererAlertesReproduction() {
        reproductionAlerteService.getAlertesVelage();
        System.out.println("[AlerteInitializer] Alertes reproduction générées.");
    }

    private void genererAlertesTraitements() {
        traitementSanteService.genererAlertesTraitementsActifs();
        System.out.println("[AlerteInitializer] Alertes traitement générées.");
    }

    private void genererAlertesStockLait() {
        venteService.genererAlerteStockLait();
        System.out.println("[AlerteInitializer] Alertes stock lait générées.");
    }
}
