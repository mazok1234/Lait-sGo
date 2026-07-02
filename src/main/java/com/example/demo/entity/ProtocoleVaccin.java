package com.example.demo.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "protocole_vaccin")
public class ProtocoleVaccin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProtocoleVaccin;

    @Column(name = "nom_vaccin", nullable = false)
    private String nomVaccin;

    @Column(name = "age_min_jours", nullable = false)
    private int ageMinJours;

    @Column(name = "age_max_jours", nullable = false)
    private int ageMaxJours;

    @Column(name = "duree_rappel_jours", nullable = false)
    private int dureeRappelJours;

    @OneToMany(mappedBy = "protocoleVaccin")
    private List<HistoriqueVaccin> historiques;

    public ProtocoleVaccin() {
    }

    public Integer getIdProtocoleVaccin() {
        return idProtocoleVaccin;
    }

    public void setIdProtocoleVaccin(Integer idProtocoleVaccin) {
        this.idProtocoleVaccin = idProtocoleVaccin;
    }

    public String getNomVaccin() {
        return nomVaccin;
    }

    public void setNomVaccin(String nomVaccin) {
        this.nomVaccin = nomVaccin;
    }

    public int getAgeMinJours() {
        return ageMinJours;
    }

    public void setAgeMinJours(int ageMinJours) {
        this.ageMinJours = ageMinJours;
    }

    public int getAgeMaxJours() {
        return ageMaxJours;
    }

    public void setAgeMaxJours(int ageMaxJours) {
        this.ageMaxJours = ageMaxJours;
    }

    public int getDureeRappelJours() {
        return dureeRappelJours;
    }

    public void setDureeRappelJours(int dureeRappelJours) {
        this.dureeRappelJours = dureeRappelJours;
    }

    public List<HistoriqueVaccin> getHistoriques() {
        return historiques;
    }

    public void setHistoriques(List<HistoriqueVaccin> historiques) {
        this.historiques = historiques;
    }

    @Override
    public String toString() {
        return this.nomVaccin != null ? this.nomVaccin : "Protocole #" + this.idProtocoleVaccin;
    }
}