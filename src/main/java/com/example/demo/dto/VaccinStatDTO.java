package com.example.demo.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class VaccinStatDTO {
    private String nomVaccin;
    private Long nombreBovins;
    private LocalDate derniereVaccination;
    private LocalDate prochaineDate;

    public VaccinStatDTO(String nomVaccin, Long nombreBovins, LocalDate derniereVaccination, LocalDate prochaineDate) {
        this.nomVaccin = nomVaccin;
        this.nombreBovins = nombreBovins;
        this.derniereVaccination = derniereVaccination;
        this.prochaineDate = prochaineDate;
    }

    public String getNomVaccin() {
        return nomVaccin;
    }

    public void setNomVaccin(String nomVaccin) {
        this.nomVaccin = nomVaccin;
    }

    public Long getNombreBovins() {
        return nombreBovins;
    }

    public void setNombreBovins(Long nombreBovins) {
        this.nombreBovins = nombreBovins;
    }

    public LocalDate getDerniereVaccination() {
        return derniereVaccination;
    }

    public void setDerniereVaccination(LocalDate derniereVaccination) {
        this.derniereVaccination = derniereVaccination;
    }

    public LocalDate getProchaineDate() {
        return prochaineDate;
    }

    public void setProchaineDate(LocalDate prochaineDate) {
        this.prochaineDate = prochaineDate;
    }

    public String getProchaineDateFormatted() {
        if (prochaineDate != null) {
            return prochaineDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return "";
    }

    public String getDerniereVaccinationFormatted() {
        if (derniereVaccination != null) {
            return derniereVaccination.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return "";
    }

    public boolean isRappelUrgent() {
        if (prochaineDate != null) {
            LocalDate today = LocalDate.now();
            return !prochaineDate.isAfter(today);
        }
        return false;
    }

    public boolean isRappelProche() {
        if (prochaineDate != null) {
            LocalDate today = LocalDate.now();
            return !prochaineDate.isAfter(today.plusDays(7)) && prochaineDate.isAfter(today);
        }
        return false;
    }

    public boolean isRappelNormal() {
        if (prochaineDate != null) {
            LocalDate today = LocalDate.now();
            return prochaineDate.isAfter(today.plusDays(7));
        }
        return false;
    }
}
