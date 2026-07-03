package com.example.demo.controller;

import com.example.demo.dto.AlerteReproductionDTO;
import com.example.demo.dto.ReproductionDTO;
import com.example.demo.entity.Vache;
import com.example.demo.repository.VacheRepository;
import com.example.demo.services.ReproductionAlerteService;
import com.example.demo.services.ReproductionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reproduction")
public class ReproductionController {

    @Autowired
    private ReproductionService reproductionService;

    @Autowired
    private ReproductionAlerteService alerteService;

    @Autowired
    private VacheRepository vacheRepository;

    // ===== Dashboard =====
    @GetMapping
    public String afficherDashboardReproduction(
            @RequestParam(value = "mois", required = false) Integer mois,
            @RequestParam(value = "annee", required = false) Integer annee,
            @RequestParam(value = "vacheId", required = false) Long vacheId,
            Model model) {

        LocalDate now = LocalDate.now();
        int selectedMonth = mois != null ? mois : now.getMonthValue();
        int selectedYear = annee != null ? annee : now.getYear();

        if (vacheId != null && vacheId <= 0) {
            vacheId = null;
        }

        List<Map<String, Object>> chaleurs = reproductionService.getChaleurs(mois, annee, vacheId);

        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("selectedVacheId", vacheId);
        model.addAttribute("compteurs", reproductionService.getDashboardCounters());
        model.addAttribute("vaches", reproductionService.getVachesPourFiltre());
        model.addAttribute("chaleurs", chaleurs);

        return "reproduction/dashboard";
    }

    @ResponseBody
    @GetMapping("/api/dashboard")
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
    @GetMapping("/api/dashboard/counters")
    public Map<String, Object> getCounters() {
        return reproductionService.getDashboardCounters();
    }

    @ResponseBody
    @GetMapping("/api/dashboard/suivi")
    public List<Map<String, Object>> getSuiviSemaine(
            @RequestParam("startOfWeek") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startOfWeek,
            @RequestParam(value = "vacheId", required = false) Long vacheId) {
        return reproductionService.getSuiviSemaine(startOfWeek, vacheId);
    }

    // ===== Alertes =====
    @GetMapping("/alertes")
    public String afficherAlertes(Model model) {
        List<AlerteReproductionDTO> alertes = alerteService.getAlertesVelage();
        model.addAttribute("alertes", alertes);
        return "reproduction/reproduction_alertes";
    }

    // ===== IA / Insémination =====
    @GetMapping("/nouvelle")
    public String afficherFormulaire(Model model) {
        List<Vache> vaches = vacheRepository.findAll();
        model.addAttribute("vaches", vaches);
        model.addAttribute("statuts", List.of("en_attente", "gestante", "echouee"));
        return "reproduction/nouvelle_ia";
    }

    @PostMapping("/enregistrer")
    public String enregistrerIA(
            @RequestParam Long vacheId,
            @RequestParam String dateIA,
            @RequestParam(required = false) String typeInjection,
            @RequestParam String semence,
            @RequestParam String inseminateur,
            Model model) {
        try {
            ReproductionDTO dto = new ReproductionDTO();
            dto.setVacheId(vacheId);
            dto.setDateIA(LocalDate.parse(dateIA));
            dto.setTypeInjection(typeInjection);
            dto.setSemence(semence);
            dto.setInséminateur(inseminateur);

            reproductionService.enregistrerIA(dto);

            model.addAttribute("success", "✅ Insémination enregistrée avec succès !");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "❌ Erreur : " + e.getMessage());
        }
        model.addAttribute("vaches", vacheRepository.findAll());
        return "reproduction/nouvelle_ia";
    }

    @GetMapping("/historique/{vacheId}")
    public String afficherHistorique(@PathVariable Long vacheId, Model model) {
        Vache vache = vacheRepository.findById(vacheId)
                .orElseThrow(() -> new RuntimeException("Vache non trouvée"));

        List<ReproductionDTO> historique = reproductionService.getHistoriqueParVache(vacheId);

        model.addAttribute("vache", vache);
        model.addAttribute("historique", historique);

        return "reproduction/historique_ia";
    }

    @PostMapping("/update-statut/{reproductionId}")
    public String updateStatut(
            @PathVariable Long reproductionId,
            @RequestParam String statut,
            @RequestParam(required = false) LocalDate dateConfirmation,
            RedirectAttributes redirectAttributes) {
        try {
            ReproductionDTO reproduction = reproductionService.getReproductionById(reproductionId);
            reproductionService.updateStatutIA(reproductionId, statut, dateConfirmation);
            redirectAttributes.addFlashAttribute("success", "Statut mis à jour avec succès !");
            return "redirect:/reproduction/historique/" + reproduction.getVacheId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/reproduction/en-attente";
        }
    }

    @GetMapping("/en-attente")
    public String afficherIAEnAttente(Model model) {
        List<ReproductionDTO> iaEnAttente = reproductionService.getIAEnAttente();
        model.addAttribute("iaEnAttente", iaEnAttente);
        return "reproduction/ia_en_attente";
    }

    @PostMapping("/supprimer/{reproductionId}")
    public String supprimerIA(@PathVariable Long reproductionId, RedirectAttributes redirectAttributes) {
        try {
            ReproductionDTO reproduction = reproductionService.getReproductionById(reproductionId);
            Long vacheId = reproduction.getVacheId();
            reproductionService.deleteIA(reproductionId);
            redirectAttributes.addFlashAttribute("success", "Insémination supprimée !");
            return "redirect:/reproduction/historique/" + vacheId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression : " + e.getMessage());
            return "redirect:/reproduction/en-attente";
        }
    }
}