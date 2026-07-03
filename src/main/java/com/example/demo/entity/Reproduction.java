package com.example.demo.entity; 

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "reproduction")
public class Reproduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vache_id", nullable = false)
    private Vache vache;

    @Column(name = "date_ia", nullable = false)
    private LocalDate dateIa;

    @Column(name = "gestation_confirmee")
    private Boolean gestationConfirmee; // Null au début, puis True/False après le contrôle

    @Column(name = "date_confirmation_gest")
    private LocalDate dateConfirmationGest; // Null tant que le contrôle n'est pas fait

    @Column(name = "date_velage_reel")
    private LocalDate dateVelageReel; // Null pendant toute la gestation (9 mois)

    @Column(name = "sexe_veau", length = 1)
    private String sexeVeau; // Null jusqu'au vêlage ('M' ou 'F')

    // --- Constructeurs ---
    public Reproduction() {}

    public Reproduction(Vache vache, LocalDate dateIa) {
        this.vache = vache;
        this.dateIa = dateIa;
    }

    // --- Getters et Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Vache getVache() { return vache; }
    public void setVache(Vache vache) { this.vache = vache; }

    public String getNumeroBoucle() {
        return vache != null ? vache.getNumeroBoucle() : "";
    }

    public LocalDate getDateIa() { return dateIa; }
    public void setDateIa(LocalDate dateIa) { this.dateIa = dateIa; }

    public Boolean getGestationConfirmee() { return gestationConfirmee; }
    public void setGestationConfirmee(Boolean gestationConfirmee) { this.gestationConfirmee = gestationConfirmee; }

    public LocalDate getDateConfirmationGest() { return dateConfirmationGest; }
    public void setDateConfirmationGest(LocalDate dateConfirmationGest) { this.dateConfirmationGest = dateConfirmationGest; }

    public LocalDate getDateVelageReel() { return dateVelageReel; }
    public void setDateVelageReel(LocalDate dateVelageReel) { this.dateVelageReel = dateVelageReel; }

    public String getSexeVeau() { return sexeVeau; }
    public void setSexeVeau(String sexeVeau) { this.sexeVeau = sexeVeau; }
}
