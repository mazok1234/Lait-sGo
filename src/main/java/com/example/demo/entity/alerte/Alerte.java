package com.example.demo.entity.alerte;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerte")
public class Alerte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_niveau", nullable = false)
    private RefNiveauAlerte niveau;

    @Column(name = "type_alerte", nullable = false, length = 50)
    private String typeAlerte;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "vache_id")
    private Long vacheId;

    @Column(nullable = false)
    private Boolean acquittee = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Alerte() {}

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public RefNiveauAlerte getNiveau() { return niveau; }
    public void setNiveau(RefNiveauAlerte niveau) { this.niveau = niveau; }
    public String getTypeAlerte() { return typeAlerte; }
    public void setTypeAlerte(String typeAlerte) { this.typeAlerte = typeAlerte; }  
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getVacheId() { return vacheId; }
    public void setVacheId(Long vacheId) { this.vacheId = vacheId; }
    public Boolean getAcquittee() { return acquittee; }
    public void setAcquittee(Boolean acquittee) { this.acquittee = acquittee; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
