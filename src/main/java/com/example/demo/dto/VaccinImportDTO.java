package com.example.demo.dto;

public class VaccinImportDTO {
    private String numeroBoucle;
    private String codeVaccin; 
    private String dateVaccination;
    private String typeInjection;


    public String getNumeroBoucle() {
        return numeroBoucle;
    }
    public void setNumeroBoucle(String numeroBoucle) {
        this.numeroBoucle = numeroBoucle;
    }

    public String getCodeVaccin() {
        return codeVaccin;
    }

    public void setCodeVaccin(String codeVaccin) {
        this.codeVaccin = codeVaccin;
    }

    public String getDateVaccination() {
        return dateVaccination;
    }

    public void setDateVaccination(String dateVaccination) {
        this.dateVaccination = dateVaccination;
    }

    public String getTypeInjection() {
        return typeInjection;
    }
    public void setTypeInjection(String typeInjection) {
        this.typeInjection = typeInjection;
    }
    public VaccinImportDTO() {
    }

    public VaccinImportDTO(String numeroBoucle, String codeVaccin, String dateVaccination, String typeInjection) {
        this.numeroBoucle = numeroBoucle;
        this.codeVaccin = codeVaccin;
        this.dateVaccination = dateVaccination;
        this.typeInjection = typeInjection;
    }
}