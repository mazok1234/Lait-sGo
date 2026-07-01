package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "medicament")
public class Medicament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du medicament est obligatoire")
    @Column(name = "nom", nullable = false, length = 150)
    private String nom;

    @Min(value = 0, message = "Le delai d'attente lait doit etre positif")
    @Column(name = "delai_attente_lait_defaut")
    private Integer delaiAttenteLaitDefaut = 0;

    @Min(value = 0, message = "Le delai d'attente viande doit etre positif")
    @Column(name = "delai_attente_viande_defaut")
    private Integer delaiAttenteViandeDefaut = 0;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Integer getDelaiAttenteLaitDefaut() {
        return delaiAttenteLaitDefaut;
    }

    public void setDelaiAttenteLaitDefaut(Integer delaiAttenteLaitDefaut) {
        this.delaiAttenteLaitDefaut = delaiAttenteLaitDefaut;
    }

    public Integer getDelaiAttenteViandeDefaut() {
        return delaiAttenteViandeDefaut;
    }

    public void setDelaiAttenteViandeDefaut(Integer delaiAttenteViandeDefaut) {
        this.delaiAttenteViandeDefaut = delaiAttenteViandeDefaut;
    }
}