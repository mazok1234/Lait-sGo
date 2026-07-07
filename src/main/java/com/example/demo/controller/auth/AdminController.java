package com.example.demo.controller.auth;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
import com.example.demo.repository.alimentation.MouvementAlimentRepository;
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
    private final MouvementAlimentRepository mouvementAlimentRepository;
    private final ProductionRepository productionRepository;
    private final VenteRepository venteRepository;

    public AdminController(VacheRepository vacheRepository, UtilisateurRepository utilisateurRepository,
            RationRepository rationRepository, MouvementAlimentRepository mouvementAlimentRepository,
            ProductionRepository productionRepository,
            VenteRepository venteRepository) {
        this.vacheRepository = vacheRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.rationRepository = rationRepository;
        this.mouvementAlimentRepository = mouvementAlimentRepository;
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
        List<Map<String, Object>> depenseStats = mouvementAlimentRepository.getDepensesMensuelles();
        List<Map<String, Object>> rentabiliteStats = buildRentabiliteMensuelle(venteStats, depenseStats);

        BigDecimal totalRevenus = venteRepository.getTotalRevenus();
        BigDecimal totalDepenses = mouvementAlimentRepository.getTotalDepensesAlimentation();
        BigDecimal totalBenefice = nz(totalRevenus).subtract(nz(totalDepenses));
        BigDecimal margeBeneficePct = nz(totalRevenus).compareTo(BigDecimal.ZERO) > 0
                ? totalBenefice.multiply(new BigDecimal("100")).divide(totalRevenus, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        model.addAttribute("prodStats", prodStats);
        model.addAttribute("venteStats", venteStats);
        model.addAttribute("depenseStats", depenseStats);
        model.addAttribute("rentabiliteStats", rentabiliteStats);
        model.addAttribute("totalRevenus", nz(totalRevenus));
        model.addAttribute("totalDepenses", nz(totalDepenses));
        model.addAttribute("totalBenefice", totalBenefice);
        model.addAttribute("margeBeneficePct", margeBeneficePct);

        return "admin/statistiques";
    }

    @GetMapping("/rapports")
    public String rapports(Model model) {
        List<Production> dernieresProductions = productionRepository
                .findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "dateProduction"))).getContent();
        List<Vente> dernieresVentes = venteRepository.findAllByOrderByDateVenteDesc(PageRequest.of(0, 10)).getContent();
        BigDecimal totalRevenus = venteRepository.getTotalRevenus();
        BigDecimal totalDepenses = mouvementAlimentRepository.getTotalDepensesAlimentation();
        BigDecimal totalBenefice = nz(totalRevenus).subtract(nz(totalDepenses));

        List<Map<String, Object>> rentabiliteStats = buildRentabiliteMensuelle(
                venteRepository.getRevenusMensuels(),
                mouvementAlimentRepository.getDepensesMensuelles());

        model.addAttribute("productions", dernieresProductions);
        model.addAttribute("ventes", dernieresVentes);
        model.addAttribute("totalRevenus", nz(totalRevenus));
        model.addAttribute("totalDepenses", nz(totalDepenses));
        model.addAttribute("totalBenefice", totalBenefice);
        model.addAttribute("rentabiliteStats", rentabiliteStats);

        return "admin/rapports";
    }

    private BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private List<Map<String, Object>> buildRentabiliteMensuelle(List<Map<String, Object>> revenus,
                                                                List<Map<String, Object>> depenses) {
        Map<String, Map<String, Object>> merged = new LinkedHashMap<>();

        for (Map<String, Object> row : revenus) {
            Integer month = ((Number) row.get("month")).intValue();
            Integer year = ((Number) row.get("year")).intValue();
            BigDecimal total = toBigDecimal(row.get("total"));

            String key = year + "-" + month;
            Map<String, Object> item = merged.computeIfAbsent(key, k -> new LinkedHashMap<>());
            item.put("month", month);
            item.put("year", year);
            item.put("revenus", total);
            item.putIfAbsent("depenses", BigDecimal.ZERO);
        }

        for (Map<String, Object> row : depenses) {
            Integer month = ((Number) row.get("month")).intValue();
            Integer year = ((Number) row.get("year")).intValue();
            BigDecimal total = toBigDecimal(row.get("total"));

            String key = year + "-" + month;
            Map<String, Object> item = merged.computeIfAbsent(key, k -> new LinkedHashMap<>());
            item.put("month", month);
            item.put("year", year);
            item.put("depenses", total);
            item.putIfAbsent("revenus", BigDecimal.ZERO);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> item : merged.values()) {
            BigDecimal revenusVal = toBigDecimal(item.get("revenus"));
            BigDecimal depensesVal = toBigDecimal(item.get("depenses"));
            item.put("benefice", revenusVal.subtract(depensesVal));
            result.add(item);
        }
        return result;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        return new BigDecimal(String.valueOf(value));
    }
}
