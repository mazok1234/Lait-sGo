package com.example.demo.dto;

import java.time.LocalDateTime;

public class AlerteDTO {
    private Long id;
    private String titre;
    private String description;
    private String niveauCode;
    private String niveauLibelle;
    private String typeCode;
    private String typeLibelle;
    private Long vacheId;
    private String vacheNom;
    private String vacheBoucle;
    private Boolean acquittee;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getNiveauCode() { return niveauCode; }
    public void setNiveauCode(String niveauCode) { this.niveauCode = niveauCode; }
    public String getNiveauLibelle() { return niveauLibelle; }
    public void setNiveauLibelle(String niveauLibelle) { this.niveauLibelle = niveauLibelle; }
    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public String getTypeLibelle() { return typeLibelle; }
    public void setTypeLibelle(String typeLibelle) { this.typeLibelle = typeLibelle; }
    public Long getVacheId() { return vacheId; }
    public void setVacheId(Long vacheId) { this.vacheId = vacheId; }
    public String getVacheNom() { return vacheNom; }
    public void setVacheNom(String vacheNom) { this.vacheNom = vacheNom; }
    public String getVacheBoucle() { return vacheBoucle; }
    public void setVacheBoucle(String vacheBoucle) { this.vacheBoucle = vacheBoucle; }
    public Boolean getAcquittee() { return acquittee; }
    public void setAcquittee(Boolean acquittee) { this.acquittee = acquittee; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
