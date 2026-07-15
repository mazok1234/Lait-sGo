package com.example.demo.controller.vente;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.vente.Vente;
import com.example.demo.services.vente.VentePdfService;
import com.example.demo.services.vente.VenteService;

@Controller
public class VenteController {
private final VenteService venteService;
private final VentePdfService ventePdfService;

    public VenteController(VenteService venteService, VentePdfService ventePdfService) {
        this.venteService = venteService;
        this.ventePdfService = ventePdfService;
    }

@GetMapping("/vente/nouveau")
public String nouveau(Model model) {
    model.addAttribute("vente", new Vente());
    model.addAttribute("produits", venteService.getProduits());
    return "vente/form";
}

@PostMapping("/vente")
public String save(@ModelAttribute Vente vente,
                   RedirectAttributes redirectAttributes) {
    try {
        Vente savedVente = venteService.effectuerVente(vente.getQuantite(), vente.getPrixUnitaire(), vente.getDateVente(), vente.getProduit());

        redirectAttributes.addFlashAttribute("success","Vente enregistrée.");
        redirectAttributes.addFlashAttribute("lastVenteId", savedVente.getId());
    } catch (RuntimeException e) {
        redirectAttributes.addFlashAttribute("error",e.getMessage());

        return "redirect:/vente/nouveau";
    }

    return "redirect:/vente/nouveau";
}

@GetMapping("/vente/liste")
public String listeVente(@RequestParam(required = false ) BigDecimal min,@RequestParam(required = false ) BigDecimal max ,@RequestParam(required = false) Integer produitId, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="desc") String order, Model model){
    model.addAttribute("page", venteService.findByPrixTotalBetweenDate(min, max, produitId, order, page, 10));
    model.addAttribute("min", min);
    model.addAttribute("max", max);
    model.addAttribute("produitId", produitId);
    model.addAttribute("order", order);
    model.addAttribute("produits", venteService.getProduits());

    return "vente/liste";
}

@GetMapping("/vente/{id}/excel")
public ResponseEntity<InputStreamResource> exportExcel(
        @PathVariable Integer id
) throws IOException {

    Vente vente = venteService.findById(id);
    if (vente == null) {
        return ResponseEntity.notFound().build();
    }

    ByteArrayInputStream excelFile;
    excelFile = ventePdfService.export(vente);

    HttpHeaders headers = new HttpHeaders();

    headers.add(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=vente_" + id + ".xlsx"
    );

    return ResponseEntity.ok()
            .headers(headers)
            .contentType(
                MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
            )
            .body(new InputStreamResource(excelFile));
}

@PostMapping("/vente/import")
public String importVentes(@RequestParam("file") MultipartFile file,
        RedirectAttributes redirectAttributes) {

    try {
        List<Vente> ventes = ventePdfService.importExcel(file.getInputStream());
        venteService.importerVentes(ventes);
    } catch (IOException | RuntimeException e) {
        redirectAttributes.addFlashAttribute(
                "error",
                e.getMessage() != null ? e.getMessage() : "Erreur lors de l'import Excel.");
    }

    return "redirect:/vente/nouveau";
}

}
