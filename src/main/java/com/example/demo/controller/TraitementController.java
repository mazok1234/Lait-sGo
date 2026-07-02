package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.entity.TraitementSante;
import com.example.demo.services.TraitementSanteService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/traitements")
public class TraitementController {

	private final TraitementSanteService traitementService;

	public TraitementController(TraitementSanteService traitementService) {
		this.traitementService = traitementService;
	}

	@GetMapping
	public String list(Model model) {
		model.addAttribute("traitements", traitementService.findAll());
		return "Traitement/liste";
	}

	@GetMapping("/new")
	public String formNew(Model model) {
		model.addAttribute("traitement", traitementService.createEmptyForm());
		addFormOptions(model);
		return "Traitement/ajoutTraitement";
	}

	@PostMapping("/save")
	public String save(@Valid @ModelAttribute("traitement") TraitementSante traitement,
			BindingResult result,
			Model model) {
		if (result.hasErrors()) {
			addFormOptions(model);
			return "Traitement/ajoutTraitement";
		}

		try {
			traitementService.save(traitement);
		} catch (IllegalArgumentException e) {
			model.addAttribute("erreur", e.getMessage());
			addFormOptions(model);
			return "Traitement/ajoutTraitement";
		}

		return "redirect:/traitements";
	}

	@GetMapping("/edit/{id}")
	public String edit(@PathVariable Long id, Model model) {
		TraitementSante traitement = traitementService.findById(id);
		if (traitement == null) {
			return "redirect:/traitements";
		}

		model.addAttribute("traitement", traitement);
		addFormOptions(model);
		return "Traitement/ajoutTraitement";
	}

	@PostMapping("/delete/{id}")
	public String delete(@PathVariable Long id) {
		traitementService.deleteById(id);
		return "redirect:/traitements";
	}

	private void addFormOptions(Model model) {
		model.addAttribute("vaches", traitementService.findAllVaches());
		model.addAttribute("maladies", traitementService.findAllMaladies());
		model.addAttribute("medicaments", traitementService.findAllMedicaments());
	}
}