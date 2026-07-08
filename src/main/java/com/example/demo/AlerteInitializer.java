package com.example.demo;

import com.example.demo.services.alerte.AlerteService;
import com.example.demo.services.alimentation.AlimentService;
import com.example.demo.services.alimentation.MouvementAlimentService;
import com.example.demo.services.reproduction.ReproductionAlerteService;
import com.example.demo.services.sante.VaccinService;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AlerteInitializer {

    @Autowired
    private ReproductionAlerteService reproductionAlerteService;

    @Autowired
    private VaccinService vaccinService;

    @Autowired
    private AlimentService alimentService;

    @Autowired
    private AlerteService alerteService;

    @Autowired
    private MouvementAlimentService mouvementAlimentService;

    @EventListener(ApplicationReadyEvent.class)
    public void initAlertes() {
        try {
            // Déclenche la détection des vêlages proches
            reproductionAlerteService.getAlertesVelage();
            System.out.println("[AlerteInitializer] Alertes vêlage vérifiées.");
        } catch (Exception e) {
            System.err.println("[AlerteInitializer] Erreur vêlage : " + e.getMessage());
        }

        try {
            // Déclenche la détection des vaccins prioritaires
            vaccinService.getVaccinsPrioritaires();
            System.out.println("[AlerteInitializer] Alertes vaccins vérifiées.");
        } catch (Exception e) {
            System.err.println("[AlerteInitializer] Erreur vaccins : " + e.getMessage());
        }

        try {
            alimentService.findAll().forEach(aliment -> {
                BigDecimal stock = mouvementAlimentService.getStockActuel(aliment.getId());
                if (aliment.getSeuilAlerteKg() != null
                        && aliment.getSeuilAlerteKg().compareTo(BigDecimal.ZERO) > 0
                        && stock.compareTo(aliment.getSeuilAlerteKg()) <= 0) {
                    alerteService.envoyerAlerte(
                            "stock_aliment_bas_" + aliment.getId(), // ex: "stock_aliment_bas_1"
                            "urgent",
                            "Stock insuffisant — " + aliment.getNom(),
                            "Stock actuel : " + stock + " kg, seuil : " + aliment.getSeuilAlerteKg() + " kg.",
                            null);
                }
            });
            System.out.println("[AlerteInitializer] Stocks aliments vérifiés.");
        } catch (Exception e) {
            System.err.println("[AlerteInitializer] Erreur stocks : " + e.getMessage());
        }
    }
}