package com.example.demo.services;

import com.example.demo.dto.AlerteDTO;
import com.example.demo.entity.Alerte;
import com.example.demo.entity.RefNiveauAlerte;
import com.example.demo.entity.RefTypeAlerte;
import com.example.demo.entity.Vache;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AlerteService {

    private final AlerteRepository alerteRepo;
    private final RefNiveauAlerteRepository niveauRepo;
    private final RefTypeAlerteRepository typeRepo;
    private final VacheRepository vacheRepo; // déjà disponible dans le projet

    // Tri fixe : danger=1, warning=2, info=3
    private static final Map<String, Integer> ORDRE_GRAVITE = Map.of(
        "danger",  1,
        "warning", 2,
        "info",    3
    );

    public AlerteService(AlerteRepository alerteRepo,
                         RefNiveauAlerteRepository niveauRepo,
                         RefTypeAlerteRepository typeRepo,
                         VacheRepository vacheRepo) {
        this.alerteRepo = alerteRepo;
        this.niveauRepo = niveauRepo;
        this.typeRepo   = typeRepo;
        this.vacheRepo  = vacheRepo;
    }

    // FA-01 : Créer une alerte (appelé par le endpoint REST)
    public Alerte creerAlerte(Integer idType, Integer idNiveau,
                               String titre, String description, Long vacheId) {
        RefNiveauAlerte niveau = niveauRepo.findById(idNiveau)
            .orElseThrow(() -> new IllegalArgumentException("Niveau introuvable : " + idNiveau));
        RefTypeAlerte type = typeRepo.findById(idType)
            .orElseThrow(() -> new IllegalArgumentException("Type introuvable : " + idType));

        Alerte alerte = new Alerte();
        alerte.setNiveau(niveau);
        alerte.setType(type);
        alerte.setTitre(titre);
        alerte.setDescription(description);
        alerte.setVacheId(vacheId);

        return alerteRepo.save(alerte);
    }

    // FA-02 + FA-04 : Lister les alertes non acquittées avec filtres et tri gravité
    public List<AlerteDTO> listerNonAcquittees(String niveauCode, Integer typeId) {
        List<Alerte> alertes;

        if (niveauCode != null && typeId != null) {
            alertes = alerteRepo
                .findByAcquitteeFalseAndNiveau_CodeAndType_IdOrderByCreatedAtDesc(niveauCode, typeId);
        } else if (niveauCode != null) {
            alertes = alerteRepo
                .findByAcquitteeFalseAndNiveau_CodeOrderByCreatedAtDesc(niveauCode);
        } else if (typeId != null) {
            alertes = alerteRepo
                .findByAcquitteeFalseAndType_IdOrderByCreatedAtDesc(typeId);
        } else {
            alertes = alerteRepo.findByAcquitteeFalseOrderByCreatedAtDesc();
        }

        // Tri danger > warning > info, puis par date décroissante
        alertes.sort(Comparator
            .comparingInt((Alerte a) ->
                ORDRE_GRAVITE.getOrDefault(a.getNiveau().getCode(), 99))
            .thenComparing(Comparator.comparing(Alerte::getCreatedAt).reversed())
        );

        return alertes.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // FA-03 : Acquitter une alerte
    public void acquitter(Long id) {
        Alerte alerte = alerteRepo.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Alerte introuvable : " + id));
        if (alerte.getAcquittee()) {
            throw new IllegalStateException("Alerte déjà acquittée");
        }
        alerte.setAcquittee(true);
        alerteRepo.save(alerte);
    }

    // FA-05 : Détail d'une alerte
    public AlerteDTO getDetail(Long id) {
        Alerte alerte = alerteRepo.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Alerte introuvable : " + id));
        return toDTO(alerte);
    }

    // Compteurs KPIs pour le dashboard
    public Map<String, Long> compterParNiveau() {
        return alerteRepo.findByAcquitteeFalseOrderByCreatedAtDesc()
            .stream()
            .collect(Collectors.groupingBy(
                a -> a.getNiveau().getCode(),
                Collectors.counting()
            ));
    }

    private AlerteDTO toDTO(Alerte a) {
        AlerteDTO dto = new AlerteDTO();
        dto.setId(a.getId());
        dto.setTitre(a.getTitre());
        dto.setDescription(a.getDescription());
        dto.setNiveauCode(a.getNiveau().getCode());
        dto.setNiveauLibelle(a.getNiveau().getLibelle());
        dto.setTypeCode(a.getType().getCode());
        dto.setTypeLibelle(a.getType().getLibelle());
        dto.setVacheId(a.getVacheId());
        dto.setAcquittee(a.getAcquittee());
        dto.setCreatedAt(a.getCreatedAt());

        // Enrichissement depuis VacheRepository (déjà disponible dans le projet)
        if (a.getVacheId() != null) {
            vacheRepo.findById(a.getVacheId()).ifPresent(v -> {
                dto.setVacheNom(v.getNumeroBoucle()); // utiliser numeroBoucle si pas de nom
                dto.setVacheBoucle(v.getNumeroBoucle());
            });
        }

        return dto;
    }
}