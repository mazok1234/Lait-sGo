package com.example.demo.dto;

import java.math.BigDecimal;

import com.example.demo.entity.sante.MedicamentFille;

public class MedicamentFilleDTO {
    private Long id;
    private BigDecimal dose;
    private String unite;
    private BigDecimal prixUnitaire;
    private Integer delaiAttenteLaitDefaut;
    private Integer delaiAttenteViandeDefaut;
    private String label;

    public static MedicamentFilleDTO from(MedicamentFille f) {
        MedicamentFilleDTO dto = new MedicamentFilleDTO();
        dto.id = f.getId();
        dto.dose = f.getDose();
        dto.unite = f.getUnite();
        dto.prixUnitaire = f.getPrixUnitaire();
        dto.delaiAttenteLaitDefaut = f.getDelaiAttenteLaitDefaut();
        dto.delaiAttenteViandeDefaut = f.getDelaiAttenteViandeDefaut();
        dto.label = f.getLabel();
        return dto;
    }

    public Long getId() { return id; }
    public BigDecimal getDose() { return dose; }
    public String getUnite() { return unite; }
    public BigDecimal getPrixUnitaire() { return prixUnitaire; }
    public Integer getDelaiAttenteLaitDefaut() { return delaiAttenteLaitDefaut; }
    public Integer getDelaiAttenteViandeDefaut() { return delaiAttenteViandeDefaut; }
    public String getLabel() { return label; }
}