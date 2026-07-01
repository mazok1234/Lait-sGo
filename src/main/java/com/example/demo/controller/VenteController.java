package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.Vente;



@Controller
public class VenteController {


@GetMapping("/vente/nouveau")
public String nouveau(Model model) {
    model.addAttribute("vente", new Vente());
    return "vente/form";
}

@PostMapping("/vente/nouveau")
public String insertion(Model model) {
    model.addAttribute("vente", new Vente());
    return "vente/form";
}


}