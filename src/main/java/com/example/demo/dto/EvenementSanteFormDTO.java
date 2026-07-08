package com.example.demo.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EvenementSanteFormDTO {

    private Long id;
    private Long vacheId;
    private Long maladieId;
    private LocalDate dateEvenement;
    private String description;
    private List<TraitementLigneDTO> lignes = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVacheId() { return vacheId; }
    public void setVacheId(Long vacheId) { this.vacheId = vacheId; }

    public Long getMaladieId() { return maladieId; }
    public void setMaladieId(Long maladieId) { this.maladieId = maladieId; }

    public LocalDate getDateEvenement() { return dateEvenement; }
    public void setDateEvenement(LocalDate dateEvenement) { this.dateEvenement = dateEvenement; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<TraitementLigneDTO> getLignes() { return lignes; }
    public void setLignes(List<TraitementLigneDTO> lignes) { this.lignes = lignes; }
}