package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TraitementLigneDTO {
    private Long id;
    private Long medicamentId;
    private Long medicamentFilleId;
    private BigDecimal dose;
    private String unite;
    private BigDecimal prixUnitaire;
    private Integer nbrMedicament = 1;
    private Integer dureeTraitement;
    private Integer delaiAttenteJ;
    private LocalDate dateDebut;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMedicamentId() { return medicamentId; }
    public void setMedicamentId(Long medicamentId) { this.medicamentId = medicamentId; }

    public Long getMedicamentFilleId() { return medicamentFilleId; }
    public void setMedicamentFilleId(Long id) { this.medicamentFilleId = id; }

    public BigDecimal getDose() { return dose; }
    public void setDose(BigDecimal dose) { this.dose = dose; }

    public String getUnite() { return unite; }
    public void setUnite(String unite) { this.unite = unite; }

    public BigDecimal getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(BigDecimal prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    public Integer getNbrMedicament() { return nbrMedicament; }
    public void setNbrMedicament(Integer n) { this.nbrMedicament = n; }

    public Integer getDureeTraitement() { return dureeTraitement; }
    public void setDureeTraitement(Integer d) { this.dureeTraitement = d; }

    public Integer getDelaiAttenteJ() { return delaiAttenteJ; }
    public void setDelaiAttenteJ(Integer d) { this.delaiAttenteJ = d; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate d) { this.dateDebut = d; }
}