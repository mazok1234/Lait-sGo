package com.example.demo.controller;

import com.example.demo.entity.AffectationRationVache;
import com.example.demo.services.AffectationRationVacheService;
import com.example.demo.services.RationService;
import com.example.demo.repository.RationRepository;
import com.example.demo.repository.VacheRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/affectations-rations")
public class AffectationRationVacheController {

    private final AffectationRationVacheService affectationService;
    private final VacheRepository vacheRepository;
    private final RationRepository rationRepository;
    private final RationService rationService;

    public AffectationRationVacheController(AffectationRationVacheService affectationService,
                                            VacheRepository vacheRepository,
                                            RationRepository rationRepository,
                                            RationService rationService) {
        this.affectationService = affectationService;
        this.vacheRepository = vacheRepository;
        this.rationRepository = rationRepository;
        this.rationService = rationService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("affectations", affectationService.findAll());
        model.addAttribute("rationsActives", rationService.getRationsActivesVaches());
        return "affectations_rations/liste";
    }

    @GetMapping("/new")
    public String formAdd(Model model) {
        AffectationRationVache affectation = new AffectationRationVache();
        affectation.setActif(true);
        model.addAttribute("affectation", affectation);
        model.addAttribute("vaches", vacheRepository.findAll());
        model.addAttribute("rations", rationRepository.findAll());
        return "affectations_rations/ajout";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("affectation") AffectationRationVache affectation,
                       BindingResult result,
                       Model model) {
        if (affectation.getDateDebut() != null && affectation.getDateFin() != null
            && affectation.getDateFin().isBefore(affectation.getDateDebut())) {
            result.rejectValue("dateFin", "affectation.dateFin.invalide",
                    "La date fin doit etre superieure ou egale a la date debut");
        }

        if (result.hasErrors()) {
            model.addAttribute("vaches", vacheRepository.findAll());
            model.addAttribute("rations", rationRepository.findAll());
            return affectation.getId() == null ? "affectations_rations/ajout" : "affectations_rations/modifier";
        }

        affectationService.save(affectation);
        return "redirect:/affectations-rations";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        AffectationRationVache affectation = affectationService.findById(id);
        if (affectation == null) {
            return "redirect:/affectations-rations";
        }

        model.addAttribute("affectation", affectation);
        model.addAttribute("vaches", vacheRepository.findAll());
        model.addAttribute("rations", rationRepository.findAll());
        return "affectations_rations/modifier";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        affectationService.deleteById(id);
        return "redirect:/affectations-rations";
    }
}
