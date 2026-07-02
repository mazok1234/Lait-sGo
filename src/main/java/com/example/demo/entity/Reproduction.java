package com.example.demo.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(name = "semence")
    private String semence;

    @Column(name = "inséminateur")
    private String inséminateur;

    @Column(name = "type_injection")
    private String typeInjection;

    @Column(name = "statut_ia")
    private String statutIA = "en_attente";

    @Column(name = "gestation_confirmee")
    private Boolean gestationConfirmee;

    @Column(name = "date_confirmation_gest")
    private LocalDate dateConfirmationGest;

    @Column(name = "date_velage_reel")
    private LocalDate dateVelageReel;

    @Column(name = "sexe_veau")
    private String sexeVeau;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ============ CONSTRUCTEURS ============
    public Reproduction() {
    }

    public Reproduction(Vache vache, LocalDate dateIA, String semence, String inséminateur, String typeInjection) {
        this.vache = vache;
        this.dateIA = dateIA;
        this.semence = semence;
        this.inséminateur = inséminateur;
        this.typeInjection = typeInjection;
        this.statutIA = "en_attente";
    }

    // ============ GETTERS ============
    public Long getId() {
        return id;
    }

    public Vache getVache() {
        return vache;
    }

    public LocalDate getDateIA() {
        return dateIA;
    }

    public String getSemence() {
        return semence;
    }

    public String getInséminateur() {
        return inséminateur;
    }

    public String getTypeInjection() {
        return typeInjection;
    }

    public String getStatutIA() {
        return statutIA;
    }

    public Boolean getGestationConfirmee() {
        return gestationConfirmee;
    }

    public LocalDate getDateConfirmationGest() {
        return dateConfirmationGest;
    }

    public LocalDate getDateVelageReel() {
        return dateVelageReel;
    }

    public String getSexeVeau() {
        return sexeVeau;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ============ SETTERS ============
    public void setId(Long id) {
        this.id = id;
    }

    public void setVache(Vache vache) {
        this.vache = vache;
    }

    public void setDateIA(LocalDate dateIA) {
        this.dateIA = dateIA;
    }

    public void setSemence(String semence) {
        this.semence = semence;
    }

    public void setInséminateur(String inséminateur) {
        this.inséminateur = inséminateur;
    }

    public void setTypeInjection(String typeInjection) {
        this.typeInjection = typeInjection;
    }

    public void setStatutIA(String statutIA) {
        this.statutIA = statutIA;
    }

    public void setGestationConfirmee(Boolean gestationConfirmee) {
        this.gestationConfirmee = gestationConfirmee;
    }

    public void setDateConfirmationGest(LocalDate dateConfirmationGest) {
        this.dateConfirmationGest = dateConfirmationGest;
    }

    public void setDateVelageReel(LocalDate dateVelageReel) {
        this.dateVelageReel = dateVelageReel;
    }

    public void setSexeVeau(String sexeVeau) {
        this.sexeVeau = sexeVeau;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Reproduction{" +
                "id=" + id +
                ", vacheId=" + (vache != null ? vache.getId() : null) +
                ", dateIA=" + dateIA +
                ", semence='" + semence + '\'' +
                ", inséminateur='" + inséminateur + '\'' +
                ", statutIA='" + statutIA + '\'' +
                '}';
    }
}
