package com.example.demo.controller;

import com.example.demo.entity.Alerte;
import com.example.demo.services.AlerteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alertes")
public class AlerteApiController {

    private final AlerteService alerteService;

    public AlerteApiController(AlerteService alerteService) {
        this.alerteService = alerteService;
    }

    // FA-01 : Réception d'une alerte poussée par un autre module
    @PostMapping
    public ResponseEntity<Alerte> creerAlerte(@Valid @RequestBody AlerteCreateRequest req) {
        Alerte alerte = alerteService.creerAlerte(
            req.getIdType(), req.getIdNiveau(),
            req.getTitre(), req.getDescription(), req.getVacheId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(alerte);
    }

    // Classe interne pour le body de la requête
    public static class AlerteCreateRequest {

        @NotNull
        private Integer idType;

        @NotNull
        private Integer idNiveau;

        @NotBlank
        @Size(max = 200)
        private String titre;

        private String description;

        private Long vacheId; // nullable si alerte globale ferme

        public Integer getIdType() { return idType; }
        public void setIdType(Integer idType) { this.idType = idType; }
        public Integer getIdNiveau() { return idNiveau; }
        public void setIdNiveau(Integer idNiveau) { this.idNiveau = idNiveau; }
        public String getTitre() { return titre; }
        public void setTitre(String titre) { this.titre = titre; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Long getVacheId() { return vacheId; }
        public void setVacheId(Long vacheId) { this.vacheId = vacheId; }
    }
}