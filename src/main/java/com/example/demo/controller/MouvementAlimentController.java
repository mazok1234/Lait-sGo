package com.example.demo.controller;

import com.example.demo.entity.Aliment;
import com.example.demo.entity.MouvementAliment;
import com.example.demo.repository.AlimentRepository;
import com.example.demo.services.MouvementAlimentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/mouvementsAlim")
public class MouvementAlimentController {

    private final MouvementAlimentService mouvementService;
    private final AlimentRepository alimentRepo;

    public MouvementAlimentController(MouvementAlimentService mouvementService,
                                      AlimentRepository alimentRepo) {
        this.mouvementService = mouvementService;
        this.alimentRepo = alimentRepo;
    }

    // LISTE de tous les mouvements
    @GetMapping
    public String list(Model model) {
        model.addAttribute("mouvements", mouvementService.findAll());
        return "mouvementsAlim/liste";
    }

    // FORM : depuis /aliments -> +Mvt passe l'alimentId en paramètre
    @GetMapping("/new")
    public String form(@RequestParam(required = false) Long alimentId, Model model) {
        MouvementAliment mouvement = new MouvementAliment();
        mouvement.setDateMouvement(LocalDate.now());

        if (alimentId != null) {
            alimentRepo.findById(alimentId).ifPresent(mouvement::setAliment);
        }

        model.addAttribute("mouvement", mouvement);
        model.addAttribute("aliments", alimentRepo.findAll());
        model.addAttribute("alimentIdPrefill", alimentId);
        return "mouvementsAlim/form";
    }

    // SAVE
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("mouvement") MouvementAliment mouvement,
                       BindingResult result, Model model) {
        String erreurDateSortie = mouvementService.getErreurDateSortie(mouvement);
        if (erreurDateSortie != null) {
            result.rejectValue("dateMouvement", "mouvement.date.invalide", erreurDateSortie);
        }

        if (result.hasErrors()) {
            model.addAttribute("aliments", alimentRepo.findAll());
            return "mouvementsAlim/form";
        }
        mouvementService.save(mouvement);
        return "redirect:/mouvementsAlim";
    }

    // EDIT
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        MouvementAliment mouvement = mouvementService.findById(id);
        if (mouvement == null) {
            return "redirect:/mouvementsAlim";
        }

        model.addAttribute("mouvement", mouvement);
        model.addAttribute("aliments", alimentRepo.findAll());
        return "mouvementsAlim/form";
    }

    // DELETE
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        mouvementService.deleteById(id);
        return "redirect:/mouvementsAlim";
    }
}