package com.example.demo.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name="vente")
public class Vente{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "date_vente")
    private LocalDate dateVente;

    @Column(name = "quantite_lait")
    private BigDecimal quantiteLait;

    @Column(name = "prix_unitaire")
    private BigDecimal prixUnitaire;

    @Column(name = "created_at")
    private LocalDateTime createdAt;




}