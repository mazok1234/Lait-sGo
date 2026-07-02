package com.example.demo.cheptel.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "vache")
public class Vache {

    @Id
    private Long id;

    @Column(name = "numero_boucle", nullable = false, length = 20, unique = true)
    private String numeroBoucle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_race", nullable = false)
    private Race race;

    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance;

    @Column(name = "poids_kg")
    private BigDecimal poidsKg;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_statut", nullable = false)
    private StatutVache statut;

    // Correspond à "née à la ferme" si mere_id != null
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mere_id")
    private Vache mere;

    @Column(name = "score_bcs")
    private BigDecimal scoreBcs;

    @Column(name = "score_locomotion")
    private Short scoreLocomotion;

    public Vache() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroBoucle() {
        return numeroBoucle;
    }

    public void setNumeroBoucle(String numeroBoucle) {
        this.numeroBoucle = numeroBoucle;
    }

    public Race getRace() {
        return race;
    }

    public void setRace(Race race) {
        this.race = race;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public BigDecimal getPoidsKg() {
        return poidsKg;
    }

    public void setPoidsKg(BigDecimal poidsKg) {
        this.poidsKg = poidsKg;
    }

    public StatutVache getStatut() {
        return statut;
    }

    public void setStatut(StatutVache statut) {
        this.statut = statut;
    }

    public Vache getMere() {
        return mere;
    }

    public void setMere(Vache mere) {
        this.mere = mere;
    }

    public BigDecimal getScoreBcs() {
        return scoreBcs;
    }

    public void setScoreBcs(BigDecimal scoreBcs) {
        this.scoreBcs = scoreBcs;
    }

    public Short getScoreLocomotion() {
        return scoreLocomotion;
    }

    public void setScoreLocomotion(Short scoreLocomotion) {
        this.scoreLocomotion = scoreLocomotion;
    }
}

