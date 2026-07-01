package com.example.demo.controller;

import com.example.demo.entity.HistoriqueVaccin;
import com.example.demo.entity.ProtocoleVaccin;
import com.example.demo.entity.Vache;
import com.example.demo.repository.HistoriqueVaccinRepository;
import com.example.demo.repository.ProtocoleVaccinRepository;
import com.example.demo.repository.VacheRepository;
import com.example.demo.services.VaccinService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/vaccins")
public class VaccinController {

    private final HistoriqueVaccinRepository historiqueRepo;
    private final VacheRepository vacheRepo;
    private final ProtocoleVaccinRepository protocoleRepo;
    private final VaccinService vaccinService;

    public VaccinController(
            HistoriqueVaccinRepository historiqueRepo,
            VacheRepository vacheRepo,
            ProtocoleVaccinRepository protocoleRepo,
            VaccinService vaccinService) {
        this.historiqueRepo = historiqueRepo;
        this.vacheRepo = vacheRepo;
        this.protocoleRepo = protocoleRepo;
        this.vaccinService = vaccinService;
    }

    @GetMapping("")
    public String index(Model model) {

        model.addAttribute("totalVaccins",
                historiqueRepo.count());

        model.addAttribute("prioritaires",
                vaccinService.getVaccinsPrioritaires());

        model.addAttribute("rappelsCount",
                vaccinService.getVaccinsPrioritaires().size());

        model.addAttribute("okCount",
                historiqueRepo.count() -
                        vaccinService.getVaccinsPrioritaires().size());

        return "vaccins/liste";
    }

    @GetMapping("/historique/form")
    public String form(Model model) {

        model.addAttribute("vaches", vacheRepo.findAll());
        model.addAttribute("protocoles", protocoleRepo.findAll());

        return "vaccins/form";
    }


    @PostMapping("/historique/save")
    public String saveHistorique(
            @RequestParam Long vacheId,
            @RequestParam Integer protocoleId,
            @RequestParam String dateVaccination,
            @RequestParam String typeInjection
    ) {

        Vache vache = vacheRepo.findById(vacheId)
                .orElseThrow(() -> new RuntimeException("Vache introuvable"));

        ProtocoleVaccin protocole = protocoleRepo.findById(protocoleId)
                .orElseThrow(() -> new RuntimeException("Protocole introuvable"));

        HistoriqueVaccin h = new HistoriqueVaccin();
        h.setVache(vache);
        h.setProtocoleVaccin(protocole);
        h.setDateVaccination(LocalDate.parse(dateVaccination));
        h.setTypeInjection(typeInjection);

        historiqueRepo.save(h);

        return "redirect:/vaccins/historique/form";
    }


    @GetMapping("/rappels")
    public String rappels(Model model) {

        model.addAttribute("rappels",
                vaccinService.getVaccinsPrioritaires());

        return "vaccins/rappels";
    }


    @GetMapping("/vache/{id}")
    public String historiqueParVache(@PathVariable Long id, Model model) {

        Vache vache = vacheRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Vache introuvable"));

        model.addAttribute("vache", vache);
        model.addAttribute("historiques",
                historiqueRepo.findByVache(vache));

        return "vaccins/historique-vache";
    }

    @GetMapping("/stats")
    public String stats(Model model) {

        model.addAttribute("stats",vaccinService.getStatistiquesVaccins());

        return "vaccins/stats";
    }
}