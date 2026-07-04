package com.example.demo.controller.production;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.production.Production;
import com.example.demo.entity.cheptel.Vache;
import com.example.demo.repository.cheptel.VacheRepository;
import com.example.demo.repository.vente.VenteRepository;
import com.example.demo.services.production.ProductionService;
import com.example.demo.services.cheptel.StatutLactationVacheService;

@Controller
@RequestMapping("/productions")
public class ProductionController {
    private static final String STATUT_VACHE_EN_LACTATION = "En_lactation";

    private final ProductionService productionService;
    private final VacheRepository vacheRepository;
    private final VenteRepository venteRepository;
    private final StatutLactationVacheService statutLactationService;

    public ProductionController(ProductionService productionService, VacheRepository vacheRepository,
            VenteRepository venteRepository, StatutLactationVacheService statutLactationService) {
        this.productionService = productionService;
        this.vacheRepository = vacheRepository;
        this.venteRepository = venteRepository;
        this.statutLactationService = statutLactationService;
    }

    private List<Vache> vachesEnLactation() {
        Integer statutId = statutLactationService.getByLibelle(STATUT_VACHE_EN_LACTATION).getId();
        return vacheRepository.findAllById(statutLactationService.findVacheIdsByStatut(statutId));
    }

    @GetMapping
    public String listeProductions(Model model) {
        model.addAttribute("productions", productionService.getAllProductions());
        model.addAttribute("totalProductionJour", productionService.getTotalProductionDuJour());
        model.addAttribute("totalSortieJour", venteRepository.getTotalVenduByDate(LocalDate.now()));
        model.addAttribute("stockRestant", productionService.getStockRestant());
        return "production/liste";
    }

    @GetMapping("/nouvelle")
    public String formulaireProduction(Model model) {
        model.addAttribute("mode", "create");
        if (!model.containsAttribute("production")) {
            model.addAttribute("production", new Production());
        }
        model.addAttribute("vaches", vachesEnLactation());
        return "production/ajoutProduction";
    }

    @PostMapping("/enregistrer")
    public String enregistrerProduction(@ModelAttribute("production") Production production,
            RedirectAttributes redirectAttributes) {
        try {
            productionService.saveProduction(production);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("production", production);
            return "redirect:/productions/nouvelle";
        }
        return "redirect:/productions";
    }

    @GetMapping("/{id}/edit")
    public String formulaireEdition(@PathVariable Long id, Model model) {
        model.addAttribute("mode", "edit");
        model.addAttribute("productionId", id);
        if (!model.containsAttribute("production")) {
            model.addAttribute("production", productionService.getById(id));
        }
        model.addAttribute("vaches", vachesEnLactation());
        return "production/ajoutProduction";
    }

    @PostMapping("/{id}/edit")
    public String modifierProduction(@PathVariable Long id, @ModelAttribute("production") Production production,
            RedirectAttributes redirectAttributes) {
        try {
            productionService.updateProduction(id, production);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("production", production);
            return "redirect:/productions/" + id + "/edit";
        }
        return "redirect:/productions";
    }

    @GetMapping("/{id}/delete")
    public String supprimerProduction(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productionService.delete(id);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/productions";
    }
}
