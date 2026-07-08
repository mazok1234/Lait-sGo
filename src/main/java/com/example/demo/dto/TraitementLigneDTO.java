package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TraitementLigneDTO {

    private Long id;
    private Long medicamentId;
    private Integer nbrMedicament;
    private BigDecimal dose;
    private String unite;
    private Integer dureeTraitement;
    private Integer delaiAttenteJ;
    private LocalDate dateDebut;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMedicamentId() { return medicamentId; }
    public void setMedicamentId(Long medicamentId) { this.medicamentId = medicamentId; }

    public Integer getNbrMedicament() { return nbrMedicament; }
    public void setNbrMedicament(Integer nbrMedicament) { this.nbrMedicament = nbrMedicament; }

    public BigDecimal getDose() { return dose; }
    public void setDose(BigDecimal dose) { this.dose = dose; }

    public String getUnite() { return unite; }
    public void setUnite(String unite) { this.unite = unite; }

    public Integer getDureeTraitement() { return dureeTraitement; }
    public void setDureeTraitement(Integer dureeTraitement) { this.dureeTraitement = dureeTraitement; }

    public Integer getDelaiAttenteJ() { return delaiAttenteJ; }
    public void setDelaiAttenteJ(Integer delaiAttenteJ) { this.delaiAttenteJ = delaiAttenteJ; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
}