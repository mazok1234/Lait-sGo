package com.example.demo.entity.sante;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.demo.entity.cheptel.Vache;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "evenement_sante")
public class EvenementSante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La vache est obligatoire")
    @ManyToOne
    @JoinColumn(name = "vache_id", nullable = false)
    private Vache vache;

    @NotNull(message = "La maladie est obligatoire")
    @ManyToOne
    @JoinColumn(name = "maladie_id", nullable = false)
    private Maladie maladie;

    @NotNull(message = "La date de l'evenement est obligatoire")
    @Column(name = "date_evenement", nullable = false)
    private LocalDate dateEvenement;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "nbr_medicament", nullable = false)
    private Integer nbrMedicament = 0;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Vache getVache() {
        return vache;
    }

    public void setVache(Vache vache) {
        this.vache = vache;
    }

    public Maladie getMaladie() {
        return maladie;
    }

    public void setMaladie(Maladie maladie) {
        this.maladie = maladie;
    }

    public LocalDate getDateEvenement() {
        return dateEvenement;
    }

    public void setDateEvenement(LocalDate dateEvenement) {
        this.dateEvenement = dateEvenement;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getNbrMedicament() {
        return nbrMedicament;
    }

    public void setNbrMedicament(Integer nbrMedicament) {
        this.nbrMedicament = nbrMedicament;
    }


    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
