package com.example.demo.entity.alimentation;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "ration")
public class Ration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de la ration est obligatoire")
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "id_phase_lactation")
    private Integer idPhaseLactation;

    @Column(name = "production_min_l", precision = 5, scale = 1)
    private BigDecimal productionMinL;

    @Column(name = "production_max_l", precision = 5, scale = 1)
    private BigDecimal productionMaxL;

    @Column(name = "bcs_min", precision = 3, scale = 2)
    private BigDecimal bcsMin;

    @Column(name = "bcs_max", precision = 3, scale = 2)
    private BigDecimal bcsMax;

    @Column(name = "id_statut_sante")
    private Integer idStatutSante;

    @Column(name = "priorite", nullable = false)
    private Integer priorite = 0;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Integer getIdPhaseLactation() {
        return idPhaseLactation;
    }

    public void setIdPhaseLactation(Integer idPhaseLactation) {
        this.idPhaseLactation = idPhaseLactation;
    }

    public BigDecimal getProductionMinL() {
        return productionMinL;
    }

    public void setProductionMinL(BigDecimal productionMinL) {
        this.productionMinL = productionMinL;
    }

    public BigDecimal getProductionMaxL() {
        return productionMaxL;
    }

    public void setProductionMaxL(BigDecimal productionMaxL) {
        this.productionMaxL = productionMaxL;
    }

    public BigDecimal getBcsMin() {
        return bcsMin;
    }

    public void setBcsMin(BigDecimal bcsMin) {
        this.bcsMin = bcsMin;
    }

    public BigDecimal getBcsMax() {
        return bcsMax;
    }

    public void setBcsMax(BigDecimal bcsMax) {
        this.bcsMax = bcsMax;
    }

    public Integer getIdStatutSante() {
        return idStatutSante;
    }

    public void setIdStatutSante(Integer idStatutSante) {
        this.idStatutSante = idStatutSante;
    }

    public Integer getPriorite() {
        return priorite;
    }

    public void setPriorite(Integer priorite) {
        this.priorite = priorite;
    }
}
