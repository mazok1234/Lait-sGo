package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Production;
import com.example.demo.repository.VacheRepository;
import com.example.demo.services.ProductionService;

@Controller
@RequestMapping("/productions")
public class ProductionController {

    private final ProductionService productionService;
    private final VacheRepository vacheRepository;

    public ProductionController(ProductionService productionService, VacheRepository vacheRepository) {
        this.productionService = productionService;
        this.vacheRepository = vacheRepository;
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
        model.addAttribute("vaches", vacheRepository.findAll());
        return "production/ajoutProduction";
    }

    // Traiter l'enregistrement du formulaire
    @PostMapping("/enregistrer")
    public String enregistrerProduction(@ModelAttribute("production") Production production) {
        productionService.saveProduction(production);
        return "redirect:/productions";
    }
}
