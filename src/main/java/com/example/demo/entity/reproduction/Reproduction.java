package com.example.demo.entity.reproduction;

import com.example.demo.entity.cheptel.Vache;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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
    private LocalDate dateIA;

    @Column(name = "gestation_confirmee")
    private Boolean gestationConfirmee;

    @Column(name = "date_confirmation_gest")
    private LocalDate dateConfirmationGest;

    @Column(name = "date_velage_reel")
    private LocalDate dateVelageReel;

    @Column(name = "sexe_veau", length = 1)
    private String sexeVeau;

    @Column(name = "statut_ia", length = 20)
    private String statutIA;

    @Column(name = "semence")
    private String semence;

    @Column(name = "inseminateur")
    private String inseminateur;

    @Column(name = "type_injection")
    private String typeInjection;

    public Reproduction() {}

    public Reproduction(Vache vache, LocalDate dateIA) {
        this.vache = vache;
        this.dateIA = dateIA;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Vache getVache() { return vache; }
    public void setVache(Vache vache) { this.vache = vache; }

    public String getNumeroBoucle() {
        return vache != null ? vache.getNumeroBoucle() : "";
    }

    public LocalDate getDateIA() { return dateIA; }
    public void setDateIA(LocalDate dateIA) { this.dateIA = dateIA; }

    public Boolean getGestationConfirmee() { return gestationConfirmee; }
    public void setGestationConfirmee(Boolean gestationConfirmee) { this.gestationConfirmee = gestationConfirmee; }

    public LocalDate getDateConfirmationGest() { return dateConfirmationGest; }
    public void setDateConfirmationGest(LocalDate dateConfirmationGest) { this.dateConfirmationGest = dateConfirmationGest; }

    public LocalDate getDateVelageReel() { return dateVelageReel; }
    public void setDateVelageReel(LocalDate dateVelageReel) { this.dateVelageReel = dateVelageReel; }

    public String getSexeVeau() { return sexeVeau; }
    public void setSexeVeau(String sexeVeau) { this.sexeVeau = sexeVeau; }

    public String getStatutIA() { return statutIA; }
    public void setStatutIA(String statutIA) { this.statutIA = statutIA; }

    public String getSemence() { return semence; }
    public void setSemence(String semence) { this.semence = semence; }

    public String getInséminateur() { return inseminateur; }
    public void setInséminateur(String inseminateur) { this.inseminateur = inseminateur; }

    public String getTypeInjection() { return typeInjection; }
    public void setTypeInjection(String typeInjection) { this.typeInjection = typeInjection; }
}
