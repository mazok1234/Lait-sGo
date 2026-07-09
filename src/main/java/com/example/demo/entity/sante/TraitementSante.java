package com.example.demo.entity.sante;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "traitement_sante")
public class TraitementSante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "evenement_sante_id", nullable = false)
    private EvenementSante evenementSante;

    @NotNull(message = "Le medicament est obligatoire")
    @ManyToOne
    @JoinColumn(name = "medicament_id", nullable = false)
    private Medicament medicament;

    @Column(name = "nbr_medicament", nullable = false)
    private Integer nbrMedicament = 1;

    @NotNull
    @Column(name = "dose", nullable = false)
    private BigDecimal dose;

    @NotNull
    @Column(name = "unite", nullable = false)
    private String unite;

    @NotNull
    @Column(name = "duree_traitement", nullable = false)
    private Integer dureeTraitement;

    @Column(name = "delai_attente_j", nullable = false)
    private Integer delaiAttenteJ = 0;

    @NotNull
    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public EvenementSante getEvenementSante() { return evenementSante; }
    public void setEvenementSante(EvenementSante evenementSante) { this.evenementSante = evenementSante; }

    public Medicament getMedicament() { return medicament; }
    public void setMedicament(Medicament medicament) { this.medicament = medicament; }

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

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    // --- Champs calculés (non mappés) utilisés par la page liste ---

    @Transient
    public BigDecimal getPrixUnitaireMedicament() {
        return (medicament != null && medicament.getPrixUnitaire() != null)
                ? medicament.getPrixUnitaire()
                : BigDecimal.ZERO;
    }

    @Transient
    public BigDecimal getPrixTotalMedicament() {
        int qte = nbrMedicament != null ? nbrMedicament : 0;
        return getPrixUnitaireMedicament().multiply(BigDecimal.valueOf(qte));
    }
}