package com.example.demo.controller;

import com.example.demo.entity.VReproductionSuivi;
import com.example.demo.repository.VacheRepository;
import com.example.demo.service.ReproductionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reproduction")
public class ReproductionController {

    @Autowired
    private ReproductionService reproductionService;

    @Autowired
    private VacheRepository vacheRepository;

    /**
     * Point d'accès de test pour le Back-End du Dashboard de reproduction.
     * Cette URL renvoie toutes les données sous format JSON brut.
     * * URL de test : http://localhost:8080/api/reproduction/dashboard
     */
    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardData(
            @RequestParam(value = "vacheId", required = false) Long vacheId,
            @RequestParam(value = "targetDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate) {

        if (targetDate == null) {
            targetDate = LocalDate.now();
        }

        // Calcul de la semaine (Du Lundi au Dimanche)
        LocalDate startOfWeek = targetDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        // Récupération des données métiers depuis vos services et dépôts
        List<VReproductionSuivi> events = reproductionService.getPlanningEvents(vacheId, startOfWeek, endOfWeek);
        long enAttenteGestation = reproductionService.getAttenteGestationCount();
        double tauxReussiteIA = reproductionService.getTauxReussiteIA();

        // Construction de la réponse JSON de test
        Map<String, Object> response = new HashMap<>();
        
        // Bloc Période de test
        response.put("dateCible", targetDate);
        response.put("debutSemaine", startOfWeek);
        response.put("finSemaine", endOfWeek);
        
        // Bloc Indicateurs (KPIs)
        Map<String, Object> kpis = new HashMap<>();
        kpis.put("tauxReussiteIA", String.format("%.1f", tauxReussiteIA) + " %");
        kpis.put("attenteControleGestation", enAttenteGestation);
        response.put("indicateurs", kpis);
        
        // Bloc Événements du calendrier (IA et vêlages prévus calculés par la vue PostgreSQL)
        response.put("evenementsPlanning", events);

        return response;
    }
}