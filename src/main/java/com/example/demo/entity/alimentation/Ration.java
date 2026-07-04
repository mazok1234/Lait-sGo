package com.example.demo.entity.alimentation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "ration")
public class Ration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de la ration est obligatoire")
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @NotNull(message = "La phase de lactation est obligatoire")
    @Column(name = "id_phase_lactation", nullable = false)
    private Integer idPhaseLactation;

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

    public Integer getIdPhaseLactation() {
        return idPhaseLactation;
    }

    public void setIdPhaseLactation(Integer idPhaseLactation) {
        this.idPhaseLactation = idPhaseLactation;
    }
}
