package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ref_niveau_alerte")
public class RefNiveauAlerte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String code; // "danger", "warning", "info"

    @Column(nullable = false, length = 50)
    private String libelle;

    public RefNiveauAlerte() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
}