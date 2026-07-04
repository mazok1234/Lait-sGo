package com.example.demo.controller.alerte;

import com.example.demo.repository.alerte.RefNiveauAlerteRepository;
import com.example.demo.repository.alerte.RefTypeAlerteRepository;
import com.example.demo.services.alerte.AlerteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/alertes")
public class AlerteController {
    private final AlerteService alerteService;
    private final RefNiveauAlerteRepository niveauRepo;
    private final RefTypeAlerteRepository typeRepo;

    public AlerteController(AlerteService alerteService,
                             RefNiveauAlerteRepository niveauRepo,
                             RefTypeAlerteRepository typeRepo) {
        this.alerteService = alerteService;
        this.niveauRepo    = niveauRepo;
        this.typeRepo      = typeRepo;
    }

    @GetMapping
    public String dashboard(
        @RequestParam(required = false) String niveau,
        @RequestParam(required = false) Integer type,
        Model model
    ) {
        model.addAttribute("alertes", alerteService.listerNonAcquittees(niveau, type));
        model.addAttribute("kpis",    alerteService.compterParNiveau());
        model.addAttribute("niveaux", niveauRepo.findAll());
        model.addAttribute("types",   typeRepo.findAll());
        model.addAttribute("niveauActif", niveau);
        model.addAttribute("typeActif",   type);
        return "alertes/dashboard";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("alerte", alerteService.getDetail(id));
        return "alertes/detail";
    }

    @PostMapping("/{id}/acquitter")
    public String acquitter(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
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
