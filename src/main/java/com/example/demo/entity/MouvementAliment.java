package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mouvement_aliment")
public class MouvementAliment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "L'aliment est obligatoire")
    @ManyToOne
    @JoinColumn(name = "aliment_id", nullable = false)
    private Aliment aliment;

    @NotNull(message = "Le type de mouvement est obligatoire")
    @Column(name = "type_mouvement", nullable = false, length = 10)
    private String typeMouvement; // entree / sortie

    @NotNull(message = "La quantité est obligatoire")
    @DecimalMin(value = "0.01", message = "La quantité doit être supérieure à 0")
    @Column(name = "quantite_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantiteKg;

    @NotNull(message = "La date est obligatoire")
    @Column(name = "date_mouvement", nullable = false)
    private LocalDate dateMouvement;

    // null pour l'instant, sera rempli lors de l'implémentation de l'auth
    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public MouvementAliment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Aliment getAliment() { return aliment; }
    public void setAliment(Aliment aliment) { this.aliment = aliment; }

    public String getTypeMouvement() { return typeMouvement; }
    public void setTypeMouvement(String typeMouvement) { this.typeMouvement = typeMouvement; }

    public BigDecimal getQuantiteKg() { return quantiteKg; }
    public void setQuantiteKg(BigDecimal quantiteKg) { this.quantiteKg = quantiteKg; }

    public LocalDate getDateMouvement() { return dateMouvement; }
    public void setDateMouvement(LocalDate dateMouvement) { this.dateMouvement = dateMouvement; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}