package com.example.demo.controller;

import com.example.demo.entity.Production;
import com.example.demo.services.ProductionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/production")
public class ProductionController {

    private final ProductionService productionService;

    public ProductionController(ProductionService productionService) {
        this.productionService = productionService;
    }

    // Afficher la liste des productions
    @GetMapping
    public String listeProductions(Model model) {
        model.addAttribute("productions", productionService.getAllProductions());
        return "production/liste";
    }

    // Afficher le formulaire d'ajout
    @GetMapping("/nouvelle")
    public String formulaireProduction(Model model) {
        model.addAttribute("production", new Production());
        return "production/ajoutProduction";
    }

    // Traiter l'enregistrement du formulaire
    @PostMapping("/enregistrer")
    public String enregistrerProduction(@ModelAttribute("production") Production production) {
        productionService.saveProduction(production);
        return "redirect:/production";
    }
}