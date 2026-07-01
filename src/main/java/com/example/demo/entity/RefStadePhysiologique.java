package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "ref_stade_physiologique")
public class RefStadePhysiologique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Le code est obligatoire")
    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @NotBlank(message = "Le libelle est obligatoire")
    @Column(name = "libelle", nullable = false, length = 100)
    private String libelle;

    @NotNull(message = "Le jour min est obligatoire")
    @Min(value = 0, message = "Le jour min doit etre superieur ou egal a 0")
    @Column(name = "jour_min", nullable = false)
    private Integer jourMin;

    @NotNull(message = "Le jour max est obligatoire")
    @Min(value = 0, message = "Le jour max doit etre superieur ou egal a 0")
    @Column(name = "jour_max", nullable = false)
    private Integer jourMax;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public Integer getJourMin() {
        return jourMin;
    }

    public void setJourMin(Integer jourMin) {
        this.jourMin = jourMin;
    }

    public Integer getJourMax() {
        return jourMax;
    }

    public void setJourMax(Integer jourMax) {
        this.jourMax = jourMax;
    }
}
