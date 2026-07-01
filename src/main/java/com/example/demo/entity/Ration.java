package com.example.demo.entity;

import jakarta.persistence.*;
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

    @NotNull(message = "Le stade physiologique est obligatoire")
    @Column(name = "id_stade_physiologique", nullable = false)
    private Integer idStadePhysiologique;

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

    public Integer getIdStadePhysiologique() {
        return idStadePhysiologique;
    }

    public void setIdStadePhysiologique(Integer idStadePhysiologique) {
        this.idStadePhysiologique = idStadePhysiologique;
    }
}
