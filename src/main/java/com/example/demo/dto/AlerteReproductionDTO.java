package com.example.demo.dto;

import java.time.LocalDate;

public class AlerteReproductionDTO {
    private Long id;
    private Long vacheId;
    private String numeroBoucle;
    private LocalDate dateIA;
    private LocalDate dateVelagePrevue;
    private long joursRestants;
    private String niveauUrgence;

    public AlerteReproductionDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVacheId() { return vacheId; }
    public void setVacheId(Long vacheId) { this.vacheId = vacheId; }

    public String getNumeroBoucle() { return numeroBoucle; }
    public void setNumeroBoucle(String numeroBoucle) { this.numeroBoucle = numeroBoucle; }

    public LocalDate getDateIA() { return dateIA; }
    public void setDateIA(LocalDate dateIA) { this.dateIA = dateIA; }

    public LocalDate getDateVelagePrevue() { return dateVelagePrevue; }
    public void setDateVelagePrevue(LocalDate dateVelagePrevue) { this.dateVelagePrevue = dateVelagePrevue; }

    public long getJoursRestants() { return joursRestants; }
    public void setJoursRestants(long joursRestants) { this.joursRestants = joursRestants; }

    public String getNiveauUrgence() { return niveauUrgence; }
    public void setNiveauUrgence(String niveauUrgence) { this.niveauUrgence = niveauUrgence; }
}
