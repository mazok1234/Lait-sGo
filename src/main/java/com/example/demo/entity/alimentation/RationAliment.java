package com.example.demo.entity.alimentation;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "ration_aliment")
public class RationAliment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La ration est obligatoire")
    @ManyToOne
    @JoinColumn(name = "ration_id", nullable = false)
    private Ration ration;

    @NotNull(message = "L'aliment est obligatoire")
    @ManyToOne
    @JoinColumn(name = "aliment_id", nullable = false)
    private Aliment aliment;

    @NotNull(message = "La quantite est obligatoire")
    @DecimalMin(value = "0.01", message = "La quantite doit etre superieure a 0")
    @Column(name = "quantite_kg", nullable = false, precision = 6, scale = 2)
    private BigDecimal quantiteKg;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ration getRation() {
        return ration;
    }

    public void setRation(Ration ration) {
        this.ration = ration;
    }

    public Aliment getAliment() {
        return aliment;
    }

    public void setAliment(Aliment aliment) {
        this.aliment = aliment;
    }

    public BigDecimal getQuantiteKg() {
        return quantiteKg;
    }

    public void setQuantiteKg(BigDecimal quantiteKg) {
        this.quantiteKg = quantiteKg;
    }
}
