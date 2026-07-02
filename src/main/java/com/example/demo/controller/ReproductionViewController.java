package com.example.demo.controller;

import com.example.demo.service.ReproductionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class ReproductionViewController {

    private final ReproductionService reproductionService;

    public ReproductionViewController(ReproductionService reproductionService) {
        this.reproductionService = reproductionService;
    }

    @GetMapping("/reproduction/velage")
    public String showVelageForm(Model model) {
        List<Map<String, Object>> gestantes = reproductionService.getReproductionsGestantesPourVelage();

        model.addAttribute("reproductionsGestantes", gestantes);

        return "reproduction/form-velage";
    }

    @PostMapping("/reproduction/velage")
    public String enregistrerVelage(
            @RequestParam("reproductionId") Long reproductionId,
            @RequestParam("dateVelageReel")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateVelageReel,
            @RequestParam("sexeVeau") String sexeVeau,
            RedirectAttributes redirectAttributes) {

        reproductionService.enregistrerVelage(reproductionId, dateVelageReel, sexeVeau);
        redirectAttributes.addFlashAttribute("message", "Velage enregistre avec succes.");

        return "redirect:/reproduction/velage";
    }
}
