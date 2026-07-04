package com.example.demo.entity.sante;

import com.example.demo.entity.cheptel.Vache;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "historique_vaccin")
public class HistoriqueVaccin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idHistoriqueVaccin;

    @ManyToOne
    @JoinColumn(name = "vache_id", nullable = false)
    private Vache vache;

    @ManyToOne
    @JoinColumn(name = "id_protocole_vaccin", nullable = false)
    private ProtocoleVaccin protocoleVaccin;

    @Column(name = "date_vaccination", nullable = false)
    private LocalDate dateVaccination;

    @Column(name = "type_injection", nullable = false)
    private String typeInjection;

    @Transient
    private Long alerteId;

    public HistoriqueVaccin() {
    }

    public Long getAlerteId() {
        return alerteId;
    }

    public void setAlerteId(Long alerteId) {
        this.alerteId = alerteId;
    }

    public Integer getIdHistoriqueVaccin() {
        return idHistoriqueVaccin;
    }

    public void setIdHistoriqueVaccin(Integer idHistoriqueVaccin) {
        this.idHistoriqueVaccin = idHistoriqueVaccin;
    }

    public Vache getVache() {
        return vache;
    }

    public void setVache(Vache vache) {
        this.vache = vache;
    }

    public ProtocoleVaccin getProtocoleVaccin() {
        return protocoleVaccin;
    }

    public void setProtocoleVaccin(ProtocoleVaccin protocoleVaccin) {
        this.protocoleVaccin = protocoleVaccin;
    }

    public LocalDate getDateVaccination() {
        return dateVaccination;
    }

    public void setDateVaccination(LocalDate dateVaccination) {
        this.dateVaccination = dateVaccination;
    }

    public String getTypeInjection() {
        return typeInjection;
    }

    public void setTypeInjection(String typeInjection) {
        this.typeInjection = typeInjection;
    }
}
