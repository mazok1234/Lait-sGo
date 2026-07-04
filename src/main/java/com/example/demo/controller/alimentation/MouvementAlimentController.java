package com.example.demo.controller.alimentation;

import com.example.demo.entity.alimentation.MouvementAliment;
import com.example.demo.repository.alimentation.AlimentRepository;
import com.example.demo.services.alimentation.MouvementAlimentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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

    @GetMapping
    public String list(Model model) {
        model.addAttribute("mouvements", mouvementService.findAll());
        return "mouvementsAlim/liste";
    }

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

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        mouvementService.deleteById(id);
        return "redirect:/mouvementsAlim";
    }
}
