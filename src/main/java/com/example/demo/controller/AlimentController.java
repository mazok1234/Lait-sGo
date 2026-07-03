package com.example.demo.controller;

import com.example.demo.entity.Aliment;
import com.example.demo.repository.RefTypeAlimentRepository;
import com.example.demo.services.AlimentService;
import com.example.demo.services.MouvementAlimentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/aliments")
public class AlimentController {

    private final AlimentService alimentService;
    private final RefTypeAlimentRepository typeRepository;
    private final MouvementAlimentService mouvementService;

    public AlimentController(AlimentService alimentService,
                             RefTypeAlimentRepository typeRepository,
                             MouvementAlimentService mouvementService) {
        this.alimentService = alimentService;
        this.typeRepository = typeRepository;
        this.mouvementService = mouvementService;
    }

    // LISTE
    @GetMapping
    public String list(Model model) {
        List<Aliment> aliments = alimentService.findAll();
        // Calcule le stock actuel pour chaque aliment
        Map<Long, BigDecimal> stocks = new LinkedHashMap<>();
        long alerteCount = 0;
        BigDecimal coutTotalEstime = BigDecimal.ZERO;
        for (Aliment a : aliments) {
            BigDecimal stock = mouvementService.getStockActuel(a.getId());
            stocks.put(a.getId(), stock);
            if (a.getSeuilAlerteKg() != null && stock.compareTo(a.getSeuilAlerteKg()) < 0) {
                alerteCount++;
            }
            if (a.getPrixParKilo() != null) {
                coutTotalEstime = coutTotalEstime.add(a.getPrixParKilo().multiply(stock));
            }
        }
        model.addAttribute("aliments", aliments);
        model.addAttribute("stocks", stocks);
        model.addAttribute("alerteCount", alerteCount);
        model.addAttribute("coutTotalEstime", coutTotalEstime);
        return "aliments/liste";
    }

    // AJOUT
    @GetMapping("/new")
    public String formAdd(Model model) {
        model.addAttribute("aliment", new Aliment());
        model.addAttribute("types", typeRepository.findAll());
        return "aliments/ajout";
    }

    // SAVE
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("aliment") Aliment aliment,
                       org.springframework.validation.BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("types", typeRepository.findAll());
            return aliment.getId() == null ? "aliments/ajout" : "aliments/modifier";
        }
        alimentService.save(aliment);
        return "redirect:/aliments";
    }

    // DELETE
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        alimentService.deleteById(id);
        return "redirect:/aliments";
    }

    // EDIT
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("aliment", alimentService.findById(id));
        model.addAttribute("types", typeRepository.findAll());
        return "aliments/modifier";
    }
}