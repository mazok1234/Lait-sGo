package com.example.demo.entity.production;

import com.example.demo.entity.auth.Utilisateur;

import com.example.demo.entity.cheptel.Vache;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "production")
public class Production {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lactation_id", nullable = false)
    private Lactation lactation;

    @ManyToOne
    @JoinColumn(name = "vache_id", nullable = false)
    private Vache vache;

    @Column(name = "date_production", nullable = false)
    private LocalDate dateProduction;

    @Column(name = "quantite_litres", nullable = false, precision = 6, scale = 2)
    private BigDecimal quantiteLitres;

    @Column(name = "quantite_matin", precision = 6, scale = 2)
    private BigDecimal quantiteMatin;

    @Column(name = "quantite_soir", precision = 6, scale = 2)
    private BigDecimal quantiteSoir;

    @Column(name = "quantite_restante", precision = 6, scale = 2)
    private BigDecimal quantiteRestante = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Utilisateur createdBy;

    public Production() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Lactation getLactation() {
        return lactation;
    }

    public void setLactation(Lactation lactation) {
        this.lactation = lactation;
    }

    public Vache getVache() {
        return vache;
    }

    public void setVache(Vache vache) {
        this.vache = vache;
    }

    public LocalDate getDateProduction() {
        return dateProduction;
    }

    public void setDateProduction(LocalDate dateProduction) {
        this.dateProduction = dateProduction;
    }

    public BigDecimal getQuantiteLitres() {
        return quantiteLitres;
    }

    public void setQuantiteLitres(BigDecimal quantiteLitres) {
        this.quantiteLitres = quantiteLitres;
    }

    public BigDecimal getQuantiteMatin() {
        return quantiteMatin;
    }

    public void setQuantiteMatin(BigDecimal quantiteMatin) {
        this.quantiteMatin = quantiteMatin;
    }

    public BigDecimal getQuantiteSoir() {
        return quantiteSoir;
    }

    public void setQuantiteSoir(BigDecimal quantiteSoir) {
        this.quantiteSoir = quantiteSoir;
    }

    public BigDecimal getQuantiteRestante() {
        return quantiteRestante;
    }

    public void setQuantiteRestante(BigDecimal quantiteRestante) {
        this.quantiteRestante = quantiteRestante;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Utilisateur getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Utilisateur createdBy) {
        this.createdBy = createdBy;
    }

    @PrePersist
    void prePersist() {
        if (quantiteRestante == null) {
            quantiteRestante = BigDecimal.ZERO;
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
