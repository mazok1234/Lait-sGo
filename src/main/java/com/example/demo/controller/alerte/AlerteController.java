package com.example.demo.controller.alerte;

import com.example.demo.repository.alerte.RefNiveauAlerteRepository;
import com.example.demo.services.alerte.AlerteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;
import java.util.Set;

@Controller
@RequestMapping("/alertes")
public class AlerteController {

    private static final Set<String> AUTO_ACQUITTED_MODULES = Set.of(
            "Production",
            "Alimentation",
            "Vente",
            "Reproduction"
    );

    private final AlerteService alerteService;
    private final RefNiveauAlerteRepository niveauRepo;

    public AlerteController(AlerteService alerteService,
            RefNiveauAlerteRepository niveauRepo) {
        this.alerteService = alerteService;
        this.niveauRepo    = niveauRepo;
    }

    // Dashboard — toutes alertes (non acquittées en haut, acquittées en bas)
    @GetMapping
    public String dashboard(
            @RequestParam(required = false) String niveau,
            @RequestParam(required = false) String type,
            Model model) {
        model.addAttribute("alertes",      alerteService.listerToutesAlertes(niveau, type));
        model.addAttribute("kpis",         alerteService.compterParNiveau());
        model.addAttribute("niveaux",      niveauRepo.findAllByOrderByOrdreAsc());
        model.addAttribute("types",        alerteService.getTypesDisponibles());
        model.addAttribute("niveauActif",  niveau);
        model.addAttribute("typeActif",    type);
        return "alertes/dashboard";
    }

    // Détail d'une alerte
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("alerte", alerteService.getDetail(id));
        return "alertes/detail";
    }

    // Badge sidebar
    @GetMapping("/api/count")
    @ResponseBody
    public String countBadge() {
        long count = alerteService.compterNonAcquittees();
        return String.valueOf(count);
    }

    // Acquittement manuel
    @PostMapping("/{id}/acquitter")
    public String acquitter(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            String moduleSource = alerteService.getDetail(id).getModuleSource();
            if (moduleSource != null && AUTO_ACQUITTED_MODULES.contains(moduleSource)) {
                redirectAttrs.addFlashAttribute("erreur",
                        "Les alertes de ce module sont acquittées automatiquement lors d'une mise à jour conforme.");
                return "redirect:/alertes/" + id;
            }
            alerteService.acquitter(id);
            redirectAttrs.addFlashAttribute("succes", "Alerte acquittée avec succès.");
        } catch (IllegalStateException e) {
            redirectAttrs.addFlashAttribute("erreur", "Cette alerte est déjà acquittée.");
        } catch (NoSuchElementException e) {
            redirectAttrs.addFlashAttribute("erreur", "Alerte introuvable.");
        }
        return "redirect:/alertes";
    }
}