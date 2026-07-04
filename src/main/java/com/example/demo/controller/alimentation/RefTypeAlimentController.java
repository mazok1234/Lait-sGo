package com.example.demo.controller.alimentation;

import com.example.demo.entity.alimentation.RefTypeAliment;
import com.example.demo.services.alimentation.RefTypeAlimentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@Controller
@RequestMapping("/types-aliments")
public class RefTypeAlimentController {
    private final RefTypeAlimentService typeService;

    public RefTypeAlimentController(RefTypeAlimentService typeService) {
        this.typeService = typeService;
    }

    @GetMapping
    public String list(Model model) {
        List<RefTypeAliment> types = typeService.findAll();
        model.addAttribute("types", types);
        return "types_aliments/liste";
    }

    @GetMapping("/new")
    public String formAdd(Model model) {
        model.addAttribute("typeAliment", new RefTypeAliment());
        return "types_aliments/ajout";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("typeAliment") RefTypeAliment typeAliment, org.springframework.validation.BindingResult result) {
        if (result.hasErrors()) {
            return typeAliment.getId() == null ? "types_aliments/ajout" : "types_aliments/modifier";
        }
        typeService.save(typeAliment);
        return "redirect:/types-aliments";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("typeAliment", typeService.findById(id));
        return "types_aliments/modifier";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        typeService.deleteById(id);
        return "redirect:/types-aliments";
    }
}
