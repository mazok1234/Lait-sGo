package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "v_reproduction_suivi")
public class VReproductionSuivi {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vache_id")
    private Vache vache;

    @Column(name = "date_ia")
    private LocalDate dateIa;

    @Column(name = "date_velage_prevu")
    private LocalDate dateVelagePrevu;

    @Column(name = "gestation_confirmee")
    private Boolean gestationConfirmee;

    @Column(name = "date_confirmation_gest")
    private LocalDate dateConfirmationGest;

    @Column(name = "date_velage_reel")
    private LocalDate dateVelageReel;

    @Column(name = "sexe_veau")
    private String sexeVeau;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Vache getVache() { return vache; }
    public void setVache(Vache vache) { this.vache = vache; }
    public LocalDate getDateIa() { return dateIa; }
    public void setDateIa(LocalDate dateIa) { this.dateIa = dateIa; }
    public LocalDate getDateVelagePrevu() { return dateVelagePrevu; }
    public void setDateVelagePrevu(LocalDate dateVelagePrevu) { this.dateVelagePrevu = dateVelagePrevu; }
    public Boolean getGestationConfirmee() { return gestationConfirmee; }
    public void setGestationConfirmee(Boolean gestationConfirmee) { this.gestationConfirmee = gestationConfirmee; }
    public LocalDate getDateConfirmationGest() { return dateConfirmationGest; }
    public void setDateConfirmationGest(LocalDate dateConfirmationGest) { this.dateConfirmationGest = dateConfirmationGest; }
    public LocalDate getDateVelageReel() { return dateVelageReel; }
    public void setDateVelageReel(LocalDate dateVelageReel) { this.dateVelageReel = dateVelageReel; }
    public String getSexeVeau() { return sexeVeau; }
    public void setSexeVeau(String sexeVeau) { this.sexeVeau = sexeVeau; }
}