package com.example.demo.cheptel.controllers;


import com.example.demo.cheptel.entities.Race;
import com.example.demo.cheptel.entities.StatutVache;
import com.example.demo.cheptel.entities.Vache;
import com.example.demo.cheptel.services.RaceService;
import com.example.demo.cheptel.services.StatutVacheService;
import com.example.demo.cheptel.services.VacheService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        model.addAttribute("page", "cheptel/dashboard");
        model.addAttribute("vaches", vacheService.search(null, org.springframework.data.domain.PageRequest.of(0, 10)).getContent());
        model.addAttribute("races", raceService.findAll());
        model.addAttribute("statuts", statutVacheService.findAll());
        model.addAttribute("dashboard", true);
        return "index";
    }

    @GetMapping("/cheptel/vaches")
    public String vaches(Model model) {
        model.addAttribute("page", "cheptel/vaches");
        model.addAttribute("vaches", vacheService.search(null, org.springframework.data.domain.PageRequest.of(0, 50)).getContent());
        model.addAttribute("races", raceService.findAll());
        model.addAttribute("statuts", statutVacheService.findAll());
        return "index";
    }

    @GetMapping("/cheptel/vaches/nouveau")
    public String createVache(Model model) {
        model.addAttribute("page", "cheptel/vache-form");
        model.addAttribute("mode", "create");
        model.addAttribute("vacheForm", new VacheForm());
        model.addAttribute("races", raceService.findAll());
        model.addAttribute("statuts", statutVacheService.findAll());
        model.addAttribute("vachesMeres", vacheService.search(null, org.springframework.data.domain.PageRequest.of(0, 50)).getContent());
        return "index";
    }

    @GetMapping("/cheptel/vaches/{id}/edit")
    public String editVache(@PathVariable Long id, Model model) {
        Vache v = vacheService.getById(id);
        model.addAttribute("page", "cheptel/vache-form");
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
        model.addAttribute("vachesMeres", vacheService.search(null, org.springframework.data.domain.PageRequest.of(0, 50)).getContent());
        return "index";
    }

    @PostMapping("/cheptel/vaches/nouveau")
    public String createVachePost(VacheForm form) {
        Vache v = new Vache();
        v.setNumeroBoucle(form.getNumeroBoucle());
        v.setDateNaissance(form.getDateNaissance());
        v.setPoidsKg(form.getPoidsKg());
        v.setScoreBcs(null);
        v.setScoreLocomotion(null);

        Race race = raceService.getById(form.getRaceId());
        v.setRace(race);

        StatutVache statut = statutVacheService.getById(form.getStatutId());
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
        model.addAttribute("page", "cheptel/vache-detail");
        model.addAttribute("vache", vacheService.getById(id));
        return "index";
    }

    @GetMapping("/cheptel/vaches/{id}/delete")
    public String deleteVache(@PathVariable Long id) {
        vacheService.delete(id);
        return "redirect:/cheptel/vaches";
    }

    @GetMapping("/cheptel/races")
    public String races(Model model) {
        model.addAttribute("page", "cheptel/races");
        model.addAttribute("races", raceService.findAll());
        return "index";
    }

    @PostMapping("/cheptel/races")
    public String addRace(@RequestParam String code, @RequestParam String libelle) {
        Race r = new Race();
        r.setCode(code);
        r.setLibelle(libelle);
        raceService.create(r);
        return "redirect:/cheptel/races";
    }

    @PostMapping("/cheptel/races/update/{id}")
    public String updateRace(@PathVariable Long id, @RequestParam String libelle) {
        Race r = new Race();
        // on ne touche pas le code ici, on le laisse depuis la DB via update() ? => donc on récupère existant.
        Race existing = raceService.getById(id);
        r.setCode(existing.getCode());
        r.setLibelle(libelle);
        raceService.update(id, r);
        return "redirect:/cheptel/races";
    }

    @GetMapping("/cheptel/races/delete/{id}")
    public String deleteRace(@PathVariable Long id) {
        raceService.delete(id);
        return "redirect:/cheptel/races";
    }

    @GetMapping("/cheptel/etats-sante")
    public String etats(Model model) {
        model.addAttribute("page", "cheptel/etats-sante");
        model.addAttribute("etats", statutVacheService.findAll());
        return "index";
    }

    @PostMapping("/cheptel/etats-sante")
    public String addEtatSante(@RequestParam String code, @RequestParam String libelle) {
        StatutVache e = new StatutVache();
        e.setCode(code);
        e.setLibelle(libelle);
        statutVacheService.create(e);
        return "redirect:/cheptel/etats-sante";
    }

    @PostMapping("/cheptel/etats-sante/update/{id}")
    public String updateEtatSante(@PathVariable Long id, @RequestParam String libelle) {
        StatutVache existing = statutVacheService.getById(id);
        StatutVache e = new StatutVache();
        e.setCode(existing.getCode());
        e.setLibelle(libelle);
        statutVacheService.update(id, e);
        return "redirect:/cheptel/etats-sante";
    }

    @GetMapping("/cheptel/etats-sante/delete/{id}")
    public String deleteEtatSante(@PathVariable Long id) {
        statutVacheService.delete(id);
        return "redirect:/cheptel/etats-sante";
    }

    // DTO simple pour binder un formulaire Thymeleaf
    public static class VacheForm {
        private String numeroBoucle;
        private Long raceId;
        private Long statutId;
        private java.time.LocalDate dateNaissance;
        private java.math.BigDecimal poidsKg;
        private Long mereId;

        public String getNumeroBoucle() {
            return numeroBoucle;
        }

        public void setNumeroBoucle(String numeroBoucle) {
            this.numeroBoucle = numeroBoucle;
        }

        public Long getRaceId() {
            return raceId;
        }

        public void setRaceId(Long raceId) {
            this.raceId = raceId;
        }

        public Long getStatutId() {
            return statutId;
        }

        public void setStatutId(Long statutId) {
            this.statutId = statutId;
        }

        public java.time.LocalDate getDateNaissance() {
            return dateNaissance;
        }

        public void setDateNaissance(java.time.LocalDate dateNaissance) {
            this.dateNaissance = dateNaissance;
        }

        public java.math.BigDecimal getPoidsKg() {
            return poidsKg;
        }

        public void setPoidsKg(java.math.BigDecimal poidsKg) {
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


