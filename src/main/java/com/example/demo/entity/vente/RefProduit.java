package com.example.demo.entity.vente;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ref_produit")
public class RefProduit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String libelle;

    @Column(name = "unite_defaut", nullable = false, length = 10)
    private String uniteDefaut;

    public RefProduit() {}

    public RefProduit(String code, String libelle, String uniteDefaut) {
        this.code = code;
        this.libelle = libelle;
        this.uniteDefaut = uniteDefaut;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public String getUniteDefaut() { return uniteDefaut; }
    public void setUniteDefaut(String uniteDefaut) { this.uniteDefaut = uniteDefaut; }
}