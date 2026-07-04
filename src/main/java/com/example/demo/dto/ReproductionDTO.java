package com.example.demo.dto;

import java.time.LocalDate;

public class ReproductionDTO {
    private Long id;
    private Long vacheId;
    private String numeroBoucleVache;
    private LocalDate dateIA;
    private String semence;
    private String inséminateur;
    private String typeInjection;
    private String statutIA = "en_attente";
    private Boolean gestationConfirmee;
    private LocalDate dateConfirmationGest;
    private LocalDate dateVelageReel;
    private String sexeVeau;

    public ReproductionDTO() {
    }

    public ReproductionDTO(Long vacheId, LocalDate dateIA, String semence, String inséminateur, String typeInjection) {
        this.vacheId = vacheId;
        this.dateIA = dateIA;
        this.semence = semence;
        this.inséminateur = inséminateur;
        this.typeInjection = typeInjection;
        this.statutIA = "en_attente";
    }

    public Long getId() {
        return id;
    }

    public Long getVacheId() {
        return vacheId;
    }

    public String getNumeroBoucleVache() {
        return numeroBoucleVache;
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

    public void setId(Long id) {
        this.id = id;
    }

    public void setVacheId(Long vacheId) {
        this.vacheId = vacheId;
    }

    public void setNumeroBoucleVache(String numeroBoucleVache) {
        this.numeroBoucleVache = numeroBoucleVache;
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

    @Override
    public String toString() {
        return "ReproductionDTO{" +
                "id=" + id +
                ", vacheId=" + vacheId +
                ", dateIA=" + dateIA +
                ", semence='" + semence + '\'' +
                ", inséminateur='" + inséminateur + '\'' +
                ", statutIA='" + statutIA + '\'' +
                '}';
    }
}
