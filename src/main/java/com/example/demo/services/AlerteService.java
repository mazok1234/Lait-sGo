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
    private final VacheRepository vacheRepo;

    public AlerteService(AlerteRepository alerteRepo,
            RefNiveauAlerteRepository niveauRepo,
            RefTypeAlerteRepository typeRepo,
            VacheRepository vacheRepo) {
        this.alerteRepo = alerteRepo;
        this.niveauRepo = niveauRepo;
        this.typeRepo = typeRepo;
        this.vacheRepo = vacheRepo;
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

        alertes.sort(Comparator
                .comparingInt((Alerte a) -> a.getNiveau().getOrdre())
                .thenComparing(Comparator.comparing(Alerte::getCreatedAt).reversed()));

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
                        Collectors.counting()));
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

        if (a.getVacheId() != null) {
            vacheRepo.findById(a.getVacheId()).ifPresent(v -> {
                dto.setVacheNom(v.getNumeroBoucle());
                dto.setVacheBoucle(v.getNumeroBoucle());
            });
        }

        return dto;
    }

// FA-02 : Envoyer une alerte automatique (avec détection de doublons intelligents)
    public void envoyerAlerte(String typeCode, String niveauCode, String titre, String description, Long vacheId) {
        try {
            RefTypeAlerte type = typeRepo.findByCode(typeCode)
                    .orElseThrow(() -> new IllegalArgumentException("Type d'alerte introuvable : " + typeCode));
            RefNiveauAlerte niveau = niveauRepo.findByCode(niveauCode)
                    .orElseThrow(() -> new IllegalArgumentException("Niveau d'alerte introuvable : " + niveauCode));

            // NOUVELLE LOGIQUE : On cherche N'IMPORTE QUELLE alerte existante (active OU acquittée)
            // Pour cela, on utilise le repository pour trouver la toute dernière générée pour cette vache et ce type
            Optional<Alerte> alerteExistanteOpt;
            if (vacheId != null) {
                // On récupère toutes les alertes et on prend la plus récente
                alerteExistanteOpt = alerteRepo.findAll().stream()
                        .filter(a -> a.getVacheId() != null && a.getVacheId().equals(vacheId) && a.getType().getCode().equals(typeCode))
                        .max(Comparator.comparing(Alerte::getCreatedAt));
            } else {
                alerteExistanteOpt = alerteRepo.findAll().stream()
                        .filter(a -> a.getVacheId() == null && a.getType().getCode().equals(typeCode))
                        .max(Comparator.comparing(Alerte::getCreatedAt));
            }

            // Si une alerte existe déjà dans l'historique
            if (alerteExistanteOpt.isPresent()) {
                Alerte alerteExistante = alerteExistanteOpt.get();

                // CAS 1 : L'éleveur l'a déjà acquittée ! On refuse de la recréer.
                if (Boolean.TRUE.equals(alerteExistante.getAcquittee())) {
                    System.out.println("[Alertes] L'alerte pour " + typeCode + " a déjà été acquittée. On ne recrée rien.");
                    return; 
                }

                // CAS 2 : Elle est toujours active mais le niveau de gravité a changé (ex: attention -> urgent)
                if (!alerteExistante.getNiveau().getCode().equals(niveauCode)) {
                    alerteExistante.setNiveau(niveau);
                    alerteExistante.setTitre(titre);
                    alerteExistante.setDescription(description);
                    alerteRepo.save(alerteExistante);
                    System.out.println("[Alertes] Gravité mise à jour en [" + niveauCode + "] pour le type : " + typeCode);
                } else {
                    // Même type, active, et même niveau : doublon ignoré
                    System.out.println("[Alertes] Doublon ignoré (déjà en statut " + niveauCode + ") : " + typeCode);
                }
                return;
            }

            // Si vraiment aucune alerte n'a jamais été créée, on la crée pour la première fois
            creerAlerte(type.getId(), niveau.getId(), titre, description, vacheId);

        } catch (Exception e) {
            System.err.println("[Alertes] Alerte non envoyée : " + e.getMessage());
        }
    }

    public Optional<Alerte> obtenirAlerteActive(Long vacheId, String typeCode) {
        return alerteRepo.findByVacheIdAndType_CodeAndAcquitteeFalse(vacheId, typeCode);
    }
}