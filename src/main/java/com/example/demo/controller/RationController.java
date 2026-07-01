package com.example.demo.controller;

import com.example.demo.entity.Ration;
import com.example.demo.entity.RationAliment;
import com.example.demo.repository.AlimentRepository;
import com.example.demo.services.RationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/rations")
public class RationController {

    private final RationService rationService;
    private final AlimentRepository alimentRepository;

    public RationController(RationService rationService,
                            AlimentRepository alimentRepository) {
        this.rationService = rationService;
        this.alimentRepository = alimentRepository;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Long rationId, Model model) {
        List<RationService.RationCardVm> rations = rationService.getRationCards();

        if (rations.isEmpty()) {
            return "redirect:/rations/new";
        }

        Long selectedRationId = rationId != null ? rationId : rations.get(0).id();
        Ration rationSelectionnee = rationService.findById(selectedRationId);

        if (rationSelectionnee == null) {
            selectedRationId = rations.get(0).id();
            rationSelectionnee = rationService.findById(selectedRationId);
        }

        model.addAttribute("rations", rations);
        model.addAttribute("rationSelectionnee", rationSelectionnee);
        model.addAttribute("rationAliments", rationService.findAlimentsByRationId(selectedRationId));
        model.addAttribute("nutrition", rationService.calculateNutrition(selectedRationId));
        return "rations/liste";
    }

    @GetMapping("/new")
    public String formAjoutRation(Model model) {
        model.addAttribute("ration", new Ration());
        model.addAttribute("stades", rationService.getStades());
        return "rations/ajout";
    }

    @PostMapping("/save")
    public String saveRation(@Valid @ModelAttribute("ration") Ration ration,
                             BindingResult result,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("stades", rationService.getStades());
            return "rations/ajout";
        }

        Ration saved = rationService.saveRation(ration);
        return "redirect:/rations?rationId=" + saved.getId();
    }

    @GetMapping("/aliments/new")
    public String formAjoutAliment(@RequestParam Long rationId, Model model) {
        Ration ration = rationService.findById(rationId);
        if (ration == null) {
            return "redirect:/rations";
        }

        RationAliment rationAliment = new RationAliment();
        rationAliment.setRation(ration);

        model.addAttribute("rationAliment", rationAliment);
        model.addAttribute("ration", ration);
        model.addAttribute("aliments", alimentRepository.findAll());
        return "rations/ajout-aliment";
    }

    @PostMapping("/aliments/save")
    public String saveAlimentRation(@Valid @ModelAttribute("rationAliment") RationAliment rationAliment,
                                    BindingResult result,
                                    Model model) {
        if (result.hasErrors()) {
            Long rationId = rationAliment.getRation() != null ? rationAliment.getRation().getId() : null;
            model.addAttribute("ration", rationId != null ? rationService.findById(rationId) : null);
            model.addAttribute("aliments", alimentRepository.findAll());
            return "rations/ajout-aliment";
        }

        rationService.saveRationAliment(rationAliment);
        return "redirect:/rations?rationId=" + rationAliment.getRation().getId();
    }

    @GetMapping("/aliments/delete/{id}")
    public String deleteAlimentRation(@PathVariable Long id, @RequestParam Long rationId) {
        rationService.deleteRationAliment(id);
        return "redirect:/rations?rationId=" + rationId;
    }
}
