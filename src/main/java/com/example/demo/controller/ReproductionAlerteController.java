package com.example.demo.controller;

import com.example.demo.dto.AlerteReproductionDTO;
import com.example.demo.services.ReproductionAlerteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/reproduction")
public class ReproductionAlerteController {

    @Autowired
    private ReproductionAlerteService alerteService;

    @GetMapping("/alertes")
    public String afficherAlertes(Model model) {
        List<AlerteReproductionDTO> alertes = alerteService.getAlertesVelage();
        model.addAttribute("alertes", alertes);
        return "reproduction/reproduction_alertes";
    }
}