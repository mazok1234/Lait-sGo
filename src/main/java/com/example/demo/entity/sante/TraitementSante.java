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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "traitement_sante")
public class TraitementSante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "evenement_sante_id", nullable = false)
    private EvenementSante evenementSante;

    @ManyToOne
    @JoinColumn(name = "medicament_id", nullable = false)
    private Medicament medicament;

    @NotNull(message = "La dose est obligatoire")
    @DecimalMin(value = "0.01", message = "La dose doit etre superieure a 0")
    @Column(name = "dose", nullable = false, precision = 10, scale = 2)
    private BigDecimal dose;

    @NotBlank(message = "L'unite est obligatoire")
    @Column(name = "unite", nullable = false, length = 50)
    private String unite;

    @NotNull(message = "La duree du traitement est obligatoire")
    @Min(value = 1, message = "La duree du traitement doit etre au moins de 1 jour")
    @Column(name = "duree_traitement", nullable = false)
    private Integer dureeTraitement;

    @NotNull(message = "Le delai d'attente est obligatoire")
    @Min(value = 0, message = "Le delai d'attente doit etre positif")
    @Column(name = "delai_attente_j", nullable = false)
    private Integer delaiAttenteJ;

    @NotNull(message = "La date de debut est obligatoire")
    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Transient
    @NotNull(message = "La vache est obligatoire")
    private Long vacheId;

    @Transient
    @NotNull(message = "La maladie est obligatoire")
    private Long maladieId;

    @Transient
    @NotNull(message = "Le medicament est obligatoire")
    private Long medicamentId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EvenementSante getEvenementSante() {
        return evenementSante;
    }

    public void setEvenementSante(EvenementSante evenementSante) {
        this.evenementSante = evenementSante;
    }

    public Medicament getMedicament() {
        return medicament;
    }

    public void setMedicament(Medicament medicament) {
        this.medicament = medicament;
    }

    public BigDecimal getDose() {
        return dose;
    }

    public void setDose(BigDecimal dose) {
        this.dose = dose;
    }

    public String getUnite() {
        return unite;
    }

    public void setUnite(String unite) {
        this.unite = unite;
    }

    public Integer getDureeTraitement() {
        return dureeTraitement;
    }

    public void setDureeTraitement(Integer dureeTraitement) {
        this.dureeTraitement = dureeTraitement;
    }

    public Integer getDelaiAttenteJ() {
        return delaiAttenteJ;
    }

    public void setDelaiAttenteJ(Integer delaiAttenteJ) {
        this.delaiAttenteJ = delaiAttenteJ;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public Long getVacheId() {
        if (evenementSante != null && evenementSante.getVache() != null) {
            return evenementSante.getVache().getId();
        }
        return vacheId;
    }

    public void setVacheId(Long vacheId) {
        this.vacheId = vacheId;
    }

    public Long getMaladieId() {
        if (evenementSante != null && evenementSante.getMaladie() != null) {
            return evenementSante.getMaladie().getId();
        }
        return maladieId;
    }

    public void setMaladieId(Long maladieId) {
        this.maladieId = maladieId;
    }

    public Long getMedicamentId() {
        if (medicament != null) {
            return medicament.getId();
        }
        return medicamentId;
    }

    public void setMedicamentId(Long medicamentId) {
        this.medicamentId = medicamentId;
    }
}
