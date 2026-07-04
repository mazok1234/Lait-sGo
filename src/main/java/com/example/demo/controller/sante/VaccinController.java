package com.example.demo.controller.sante;

import com.example.demo.entity.sante.HistoriqueVaccin;
import com.example.demo.entity.sante.ProtocoleVaccin;
import com.example.demo.entity.cheptel.Vache;
import com.example.demo.repository.sante.HistoriqueVaccinRepository;
import com.example.demo.repository.sante.ProtocoleVaccinRepository;
import com.example.demo.repository.cheptel.VacheRepository;
import com.example.demo.services.sante.VaccinService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.io.ByteArrayInputStream;
import java.util.List;
import com.example.demo.services.sante.ExportService;
import java.io.IOException;
import com.example.demo.services.sante.ImportService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/vaccins")
public class VaccinController {
        private final HistoriqueVaccinRepository historiqueRepo;
        private final VacheRepository vacheRepo;
        private final ProtocoleVaccinRepository protocoleRepo;
        private final VaccinService vaccinService;
        private final ExportService exportService;
        private final ImportService importService;

        public VaccinController(
                HistoriqueVaccinRepository historiqueRepo,
                VacheRepository vacheRepo,
                ProtocoleVaccinRepository protocoleRepo,
                VaccinService vaccinService,
                ExportService exportService,
                ImportService importService) {
            this.historiqueRepo = historiqueRepo;
            this.vacheRepo = vacheRepo;
            this.protocoleRepo = protocoleRepo;
            this.vaccinService = vaccinService;
            this.exportService = exportService;
            this.importService = importService;
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

            model.addAttribute("vaches",
                    vacheRepo.findAll());

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
                model.addAttribute("status",vaccinService.statut(vaccinService.getVaccinsPrioritaires()));

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

        @GetMapping("/exportHistorique")
        public ResponseEntity<InputStreamResource> exportHistorique(@RequestParam(required = false) Long vacheId,
                                       @RequestParam(required = false) LocalDate datedebut,
                                       @RequestParam(required = false) LocalDate datefin) throws IOException {
            List<HistoriqueVaccin> historiques = historiqueRepo.findVaccins(vacheId, datedebut, datefin);

            ByteArrayInputStream in = exportService.exportToExcel(historiques, HistoriqueVaccin.class, "Vaccins");

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=vaccins.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));
        }

        @PostMapping("/importHistorique")
        public String importHistorique(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Veuillez sélectionner un fichier Excel à envoyer.");
                return "redirect:/vaccins";
            }

            try {
                importService.importVaccins(file);
                redirectAttributes.addFlashAttribute("success", "L'importation de l'historique a été effectuée avec succès !");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Erreur lors de l'import : " + e.getMessage());
            }

            return "redirect:/vaccins";
        }
}
