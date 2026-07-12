package com.example.demo.entity.sante;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "medicament_fille")
public class MedicamentFille {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le medicament parent est obligatoire")
    @ManyToOne
    @JoinColumn(name = "medicament_id", nullable = false)
    private Medicament medicament;

    @NotNull
    @Column(name = "dose", nullable = false)
    private BigDecimal dose;

    @NotNull
    @Column(name = "unite", nullable = false)
    private String unite;

    @Min(value = 0, message = "Le delai d'attente lait doit etre positif")
    @Column(name = "delai_attente_lait_defaut")
    private Integer delaiAttenteLaitDefaut = 0;

    @Min(value = 0, message = "Le delai d'attente viande doit etre positif")
    @Column(name = "delai_attente_viande_defaut")
    private Integer delaiAttenteViandeDefaut = 0;

    @Column(name = "prix_unitaire", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaire = BigDecimal.ZERO;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Medicament getMedicament() { return medicament; }
    public void setMedicament(Medicament medicament) { this.medicament = medicament; }

    public BigDecimal getDose() { return dose; }
    public void setDose(BigDecimal dose) { this.dose = dose; }

    public String getUnite() { return unite; }
    public void setUnite(String unite) { this.unite = unite; }

    public Integer getDelaiAttenteLaitDefaut() { return delaiAttenteLaitDefaut; }
    public void setDelaiAttenteLaitDefaut(Integer v) { this.delaiAttenteLaitDefaut = v; }

    public Integer getDelaiAttenteViandeDefaut() { return delaiAttenteViandeDefaut; }
    public void setDelaiAttenteViandeDefaut(Integer v) { this.delaiAttenteViandeDefaut = v; }

    public BigDecimal getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(BigDecimal prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    @Transient
    public String getLabel() {
        return (medicament != null ? medicament.getNom() : "")
                + " - " + dose + " " + unite + " - " + prixUnitaire + " Ar";
    }
}