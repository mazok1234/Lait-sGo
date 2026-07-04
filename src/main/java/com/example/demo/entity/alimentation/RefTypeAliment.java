package com.example.demo.entity.alimentation;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "ref_type_aliment")
public class RefTypeAliment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Le code est obligatoire")
    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @NotBlank(message = "Le libellé est obligatoire")
    @Column(name = "libelle", nullable = false, length = 100)
    private String libelle;

    public RefTypeAliment() {}

    public RefTypeAliment(Integer id, String code, String libelle) {
        this.id = id;
        this.code = code;
        this.libelle = libelle;
    }

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
}
