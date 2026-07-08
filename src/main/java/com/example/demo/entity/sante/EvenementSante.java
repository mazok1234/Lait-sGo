package com.example.demo.entity.sante;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.entity.cheptel.Vache;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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

    @OneToMany(mappedBy = "evenementSante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TraitementSante> traitements = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Vache getVache() { return vache; }
    public void setVache(Vache vache) { this.vache = vache; }

    public Maladie getMaladie() { return maladie; }
    public void setMaladie(Maladie maladie) { this.maladie = maladie; }

    public LocalDate getDateEvenement() { return dateEvenement; }
    public void setDateEvenement(LocalDate dateEvenement) { this.dateEvenement = dateEvenement; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<TraitementSante> getTraitements() { return traitements; }
    public void setTraitements(List<TraitementSante> traitements) { this.traitements = traitements; }

    // --- Champs calculés utilisés par la liste ---

    @Transient
    public int getNombreMedicaments() {
        return traitements != null ? traitements.size() : 0;
    }

    @Transient
    public BigDecimal getPrixTotalTraitement() {
        if (traitements == null) return BigDecimal.ZERO;
        return traitements.stream()
                .map(TraitementSante::getPrixTotalMedicament)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transient
    public LocalDate getDateFinPlusTardive() {
        if (traitements == null || traitements.isEmpty()) return null;
        return traitements.stream()
                .map(TraitementSante::getDateFin)
                .filter(java.util.Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null);
    }

    @Transient
    public boolean isEnCours() {
        LocalDate finPlusTardive = getDateFinPlusTardive();
        return finPlusTardive != null && !LocalDate.now().isAfter(finPlusTardive);
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}