package com.example.demo.services.alerte;

import com.example.demo.dto.AlerteDTO;
import com.example.demo.entity.alerte.Alerte;
import com.example.demo.entity.alerte.RefNiveauAlerte;
import com.example.demo.entity.alerte.RefTypeAlerte;
import com.example.demo.repository.alerte.AlerteRepository;
import com.example.demo.repository.alerte.RefNiveauAlerteRepository;
import com.example.demo.repository.alerte.RefTypeAlerteRepository;
import com.example.demo.repository.cheptel.VacheRepository;
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

    public void acquitter(Long id) {
        Alerte alerte = alerteRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Alerte introuvable : " + id));
        if (alerte.getAcquittee()) {
            throw new IllegalStateException("Alerte déjà acquittée");
        }
        alerte.setAcquittee(true);
        alerteRepo.save(alerte);
    }

    public AlerteDTO getDetail(Long id) {
        Alerte alerte = alerteRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Alerte introuvable : " + id));
        return toDTO(alerte);
    }

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

    public void envoyerAlerte(String typeCode, String niveauCode, String titre, String description, Long vacheId) {
        try {
            RefTypeAlerte type = typeRepo.findByCode(typeCode)
                    .orElseThrow(() -> new IllegalArgumentException("Type d'alerte introuvable : " + typeCode));
            RefNiveauAlerte niveau = niveauRepo.findByCode(niveauCode)
                    .orElseThrow(() -> new IllegalArgumentException("Niveau d'alerte introuvable : " + niveauCode));

            Optional<Alerte> alerteExistanteOpt;
            if (vacheId != null) {
                alerteExistanteOpt = alerteRepo.findAll().stream()
                        .filter(a -> a.getVacheId() != null && a.getVacheId().equals(vacheId) && a.getType().getCode().equals(typeCode))
                        .max(Comparator.comparing(Alerte::getCreatedAt));
            } else {
                alerteExistanteOpt = alerteRepo.findAll().stream()
                        .filter(a -> a.getVacheId() == null && a.getType().getCode().equals(typeCode))
                        .max(Comparator.comparing(Alerte::getCreatedAt));
            }

            if (alerteExistanteOpt.isPresent()) {
                Alerte alerteExistante = alerteExistanteOpt.get();

                if (Boolean.TRUE.equals(alerteExistante.getAcquittee())) {
                    System.out.println("[Alertes] L'alerte pour " + typeCode + " a déjà été acquittée. On ne recrée rien.");
                    return;
                }

                if (!alerteExistante.getNiveau().getCode().equals(niveauCode)) {
                    alerteExistante.setNiveau(niveau);
                    alerteExistante.setTitre(titre);
                    alerteExistante.setDescription(description);
                    alerteRepo.save(alerteExistante);
                    System.out.println("[Alertes] Gravité mise à jour en [" + niveauCode + "] pour le type : " + typeCode);
                } else {
                    System.out.println("[Alertes] Doublon ignoré (déjà en statut " + niveauCode + ") : " + typeCode);
                }
                return;
            }

            creerAlerte(type.getId(), niveau.getId(), titre, description, vacheId);
        } catch (Exception e) {
            System.err.println("[Alertes] Alerte non envoyée : " + e.getMessage());
        }
    }

    public Optional<Alerte> obtenirAlerteActive(Long vacheId, String typeCode) {
        return alerteRepo.findByVacheIdAndType_CodeAndAcquitteeFalse(vacheId, typeCode);
    }

    public void detacherVache(Long vacheId) {
        List<Alerte> alertes = alerteRepo.findByVacheId(vacheId);
        alertes.forEach(a -> a.setVacheId(null));
        alerteRepo.saveAll(alertes);
    }
}
