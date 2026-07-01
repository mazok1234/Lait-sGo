package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "aliment")
public class Aliment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(name = "nom", nullable = false, unique = true, length = 100)
    private String nom;

    @NotNull(message = "Le type d'aliment est obligatoire")
    @ManyToOne
    @JoinColumn(name = "id_type_aliment", nullable = false)
    private RefTypeAliment typeAliment;

    @DecimalMin(value = "0.0", message = "La valeur doit être positive")
    @Column(name = "ufl", precision = 5, scale = 3)
    private BigDecimal ufl;

    @DecimalMin(value = "0.0", message = "La valeur doit être positive")
    @Column(name = "pdi_g", precision = 6, scale = 2)
    private BigDecimal pdiG;

    @DecimalMin(value = "0.0", message = "La valeur doit être positive")
    @Column(name = "seuil_alerte_kg", precision = 10, scale = 2)
    private BigDecimal seuilAlerteKg = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "La valeur doit être positive")
    @Column(name = "prix_par_kilo", precision = 8, scale = 2)
    private BigDecimal prixParKilo;

    public Aliment() {}

    public Aliment(Long id, String nom, RefTypeAliment typeAliment,
                   BigDecimal ufl, BigDecimal pdiG,
                   BigDecimal seuilAlerteKg, BigDecimal prixParKilo) {
        this.id = id;
        this.nom = nom;
        this.typeAliment = typeAliment;
        this.ufl = ufl;
        this.pdiG = pdiG;
        this.seuilAlerteKg = seuilAlerteKg;
        this.prixParKilo = prixParKilo;
    }

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

    public RefTypeAliment getTypeAliment() {
        return typeAliment;
    }

    public void setTypeAliment(RefTypeAliment typeAliment) {
        this.typeAliment = typeAliment;
    }

    public BigDecimal getUfl() {
        return ufl;
    }

    public void setUfl(BigDecimal ufl) {
        this.ufl = ufl;
    }

    public BigDecimal getPdiG() {
        return pdiG;
    }

    public void setPdiG(BigDecimal pdiG) {
        this.pdiG = pdiG;
    }

    public BigDecimal getSeuilAlerteKg() {
        return seuilAlerteKg;
    }

    public void setSeuilAlerteKg(BigDecimal seuilAlerteKg) {
        this.seuilAlerteKg = seuilAlerteKg;
    }

    public BigDecimal getPrixParKilo() {
        return prixParKilo;
    }

    public void setPrixParKilo(BigDecimal prixParKilo) {
        this.prixParKilo = prixParKilo;
    }
}