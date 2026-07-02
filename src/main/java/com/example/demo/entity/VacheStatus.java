package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "vache_status")
public class VacheStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vache", nullable = false)
    private Vache vache;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_status", nullable = false)
    private RefStatutVache statut;

    @Column(name = "id_debut", nullable = false)
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