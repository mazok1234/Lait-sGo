package com.example.demo.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.services.ReproductionService;

@Controller
@CrossOrigin(origins = "*")
public class ReproductionController {

    @Autowired
    private ReproductionService reproductionService;

    @GetMapping("/reproduction")
    public String afficherDashboardReproduction(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "vacheId", required = false) Long vacheId,
            Model model) {

        LocalDate currentDate = date != null ? date : LocalDate.now();
        LocalDate startOfWeek = currentDate.with(DayOfWeek.MONDAY);
        List<Map<String, Object>> evenements = reproductionService.getSuiviSemaine(startOfWeek, vacheId);

        if (date == null && evenements.isEmpty()) {
            LocalDate dateEvenement = reproductionService.getDateEvenementPertinent(LocalDate.now());
            if (dateEvenement != null) {
                currentDate = dateEvenement;
                startOfWeek = currentDate.with(DayOfWeek.MONDAY);
                evenements = reproductionService.getSuiviSemaine(startOfWeek, vacheId);
            }
        }

        model.addAttribute("currentDate", currentDate);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("startOfWeek", startOfWeek);
        model.addAttribute("endOfWeek", startOfWeek.plusDays(6));
        model.addAttribute("selectedVacheId", vacheId);
        model.addAttribute("compteurs", reproductionService.getDashboardCounters());
        model.addAttribute("vaches", reproductionService.getVachesPourFiltre());
        model.addAttribute("evenements", evenements);

        return "reproduction/dashboard";
    }

    @ResponseBody
    @GetMapping("/api/reproduction/dashboard")
    public Map<String, Object> getDashboardData(
            @RequestParam(value = "vacheId", required = false) Long vacheId,
            @RequestParam(value = "targetDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate) {

        LocalDate currentDate = targetDate != null ? targetDate : LocalDate.now();
        LocalDate startOfWeek = currentDate.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        Map<String, Object> response = new HashMap<>();
        response.put("dateCible", currentDate);
        response.put("debutSemaine", startOfWeek);
        response.put("finSemaine", endOfWeek);
        response.put("indicateurs", reproductionService.getDashboardCounters());
        response.put("evenementsPlanning", reproductionService.getSuiviSemaine(startOfWeek, vacheId));

        return response;
    }

    @ResponseBody
    @GetMapping("/api/reproduction/dashboard/counters")
    public ResponseEntity<Map<String, Object>> getCounters() {
        return ResponseEntity.ok(reproductionService.getDashboardCounters());
    }

    @ResponseBody
    @GetMapping("/api/reproduction/dashboard/suivi")
    public ResponseEntity<List<Map<String, Object>>> getSuiviSemaine(
            @RequestParam("startOfWeek") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startOfWeek,
            @RequestParam(value = "vacheId", required = false) Long vacheId) {
        return ResponseEntity.ok(reproductionService.getSuiviSemaine(startOfWeek, vacheId));
    }
}
