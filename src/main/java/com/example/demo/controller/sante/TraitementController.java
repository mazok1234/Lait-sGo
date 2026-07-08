package com.example.demo.controller.sante;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.dto.EvenementSanteFormDTO;
import com.example.demo.entity.sante.EvenementSante;
import com.example.demo.services.sante.TraitementSanteService;

@Controller
@RequestMapping("/traitements")
public class TraitementController {

    private final TraitementSanteService traitementService;

    public TraitementController(TraitementSanteService traitementService) {
        this.traitementService = traitementService;
    }

    @GetMapping
    public String list(Model model) {
        traitementService.synchronizeVacheStatuses();
        model.addAttribute("evenements", traitementService.findAllEvenements());
        model.addAttribute("vachesTariees", traitementService.countVachesTariees());
        return "Traitement/liste";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        EvenementSante evenement = traitementService.findEvenementById(id);
        if (evenement == null) {
            return "redirect:/traitements";
        }
        model.addAttribute("evenement", evenement);
        return "Traitement/detailTraitement";
    }

    @GetMapping("/new")
    public String nouveau(Model model) {
        model.addAttribute("evenementForm", traitementService.createEmptyForm());
        addFormOptions(model);
        return "Traitement/ajoutTraitement";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        EvenementSanteFormDTO form = traitementService.findFormById(id);
        if (form == null) {
            return "redirect:/traitements";
        }
        model.addAttribute("evenementForm", form);
        addFormOptions(model);
        return "Traitement/ajoutTraitement";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("evenementForm") EvenementSanteFormDTO form, Model model) {
        try {
            traitementService.saveForm(form);
        } catch (IllegalArgumentException e) {
            model.addAttribute("erreur", e.getMessage());
            addFormOptions(model);
            return "Traitement/ajoutTraitement";
        }
        return "redirect:/traitements";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        traitementService.deleteEvenement(id);
        return "redirect:/traitements";
    }

    private void addFormOptions(Model model) {
        model.addAttribute("vaches", traitementService.findAllVaches());
        model.addAttribute("maladies", traitementService.findAllMaladies());
        model.addAttribute("medicaments", traitementService.findAllMedicaments());
    }
}