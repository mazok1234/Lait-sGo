package com.example.demo.controller;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.RefRace;
import com.example.demo.entity.RefStatutVache;
import com.example.demo.entity.Vache;
import com.example.demo.services.RaceService;
import com.example.demo.services.StatutVacheService;
import com.example.demo.services.VacheService;

@Controller
public class CheptelPageController {

    private final VacheService vacheService;
    private final RaceService raceService;
    private final StatutVacheService statutVacheService;

    public CheptelPageController(VacheService vacheService, RaceService raceService, StatutVacheService statutVacheService) {
        this.vacheService = vacheService;
        this.raceService = raceService;
        this.statutVacheService = statutVacheService;
    }

    @GetMapping("/cheptel")
    public String dashboard(Model model) {
        model.addAttribute("vaches", vacheService.search(null, PageRequest.of(0, 10)).getContent());
        model.addAttribute("races", raceService.findAll());
        model.addAttribute("statuts", statutVacheService.findAll());
        return "cheptel/dashboard";
    }

    @GetMapping("/cheptel/vaches")
    public String vaches(Model model) {
        model.addAttribute("vaches", vacheService.search(null, PageRequest.of(0, 50)).getContent());
        model.addAttribute("races", raceService.findAll());
        model.addAttribute("statuts", statutVacheService.findAll());
        return "cheptel/vaches";
    }

    @GetMapping("/cheptel/vaches/nouveau")
    public String createVache(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("vacheForm", new VacheForm());
        model.addAttribute("races", raceService.findAll());
        model.addAttribute("statuts", statutVacheService.findAll());
        model.addAttribute("vachesMeres", vacheService.search(null, PageRequest.of(0, 50)).getContent());
        return "cheptel/vache-form";
    }

    @GetMapping("/cheptel/vaches/{id}/edit")
    public String editVache(@PathVariable Long id, Model model) {
        Vache v = vacheService.getById(id);
        model.addAttribute("mode", "edit");

        VacheForm form = new VacheForm();
        form.setNumeroBoucle(v.getNumeroBoucle());
        form.setRaceId(v.getRace().getId());
        form.setStatutId(v.getStatut().getId());
        form.setDateNaissance(v.getDateNaissance());
        form.setPoidsKg(v.getPoidsKg());
        form.setMereId(v.getMere() != null ? v.getMere().getId() : null);

        model.addAttribute("vacheForm", form);
        model.addAttribute("races", raceService.findAll());
        model.addAttribute("statuts", statutVacheService.findAll());
        model.addAttribute("vachesMeres", vacheService.search(null, PageRequest.of(0, 50)).getContent());
        return "cheptel/vache-form";
    }

    @PostMapping("/cheptel/vaches/nouveau")
    public String createVachePost(VacheForm form) {
        Vache v = new Vache();
        v.setNumeroBoucle(form.getNumeroBoucle());
        v.setDateNaissance(form.getDateNaissance());
        v.setPoidsKg(form.getPoidsKg());
        v.setScoreBcs(null);
        v.setScoreLocomotion(null);

        RefRace race = raceService.getById(form.getRaceId());
        v.setRace(race);

        RefStatutVache statut = statutVacheService.getById(form.getStatutId());
        v.setStatut(statut);

        if (form.getMereId() != null) {
            v.setMere(vacheService.getById(form.getMereId()));
        }

        vacheService.create(v);
        return "redirect:/cheptel/vaches";
    }

    @PostMapping("/cheptel/vaches/{id}/edit")
    public String updateVache(@PathVariable Long id, VacheForm form) {
        Vache existing = vacheService.getById(id);
        existing.setNumeroBoucle(form.getNumeroBoucle());
        existing.setDateNaissance(form.getDateNaissance());
        existing.setPoidsKg(form.getPoidsKg());

        existing.setRace(raceService.getById(form.getRaceId()));
        existing.setStatut(statutVacheService.getById(form.getStatutId()));
        existing.setMere(form.getMereId() != null ? vacheService.getById(form.getMereId()) : null);

        vacheService.update(id, existing);
        return "redirect:/cheptel/vaches";
    }

    @GetMapping("/cheptel/vaches/{id}")
    public String vacheDetail(@PathVariable Long id, Model model) {
        model.addAttribute("vache", vacheService.getById(id));
        return "cheptel/vache-detail";
    }

    @GetMapping("/cheptel/vaches/{id}/delete")
    public String deleteVache(@PathVariable Long id) {
        vacheService.delete(id);
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

    @GetMapping("/cheptel/etats-sante")
    public String etats(Model model) {
        model.addAttribute("etats", statutVacheService.findAll());
        return "cheptel/etats-sante";
    }

    @PostMapping("/cheptel/etats-sante")
    public String addEtatSante(@RequestParam String code, @RequestParam String libelle) {
        RefStatutVache e = new RefStatutVache();
        e.setCode(code);
        e.setLibelle(libelle);
        statutVacheService.create(e);
        return "redirect:/cheptel/etats-sante";
    }

    @PostMapping("/cheptel/etats-sante/update/{id}")
    public String updateEtatSante(@PathVariable Integer id, @RequestParam String libelle) {
        RefStatutVache existing = statutVacheService.getById(id);
        RefStatutVache e = new RefStatutVache();
        e.setCode(existing.getCode());
        e.setLibelle(libelle);
        statutVacheService.update(id, e);
        return "redirect:/cheptel/etats-sante";
    }

    @GetMapping("/cheptel/etats-sante/delete/{id}")
    public String deleteEtatSante(@PathVariable Integer id) {
        statutVacheService.delete(id);
        return "redirect:/cheptel/etats-sante";
    }

    // DTO simple pour binder un formulaire Thymeleaf
    public static class VacheForm {
        private String numeroBoucle;
        private Integer raceId;
        private Integer statutId;
        private LocalDate dateNaissance;
        private BigDecimal poidsKg;
        private Long mereId;

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

        public Integer getStatutId() {
            return statutId;
        }

        public void setStatutId(Integer statutId) {
            this.statutId = statutId;
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
    }
}