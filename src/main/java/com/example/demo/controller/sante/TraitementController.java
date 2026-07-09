package com.example.demo.controller.sante;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dto.MedicamentFilleDTO;
import com.example.demo.dto.MedicamentOptionDTO;
import com.example.demo.dto.EvenementSanteFormDTO;
import com.example.demo.entity.sante.EvenementSante;
import com.example.demo.entity.sante.Medicament;
import com.example.demo.entity.sante.MedicamentFille;
import com.example.demo.services.sante.ExportService;
import com.example.demo.services.sante.ImportService;
import com.example.demo.services.sante.TraitementSanteService;

@Controller
@RequestMapping("/traitements")
public class TraitementController {

    private final TraitementSanteService traitementService;
    private final ExportService exportService;
    private final ImportService importService;

    public TraitementController(TraitementSanteService traitementService, ExportService exportService,
            ImportService importService) {
        this.traitementService = traitementService;
        this.exportService = exportService;
        this.importService = importService;
    }

    @GetMapping
    public String list(Model model) {
        traitementService.synchronizeVacheStatuses();
        model.addAttribute("evenements", traitementService.findAllEvenements());
        model.addAttribute("vachesTariees", traitementService.countVachesTariees());
        model.addAttribute("vaches", traitementService.findAllVaches());
        return "Traitement/liste";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        EvenementSante evenement = traitementService.findEvenementById(id);
        if (evenement == null) {
            return "redirect:/traitements";
        }
        model.addAttribute("evenement", evenement);
        return "Traitement/detailTraitement";
    }

    @GetMapping("/new")
    public String nouveau(Model model) {
        model.addAttribute("evenementForm", traitementService.createEmptyForm());
        addFormOptions(model);
        return "Traitement/ajoutTraitement";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        EvenementSanteFormDTO form = traitementService.findFormById(id);
        if (form == null) {
            return "redirect:/traitements";
        }
        model.addAttribute("evenementForm", form);
        addFormOptions(model);
        return "Traitement/ajoutTraitement";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("evenementForm") EvenementSanteFormDTO form, Model model) {
        try {
            traitementService.saveForm(form);
        } catch (IllegalArgumentException e) {
            model.addAttribute("erreur", e.getMessage());
            addFormOptions(model);
            return "Traitement/ajoutTraitement";
        }
        return "redirect:/traitements";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        traitementService.deleteEvenement(id);
        return "redirect:/traitements";
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportTraitements(@RequestParam(required = false) Long vacheId,
            @RequestParam(required = false) LocalDate datedebut,
            @RequestParam(required = false) LocalDate datefin) throws IOException {
        List<EvenementSante> evenements = traitementService.findAllEvenements().stream()
                .filter(e -> vacheId == null || (e.getVache() != null && vacheId.equals(e.getVache().getId())))
                .filter(e -> datedebut == null || (e.getDateEvenement() != null && !e.getDateEvenement().isBefore(datedebut)))
                .filter(e -> datefin == null || (e.getDateEvenement() != null && !e.getDateEvenement().isAfter(datefin)))
                .toList();

        ByteArrayInputStream in = exportService.exportTraitementsToExcel(evenements);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=traitements.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @PostMapping("/import")
    public String importTraitements(@RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    "Veuillez sélectionner un fichier Excel à envoyer.");
            return "redirect:/traitements";
        }

        try {
            importService.importTraitements(file);
            redirectAttributes.addFlashAttribute("success",
                    "L'importation des traitements a été effectuée avec succès !");
        } catch (IOException | RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'import : " + e.getMessage());
        }

        return "redirect:/traitements";
    }

    @ResponseBody
    @GetMapping("/api/medicaments")
    public List<MedicamentOptionDTO> medicaments(@RequestParam(required = false) Long maladieId) {
        List<Medicament> medicaments = (maladieId != null)
                ? traitementService.findMedicamentsByMaladie(maladieId)
                : traitementService.findAllMedicaments();
        return medicaments.stream()
                .map(medicament -> new MedicamentOptionDTO(medicament.getId(), medicament.getNom()))
                .toList();
    }

    @ResponseBody
    @GetMapping("/api/filles")
    public List<MedicamentFilleDTO> filles(@RequestParam Long medicamentId) {
        List<MedicamentFille> filles = traitementService.findFillesByMedicament(medicamentId);
        return filles.stream()
                .map(MedicamentFilleDTO::from)
                .toList();
    }

    private void addFormOptions(Model model) {
        model.addAttribute("vaches", traitementService.findAllVaches());
        model.addAttribute("maladies", traitementService.findAllMaladies());
        model.addAttribute("medicaments", traitementService.findAllMedicaments());
    }
}