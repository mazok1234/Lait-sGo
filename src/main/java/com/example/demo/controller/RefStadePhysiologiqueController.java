package com.example.demo.controller;

import com.example.demo.entity.RefStadePhysiologique;
import com.example.demo.services.RefStadePhysiologiqueService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/stades-physiologiques")
public class RefStadePhysiologiqueController {

    private final RefStadePhysiologiqueService stadeService;

    public RefStadePhysiologiqueController(RefStadePhysiologiqueService stadeService) {
        this.stadeService = stadeService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("stades", stadeService.findAll());
        return "stades_physiologiques/liste";
    }

    @GetMapping("/new")
    public String formAdd(Model model) {
        model.addAttribute("stade", new RefStadePhysiologique());
        return "stades_physiologiques/ajout";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("stade") RefStadePhysiologique stade,
                       BindingResult result) {
        if (stade.getJourMin() != null && stade.getJourMax() != null && stade.getJourMin() > stade.getJourMax()) {
            result.rejectValue("jourMax", "stade.jourMax.invalide", "Le jour max doit etre superieur ou egal au jour min");
        }

        if (result.hasErrors()) {
            return stade.getId() == null ? "stades_physiologiques/ajout" : "stades_physiologiques/modifier";
        }

        stadeService.save(stade);
        return "redirect:/stades-physiologiques";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("stade", stadeService.findById(id));
        return "stades_physiologiques/modifier";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        stadeService.deleteById(id);
        return "redirect:/stades-physiologiques";
    }
}
