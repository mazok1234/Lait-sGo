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
    @JoinColumn(name = "medicament_fille_id", nullable = false)
    private MedicamentFille medicamentFille;

    @Column(name = "nbr_medicament", nullable = false)
    private Integer nbrMedicament = 1;

    @NotNull
    @Column(name = "duree_traitement", nullable = false)
    private Integer dureeTraitement;

    // Override possible du delai par defaut de la fiche fille
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
    public void setEvenementSante(EvenementSante e) { this.evenementSante = e; }

    public MedicamentFille getMedicamentFille() { return medicamentFille; }
    public void setMedicamentFille(MedicamentFille medicamentFille) { this.medicamentFille = medicamentFille; }

    public Integer getNbrMedicament() { return nbrMedicament; }
    public void setNbrMedicament(Integer n) { this.nbrMedicament = n; }

    public Integer getDureeTraitement() { return dureeTraitement; }
    public void setDureeTraitement(Integer d) { this.dureeTraitement = d; }

    public Integer getDelaiAttenteJ() { return delaiAttenteJ; }
    public void setDelaiAttenteJ(Integer d) { this.delaiAttenteJ = d; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate d) { this.dateDebut = d; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate d) { this.dateFin = d; }

    // --- Delegates vers medicamentFille : evite de toucher aux templates existants ---

    @Transient
    public Medicament getMedicament() {
        return medicamentFille != null ? medicamentFille.getMedicament() : null;
    }

    @Transient
    public BigDecimal getDose() {
        return medicamentFille != null ? medicamentFille.getDose() : null;
    }

    @Transient
    public String getUnite() {
        return medicamentFille != null ? medicamentFille.getUnite() : null;
    }

    @Transient
    public BigDecimal getPrixUnitaireMedicament() {
        return (medicamentFille != null && medicamentFille.getPrixUnitaire() != null)
                ? medicamentFille.getPrixUnitaire()
                : BigDecimal.ZERO;
    }

    @Transient
    public BigDecimal getPrixTotalMedicament() {
        int qte = nbrMedicament != null ? nbrMedicament : 0;
        return getPrixUnitaireMedicament().multiply(BigDecimal.valueOf(qte));
    }
}