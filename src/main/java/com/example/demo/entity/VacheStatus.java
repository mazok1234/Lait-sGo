package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "vache_statut")
public class VacheStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vache_id", nullable = false)
    private Vache vache;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statut_id", nullable = false)
    private RefStatutVache statut;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    // Constructeurs
    public VacheStatus() {}

    public VacheStatus(Vache vache, RefStatutVache statut, LocalDate dateDebut) {
        this.vache = vache;
        this.statut = statut;
        this.dateDebut = dateDebut;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Vache getVache() { return vache; }
    public void setVache(Vache vache) { this.vache = vache; }

    public RefStatutVache getStatut() { return statut; }
    public void setStatut(RefStatutVache statut) { this.statut = statut; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
}