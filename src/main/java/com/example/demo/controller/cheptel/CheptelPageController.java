package com.example.demo.controller.cheptel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.cheptel.RefRace;
import com.example.demo.entity.cheptel.Vache;
import com.example.demo.services.cheptel.RaceService;
import com.example.demo.services.reproduction.ReproductionService;
import com.example.demo.services.cheptel.StatutLactationVacheService;
import com.example.demo.services.cheptel.StatutReproService;
import com.example.demo.services.cheptel.StatutSanteService;
import com.example.demo.services.cheptel.StatutVieService;
import com.example.demo.services.cheptel.VacheService;

@Controller
public class CheptelPageController {
    private final VacheService vacheService;
    private final RaceService raceService;
    private final StatutVieService statutVieService;
    private final StatutReproService statutReproService;
    private final StatutLactationVacheService statutLactationService;
    private final StatutSanteService statutSanteService;
    private final ReproductionService reproductionService;

    public CheptelPageController(VacheService vacheService, RaceService raceService,
            StatutVieService statutVieService, StatutReproService statutReproService,
            StatutLactationVacheService statutLactationService, StatutSanteService statutSanteService,
            ReproductionService reproductionService) {
        this.vacheService = vacheService;
        this.raceService = raceService;
        this.statutVieService = statutVieService;
        this.statutReproService = statutReproService;
        this.statutLactationService = statutLactationService;
        this.statutSanteService = statutSanteService;
        this.reproductionService = reproductionService;
    }

    @GetMapping("/cheptel")
    public String dashboard(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer raceId,
            @RequestParam(required = false) Integer vieId,
            @RequestParam(required = false) Integer reproId,
            @RequestParam(required = false) Integer lactationId,
            @RequestParam(required = false) Integer santeId,
            Model model) {
        var vaches = vacheService.search(buildVacheSpec(q, raceId, vieId, reproId, lactationId, santeId), PageRequest.of(0, 10)).getContent();
        vacheService.declencherAlertesBcs(vaches);

        model.addAttribute("vaches", enrichirAvecStatuts(vaches));
        model.addAttribute("races", raceService.findAll());
        model.addAttribute("statutsVie", statutVieService.findAll());
        model.addAttribute("statutsRepro", statutReproService.findAll());
        model.addAttribute("statutsLactation", statutLactationService.findAll());
        model.addAttribute("statutsSante", statutSanteService.findAll());

        model.addAttribute("totalVaches", vacheService.count());
        model.addAttribute("enLactationCount", statutLactationService.findVacheIdsByStatut(
                statutLactationService.getByLibelle("En_lactation").getId()).size());
        model.addAttribute("reformeeCount", statutVieService.findVacheIdsByStatut(
                statutVieService.getByLibelle("Reformee").getId()).size());
        model.addAttribute("avecMereCount", vacheService.countAvecMere());
        model.addAttribute("surveillanceCount", vacheService.countEnSurveillance());
        return "cheptel/dashboard";
    }

    private Specification<Vache> buildVacheSpec(String q, Integer raceId, Integer vieId, Integer reproId,
            Integer lactationId, Integer santeId) {
        Specification<Vache> spec = (root, query, cb) -> cb.conjunction();
        if (raceId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("race").get("id"), raceId));
        }
        if (q != null && !q.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("numeroBoucle")), "%" + q.toLowerCase() + "%"));
        }
        if (vieId != null) {
            spec = spec.and(idInSpec(statutVieService.findVacheIdsByStatut(vieId)));
        }
        if (reproId != null) {
            spec = spec.and(idInSpec(statutReproService.findVacheIdsByStatut(reproId)));
        }
        if (lactationId != null) {
            spec = spec.and(idInSpec(statutLactationService.findVacheIdsByStatut(lactationId)));
        }
        if (santeId != null) {
            spec = spec.and(idInSpec(statutSanteService.findVacheIdsByStatut(santeId)));
        }
        return spec;
    }

    private Specification<Vache> idInSpec(List<Long> ids) {
        List<Long> safe = ids.isEmpty() ? List.of(-1L) : ids;
        return (root, query, cb) -> root.get("id").in(safe);
    }

    private List<VacheAvecStatuts> enrichirAvecStatuts(List<Vache> vaches) {
        List<VacheAvecStatuts> result = new ArrayList<>();
        for (Vache v : vaches) {
            result.add(new VacheAvecStatuts(
                    v,
                    statutVieService.getStatutActuel(v.getId()).map(s -> s.getLibelle()).orElse("-"),
                    statutReproService.getStatutActuel(v.getId()).map(s -> s.getLibelle()).orElse("-"),
                    statutLactationService.getStatutActuel(v.getId()).map(s -> s.getLibelle()).orElse("-"),
                    statutSanteService.getStatutActuel(v.getId()).map(s -> s.getLibelle()).orElse("-")));
        }
        return result;
    }

    @GetMapping("/cheptel/vaches")
    public String vaches(Model model) {
        var vaches = vacheService.search(null, PageRequest.of(0, 50)).getContent();
        vacheService.declencherAlertesBcs(vaches);

        model.addAttribute("vaches", enrichirAvecStatuts(vaches));
        model.addAttribute("races", raceService.findAll());
        model.addAttribute("statutsVie", statutVieService.findAll());
        model.addAttribute("statutsRepro", statutReproService.findAll());
        model.addAttribute("statutsLactation", statutLactationService.findAll());
        model.addAttribute("statutsSante", statutSanteService.findAll());
        return "cheptel/vaches";
    }

    @GetMapping("/cheptel/vaches/nouveau")
    public String createVache(@RequestParam(required = false) Long reproductionId, Model model) {
        model.addAttribute("mode", "create");
        if (!model.containsAttribute("vacheForm")) {
            VacheForm form = new VacheForm();
            if (reproductionId != null) {
                Vache mere = reproductionService.getVacheDeReproduction(reproductionId);
                form.setRaceId(mere.getRace().getId());
                form.setMereId(mere.getId());
                form.setSexe("M");
                form.setVieId(statutVieService.getByLibelle("Veau").getId());
                form.setReproId(statutReproService.getByLibelle("Vide").getId());
                form.setLactationId(statutLactationService.getByLibelle("Tarie").getId());
                form.setSanteId(statutSanteService.getByLibelle("Saine").getId());
            }
            model.addAttribute("vacheForm", form);
        }
        model.addAttribute("reproductionId", reproductionId);
        model.addAttribute("races", raceService.findAll());
        model.addAttribute("statutsVie", statutVieService.findAll());
        model.addAttribute("statutsRepro", statutReproService.findAll());
        model.addAttribute("statutsLactation", statutLactationService.findAll());
        model.addAttribute("statutsSante", statutSanteService.findAll());
        model.addAttribute("vachesMeres", vacheService.search(null, PageRequest.of(0, 50)).getContent());
        return "cheptel/vache-form";
    }

    @GetMapping("/cheptel/vaches/{id}/edit")
    public String editVache(@PathVariable Long id, Model model) {
        Vache v = vacheService.getById(id);
        model.addAttribute("mode", "edit");

        if (!model.containsAttribute("vacheForm")) {
            VacheForm form = new VacheForm();
            form.setNumeroBoucle(v.getNumeroBoucle());
            form.setRaceId(v.getRace().getId());
            form.setDateNaissance(v.getDateNaissance());
            form.setPoidsKg(v.getPoidsKg());
            form.setMereId(v.getMere() != null ? v.getMere().getId() : null);
            form.setScoreBcs(v.getScoreBcs());
            form.setScoreLocomotion(v.getScoreLocomotion());
            statutVieService.getStatutActuel(id).ifPresent(s -> form.setVieId(s.getId()));
            statutReproService.getStatutActuel(id).ifPresent(s -> form.setReproId(s.getId()));
            statutLactationService.getStatutActuel(id).ifPresent(s -> form.setLactationId(s.getId()));
            statutSanteService.getStatutActuel(id).ifPresent(s -> form.setSanteId(s.getId()));
            model.addAttribute("vacheForm", form);
        }
        model.addAttribute("vacheId", id);
        model.addAttribute("races", raceService.findAll());
        model.addAttribute("statutsVie", statutVieService.findAll());
        model.addAttribute("statutsRepro", statutReproService.findAll());
        model.addAttribute("statutsLactation", statutLactationService.findAll());
        model.addAttribute("statutsSante", statutSanteService.findAll());
        model.addAttribute("vachesMeres", vacheService.search(null, PageRequest.of(0, 50)).getContent());
        return "cheptel/vache-form";
    }

    @PostMapping("/cheptel/vaches/nouveau")
    public String createVachePost(VacheForm form, @RequestParam(required = false) Long reproductionId,
            RedirectAttributes redirectAttributes) {
        Vache v = new Vache();
        v.setNumeroBoucle(form.getNumeroBoucle());
        v.setDateNaissance(form.getDateNaissance());
        v.setPoidsKg(form.getPoidsKg());
        v.setScoreBcs(form.getScoreBcs());
        v.setScoreLocomotion(form.getScoreLocomotion());

        RefRace race = raceService.getById(form.getRaceId());
        v.setRace(race);

        if (form.getMereId() != null) {
            v.setMere(vacheService.getById(form.getMereId()));
        }

        try {
            vacheService.create(v);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("vacheForm", form);
            String suffix = reproductionId != null ? "?reproductionId=" + reproductionId : "";
            return "redirect:/cheptel/vaches/nouveau" + suffix;
        }

        Integer vieId = form.getVieId();
        if (form.getSexe() != null && !form.getSexe().isBlank()) {
            vieId = statutVieService.getByLibelle("M".equals(form.getSexe()) ? "Veau" : "Genisse").getId();
        }

        LocalDate today = LocalDate.now();
        if (vieId != null) {
            statutVieService.ouvrirInitial(v, vieId, today);
        }
        if (form.getReproId() != null) {
            statutReproService.ouvrirInitial(v, form.getReproId(), today);
        }
        if (form.getLactationId() != null) {
            statutLactationService.ouvrirInitial(v, form.getLactationId(), today);
        }
        if (form.getSanteId() != null) {
            statutSanteService.ouvrirInitial(v, form.getSanteId(), today);
        }

        if (reproductionId != null) {
            String sexeVeau = "F".equals(form.getSexe()) ? "F" : "M";
            reproductionService.confirmerVelage(reproductionId, form.getDateNaissance(), sexeVeau);
        }

        return "redirect:/cheptel/vaches";
    }

    @PostMapping("/cheptel/vaches/{id}/edit")
    public String updateVache(@PathVariable Long id, VacheForm form, RedirectAttributes redirectAttributes) {
        Vache existing = vacheService.getById(id);
        existing.setNumeroBoucle(form.getNumeroBoucle());
        existing.setDateNaissance(form.getDateNaissance());
        existing.setPoidsKg(form.getPoidsKg());
        existing.setScoreBcs(form.getScoreBcs());
        existing.setScoreLocomotion(form.getScoreLocomotion());

        existing.setRace(raceService.getById(form.getRaceId()));
        existing.setMere(form.getMereId() != null ? vacheService.getById(form.getMereId()) : null);

        try {
            vacheService.update(id, existing);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("vacheForm", form);
            return "redirect:/cheptel/vaches/" + id + "/edit";
        }

        LocalDate today = LocalDate.now();
        if (form.getVieId() != null) {
            statutVieService.changerStatut(existing, form.getVieId(), today);
        }
        if (form.getReproId() != null) {
            statutReproService.changerStatut(existing, form.getReproId(), today);
        }
        if (form.getLactationId() != null) {
            statutLactationService.changerStatut(existing, form.getLactationId(), today);
        }
        if (form.getSanteId() != null) {
            statutSanteService.changerStatut(existing, form.getSanteId(), today);
        }

        return "redirect:/cheptel/vaches";
    }

    @GetMapping("/cheptel/vaches/{id}")
    public String vacheDetail(@PathVariable Long id, Model model) {
        Vache vache = vacheService.getById(id);
        model.addAttribute("vache", vache);
        model.addAttribute("statutVie", statutVieService.getStatutActuel(id).map(s -> s.getLibelle()).orElse("-"));
        model.addAttribute("statutRepro", statutReproService.getStatutActuel(id).map(s -> s.getLibelle()).orElse("-"));
        model.addAttribute("statutLactation", statutLactationService.getStatutActuel(id).map(s -> s.getLibelle()).orElse("-"));
        model.addAttribute("statutSante", statutSanteService.getStatutActuel(id).map(s -> s.getLibelle()).orElse("-"));
        return "cheptel/vache-detail";
    }

    @GetMapping("/cheptel/vaches/{id}/delete")
    public String deleteVache(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            vacheService.delete(id);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cheptel/vaches";
    }

    @GetMapping("/cheptel/races")
    public String races(Model model) {
        model.addAttribute("races", raceService.findAll());
        return "cheptel/races";
    }

    @PostMapping("/cheptel/races")
    public String addRace(@RequestParam String code, @RequestParam String libelle) {
        RefRace r = new RefRace();
        r.setCode(code);
        r.setLibelle(libelle);
        raceService.create(r);
        return "redirect:/cheptel/races";
    }

    @PostMapping("/cheptel/races/update/{id}")
    public String updateRace(@PathVariable Integer id, @RequestParam String libelle) {
        RefRace existing = raceService.getById(id);
        RefRace r = new RefRace();
        r.setCode(existing.getCode());
        r.setLibelle(libelle);
        raceService.update(id, r);
        return "redirect:/cheptel/races";
    }

    @GetMapping("/cheptel/races/delete/{id}")
    public String deleteRace(@PathVariable Integer id) {
        raceService.delete(id);
        return "redirect:/cheptel/races";
    }

    public static class VacheAvecStatuts {
        private final Vache vache;
        private final String vie;
        private final String repro;
        private final String lactation;
        private final String sante;

        public VacheAvecStatuts(Vache vache, String vie, String repro, String lactation, String sante) {
            this.vache = vache;
            this.vie = vie;
            this.repro = repro;
            this.lactation = lactation;
            this.sante = sante;
        }

        public Vache getVache() {
            return vache;
        }

        public String getVie() {
            return vie;
        }

        public String getRepro() {
            return repro;
        }

        public String getLactation() {
            return lactation;
        }

        public String getSante() {
            return sante;
        }
    }

    public static class VacheForm {
        private String numeroBoucle;
        private Integer raceId;
        private LocalDate dateNaissance;
        private BigDecimal poidsKg;
        private Long mereId;
        private BigDecimal scoreBcs;
        private Short scoreLocomotion;
        private Integer vieId;
        private Integer reproId;
        private Integer lactationId;
        private Integer santeId;
        private String sexe;

        public String getNumeroBoucle() {
            return numeroBoucle;
        }

        public void setNumeroBoucle(String numeroBoucle) {
            this.numeroBoucle = numeroBoucle;
        }

        public Integer getRaceId() {
            return raceId;
        }

        public void setRaceId(Integer raceId) {
            this.raceId = raceId;
        }

        public LocalDate getDateNaissance() {
            return dateNaissance;
        }

        public void setDateNaissance(LocalDate dateNaissance) {
            this.dateNaissance = dateNaissance;
        }

        public BigDecimal getPoidsKg() {
            return poidsKg;
        }

        public void setPoidsKg(BigDecimal poidsKg) {
            this.poidsKg = poidsKg;
        }

        public Long getMereId() {
            return mereId;
        }

        public void setMereId(Long mereId) {
            this.mereId = mereId;
        }

        public BigDecimal getScoreBcs() {
            return scoreBcs;
        }

        public void setScoreBcs(BigDecimal scoreBcs) {
            this.scoreBcs = scoreBcs;
        }

        public Short getScoreLocomotion() {
            return scoreLocomotion;
        }

        public void setScoreLocomotion(Short scoreLocomotion) {
            this.scoreLocomotion = scoreLocomotion;
        }

        public Integer getVieId() {
            return vieId;
        }

        public void setVieId(Integer vieId) {
            this.vieId = vieId;
        }

        public Integer getReproId() {
            return reproId;
        }

        public void setReproId(Integer reproId) {
            this.reproId = reproId;
        }

        public Integer getLactationId() {
            return lactationId;
        }

        public void setLactationId(Integer lactationId) {
            this.lactationId = lactationId;
        }

        public Integer getSanteId() {
            return santeId;
        }

        public void setSanteId(Integer santeId) {
            this.santeId = santeId;
        }

        public String getSexe() {
            return sexe;
        }

        public void setSexe(String sexe) {
            this.sexe = sexe;
        }
    }
}
