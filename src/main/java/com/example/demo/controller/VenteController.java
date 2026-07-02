package com.example.demo.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.Vente;
import com.example.demo.services.VenteService;



@Controller
public class VenteController {
private final VenteService venteService;

    public VenteController(VenteService venteService) {
        this.venteService = venteService;
    }

@GetMapping("/vente/nouveau")
public String nouveau(Model model) {
    model.addAttribute("vente", new Vente());
    return "vente/form";
}

@PostMapping("/vente")
public String save(@ModelAttribute Vente vente,
                   RedirectAttributes redirectAttributes) {

    try {

        venteService.effectuerVente(vente.getQuantiteLait(),vente.getPrixUnitaire(),vente.getDateVente());

        redirectAttributes.addFlashAttribute("success","Vente enregistrée.");

    } catch (RuntimeException e) {

        redirectAttributes.addFlashAttribute("error",e.getMessage());

        return "redirect:/vente/nouveau";
    }

    return "redirect:/vente/nouveau";
}

@GetMapping("/vente/liste")
public String listeVente(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="desc") String order, Model model){

    if(order.equals("asc")) {
        model.addAttribute("page", venteService.getOldestVentes(page, 10));
    } else {
        model.addAttribute("page", venteService.getMostRecentVentes(page , 10));
    }

    return "vente/liste";
}

}