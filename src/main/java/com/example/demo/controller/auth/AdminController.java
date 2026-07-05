package com.example.demo.controller.auth;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.entity.production.Production;
import com.example.demo.entity.vente.Vente;
import com.example.demo.repository.alimentation.RationRepository;
import com.example.demo.repository.auth.UtilisateurRepository;
import com.example.demo.repository.cheptel.VacheRepository;
import com.example.demo.repository.production.ProductionRepository;
import com.example.demo.repository.vente.VenteRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final VacheRepository vacheRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final RationRepository rationRepository;
    private final ProductionRepository productionRepository;
    private final VenteRepository venteRepository;

    public AdminController(VacheRepository vacheRepository, UtilisateurRepository utilisateurRepository,
            RationRepository rationRepository, ProductionRepository productionRepository,
            VenteRepository venteRepository) {
        this.vacheRepository = vacheRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.rationRepository = rationRepository;
        this.productionRepository = productionRepository;
        this.venteRepository = venteRepository;
    }

    @GetMapping
    public String dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDe,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateA,
            Model model) {
        if (dateDe != null && dateA != null && dateA.isBefore(dateDe)) {
            LocalDate tmp = dateDe;
            dateDe = dateA;
            dateA = tmp;
        }

        long nbVaches = vacheRepository.count();
        long nbEleveurs = utilisateurRepository.count();
        long nbRations = rationRepository.count();
        BigDecimal quantiteLait = productionRepository.getTotalProductionBetweenDates(dateDe, dateA);
        BigDecimal totalVentes = venteRepository.getTotalRevenusBetweenDates(dateDe, dateA);

        model.addAttribute("nbVaches", nbVaches);
        model.addAttribute("nbEleveurs", nbEleveurs);
        model.addAttribute("nbRations", nbRations);
        model.addAttribute("quantiteLait", quantiteLait != null ? quantiteLait : BigDecimal.ZERO);
        model.addAttribute("totalVentes", totalVentes != null ? totalVentes : BigDecimal.ZERO);
        model.addAttribute("dateDe", dateDe);
        model.addAttribute("dateA", dateA);

        return "admin/dashboard";
    }

    @GetMapping("/statistiques")
    public String statistiques(Model model) {
        List<Map<String, Object>> prodStats = productionRepository.getProductionMensuelle();
        List<Map<String, Object>> venteStats = venteRepository.getRevenusMensuels();

        model.addAttribute("prodStats", prodStats);
        model.addAttribute("venteStats", venteStats);

        return "admin/statistiques";
    }

    @GetMapping("/rapports")
    public String rapports(Model model) {
        List<Production> dernieresProductions = productionRepository
                .findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "dateProduction"))).getContent();
        List<Vente> dernieresVentes = venteRepository.findAllByOrderByDateVenteDesc(PageRequest.of(0, 10)).getContent();

        model.addAttribute("productions", dernieresProductions);
        model.addAttribute("ventes", dernieresVentes);

        return "admin/rapports";
    }
}
