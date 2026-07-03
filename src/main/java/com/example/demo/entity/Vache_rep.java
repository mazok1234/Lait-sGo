package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(name = "vache")
public class Vache_rep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_boucle", nullable = false, unique = true, length = 20)
    private String numeroBoucle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_race", nullable = false)
    private RefRace race;

    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance;

    @Column(name = "poids_kg", precision = 6, scale = 1)
    private BigDecimal poidsKg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mere_id")
    private Vache mere;

    @Column(name = "score_bcs", precision = 3, scale = 2)
    private BigDecimal scoreBcs;

    @Column(name = "score_locomotion")
    private Short scoreLocomotion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();

    // Constructeurs
    public Vache_rep() {}

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroBoucle() { return numeroBoucle; }
    public void setNumeroBoucle(String numeroBoucle) { this.numeroBoucle = numeroBoucle; }

    public RefRace getRace() { return race; }
    public void setRace(RefRace race) { this.race = race; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public BigDecimal getPoidsKg() { return poidsKg; }
    public void setPoidsKg(BigDecimal poidsKg) { this.poidsKg = poidsKg; }

    public Vache getMere() { return mere; }
    public void setMere(Vache mere) { this.mere = mere; }

    public BigDecimal getScoreBcs() { return scoreBcs; }
    public void setScoreBcs(BigDecimal scoreBcs) { this.scoreBcs = scoreBcs; }

    public Short getScoreLocomotion() { return scoreLocomotion; }
    public void setScoreLocomotion(Short scoreLocomotion) { this.scoreLocomotion = scoreLocomotion; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
}