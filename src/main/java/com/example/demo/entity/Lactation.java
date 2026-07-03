package com.example.demo.entity;

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
@Table(name = "lactation")
public class Lactation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vache_id", nullable = false)
    private Vache vache;

    @Column(name = "numero_lactation", nullable = false)
    private Short numeroLactation;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @ManyToOne
    @JoinColumn(name = "id_statut", nullable = false)
    private RefStatutLactation statut;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Lactation() {
    }

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

    public Short getNumeroLactation() {
        return numeroLactation;
    }

    public void setNumeroLactation(Short numeroLactation) {
        this.numeroLactation = numeroLactation;
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

    public RefStatutLactation getStatut() {
        return statut;
    }

    public void setStatut(RefStatutLactation statut) {
        this.statut = statut;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}