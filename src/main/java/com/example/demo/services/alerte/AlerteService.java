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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class AlerteService {

    private final AlerteRepository alerteRepo;
    private final RefNiveauAlerteRepository niveauRepo;
    private final RefTypeAlerteRepository typeRepo;
    private final VacheRepository vacheRepo;

    // Mapping type → module (logique métier — pas en BDD)
    public static final String STOCK_ALIMENT_BAS_PREFIX = "stock_aliment_bas";
    private static final Map<String, String> MODULE_PAR_TYPE = new HashMap<>();
    static {
        MODULE_PAR_TYPE.put("vaccin_en_retard", "Santé — Vaccination");
        MODULE_PAR_TYPE.put("rappel_vaccin", "Santé — Vaccination");
        MODULE_PAR_TYPE.put("vaccin_prioritaire", "Santé — Vaccination");
        MODULE_PAR_TYPE.put("traitement_en_cours", "Santé — Traitement");
        MODULE_PAR_TYPE.put("rappel_velage", "Reproduction");
        MODULE_PAR_TYPE.put("stock_lait_bas", "Vente");
        MODULE_PAR_TYPE.put(STOCK_ALIMENT_BAS_PREFIX, "Alimentation");
        MODULE_PAR_TYPE.put("bcs_hors_plage", "Cheptel");
        MODULE_PAR_TYPE.put("baisse_production", "Production");
    }

    public AlerteService(AlerteRepository alerteRepo,
            RefNiveauAlerteRepository niveauRepo,
            RefTypeAlerteRepository typeRepo,
            VacheRepository vacheRepo) {
        this.alerteRepo = alerteRepo;
        this.niveauRepo = niveauRepo;
        this.typeRepo = typeRepo;
        this.vacheRepo = vacheRepo;
    }

    // ----------------------------------------------------------------
    // FA-01 : Créer une alerte
    // ----------------------------------------------------------------
    public Alerte creerAlerte(String typeAlerte, Integer idNiveau,
            String titre, String description, Long vacheId) {
        RefNiveauAlerte niveau = niveauRepo.findById(idNiveau)
                .orElseThrow(() -> new IllegalArgumentException("Niveau introuvable : " + idNiveau));
        RefTypeAlerte type = resolveOrCreateType(typeAlerte);
        Alerte alerte = new Alerte();
        alerte.setNiveau(niveau);
        alerte.setType(type);
        alerte.setTitre(titre);
        alerte.setDescription(description);
        alerte.setVacheId(vacheId);
        return alerteRepo.save(alerte);
    }

    // Les codes "catalogue" (8 valeurs fixes) sont pré-remplis en base ; les codes
    // dynamiques par instance (ex: stock_aliment_bas_<alimentId>) sont provisionnés
    // à la volée pour respecter la contrainte FK id_type -> ref_type_alerte.
    private RefTypeAlerte resolveOrCreateType(String code) {
        return typeRepo.findByCode(code).orElseGet(() -> {
            RefTypeAlerte t = new RefTypeAlerte();
            t.setCode(code);
            t.setLibelle(code);
            return typeRepo.save(t);
        });
    }

    // ----------------------------------------------------------------
    // FA-02 : Dashboard — non acquittées (triées) + acquittées en bas
    // ----------------------------------------------------------------
    public List<AlerteDTO> listerToutesAlertes(String niveauCode, String typeAlerte) {

        // 1. Alertes non acquittées avec filtres
        List<Alerte> nonAcquittees;
        List<String> filtreTypes = getTypeFilterKeys(typeAlerte);
        boolean filtreParModule = typeAlerte != null && filtreTypes.size() > 0;

        if (niveauCode != null && typeAlerte != null) {
            if (filtreTypes.size() == 1 && STOCK_ALIMENT_BAS_PREFIX.equals(filtreTypes.get(0))) {
                nonAcquittees = alerteRepo
                        .findByAcquitteeFalseAndNiveau_CodeAndType_CodeStartingWithOrderByCreatedAtDesc(
                                niveauCode, STOCK_ALIMENT_BAS_PREFIX);
            } else if (filtreTypes.size() == 1) {
                nonAcquittees = alerteRepo
                        .findByAcquitteeFalseAndNiveau_CodeAndType_CodeOrderByCreatedAtDesc(
                                niveauCode, filtreTypes.get(0));
            } else {
                nonAcquittees = alerteRepo
                        .findByAcquitteeFalseAndNiveau_CodeOrderByCreatedAtDesc(niveauCode)
                        .stream()
                        .filter(a -> matchesAnyType(a.getType().getCode(), filtreTypes))
                        .collect(Collectors.toList());
            }
        } else if (niveauCode != null) {
            nonAcquittees = alerteRepo
                    .findByAcquitteeFalseAndNiveau_CodeOrderByCreatedAtDesc(niveauCode);
        } else if (typeAlerte != null) {
            if (filtreTypes.size() == 1 && STOCK_ALIMENT_BAS_PREFIX.equals(filtreTypes.get(0))) {
                nonAcquittees = alerteRepo
                        .findByAcquitteeFalseAndType_CodeStartingWithOrderByCreatedAtDesc(
                                STOCK_ALIMENT_BAS_PREFIX);
            } else if (filtreTypes.size() == 1) {
                nonAcquittees = alerteRepo
                        .findByAcquitteeFalseAndType_CodeOrderByCreatedAtDesc(filtreTypes.get(0));
            } else {
                nonAcquittees = alerteRepo.findByAcquitteeFalseOrderByCreatedAtDesc()
                        .stream()
                        .filter(a -> matchesAnyType(a.getType().getCode(), filtreTypes))
                        .collect(Collectors.toList());
            }
        } else {
            nonAcquittees = alerteRepo.findByAcquitteeFalseOrderByCreatedAtDesc();
        }

        // Trier par gravité (ordre BDD) puis par date
        nonAcquittees.sort(Comparator
                .comparingInt((Alerte a) -> a.getNiveau().getOrdre())
                .thenComparing(Comparator.comparing(Alerte::getCreatedAt).reversed()));

        // 2. Alertes acquittées — appliquer le filtre module si activé, mais jamais filtrer par niveau
        List<Alerte> acquittees = alerteRepo.findByAcquitteeTrueOrderByCreatedAtDesc();
        if (filtreParModule) {
            acquittees = acquittees.stream()
                    .filter(a -> matchesAnyType(a.getType().getCode(), filtreTypes))
                    .collect(Collectors.toList());
        }

        // 3. Fusionner : non acquittées en premier, acquittées en bas
        return Stream.concat(nonAcquittees.stream(), acquittees.stream())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    // FA-03 : Acquittement manuel (met à true, ne supprime pas)
    // ----------------------------------------------------------------
    public void acquitter(Long id) {
        Alerte alerte = alerteRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Alerte introuvable : " + id));
        if (alerte.getAcquittee()) {
            throw new IllegalStateException("Alerte déjà acquittée");
        }
        alerte.setAcquittee(true);
        alerteRepo.save(alerte);
    }

    // ----------------------------------------------------------------
    // FA-05 : Détail d'une alerte
    // ----------------------------------------------------------------
    public AlerteDTO getDetail(Long id) {
        Alerte alerte = alerteRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Alerte introuvable : " + id));
        return toDTO(alerte);
    }

    // ----------------------------------------------------------------
    // KPIs — compter les non acquittées par niveau
    // ----------------------------------------------------------------
    public Map<String, Long> compterParNiveau() {
        return alerteRepo.findByAcquitteeFalseOrderByCreatedAtDesc()
                .stream()
                .collect(Collectors.groupingBy(
                        a -> a.getNiveau().getCode(),
                        Collectors.counting()));
    }

    // ----------------------------------------------------------------
    // Badge sidebar
    // ----------------------------------------------------------------
    public long compterNonAcquittees() {
        return alerteRepo.findByAcquitteeFalseOrderByCreatedAtDesc().size();
    }

    // ----------------------------------------------------------------
    // Filtres types disponibles (depuis le mapping, pas la BDD)
    // ----------------------------------------------------------------
    public List<Map<String, String>> getTypesDisponibles() {
        // Dédupliquer par module (une option par module dans la liste déroulante)
        Map<String, String> moduleVersCode = new LinkedHashMap<>();
        MODULE_PAR_TYPE.forEach((code, module) -> {
            moduleVersCode.putIfAbsent(module, code);
        });

        return moduleVersCode.entrySet().stream()
                .map(e -> {
                    Map<String, String> m = new LinkedHashMap<>();
                    m.put("code", e.getValue());
                    m.put("module", e.getKey());
                    return m;
                })
                .collect(Collectors.toList());
    }

    private List<String> getTypeFilterKeys(String selectedType) {
        if (selectedType == null || selectedType.isBlank()) {
            return Collections.emptyList();
        }

        if (selectedType.startsWith(STOCK_ALIMENT_BAS_PREFIX)) {
            return Collections.singletonList(STOCK_ALIMENT_BAS_PREFIX);
        }

        String module = MODULE_PAR_TYPE.get(selectedType);
        if (module != null) {
            return MODULE_PAR_TYPE.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(module))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        }

        return Collections.singletonList(selectedType);
    }

    private boolean matchesAnyType(String typeAlerte, List<String> filtreTypes) {
        if (filtreTypes == null || filtreTypes.isEmpty()) {
            return true;
        }
        return filtreTypes.stream().anyMatch(filter -> {
            if (STOCK_ALIMENT_BAS_PREFIX.equals(filter)) {
                return typeAlerte != null && typeAlerte.startsWith(STOCK_ALIMENT_BAS_PREFIX);
            }
            return filter.equals(typeAlerte);
        });
    }

    // ----------------------------------------------------------------
    // envoyerAlerte — appelé par les autres modules
    // ----------------------------------------------------------------
    public void envoyerAlerte(String typeAlerte, String niveauCode,
            String titre, String description, Long vacheId) {
        try {
            RefNiveauAlerte niveau = niveauRepo.findByCode(niveauCode)
                    .orElseThrow(() -> new IllegalArgumentException("Niveau inconnu : " + niveauCode));

            // Chercher une alerte active existante, sinon réactiver la dernière acquittée
            boolean alimPrefix = typeAlerte != null && typeAlerte.startsWith(STOCK_ALIMENT_BAS_PREFIX);
            Optional<Alerte> alerteExistante;
            if (alimPrefix) {
            alerteExistante = vacheId != null
                ? alerteRepo.findTopByVacheIdAndType_CodeStartingWithAndAcquitteeFalseOrderByCreatedAtDesc(vacheId, typeAlerte)
                : alerteRepo.findTopByVacheIdIsNullAndType_CodeStartingWithAndAcquitteeFalseOrderByCreatedAtDesc(typeAlerte);
            } else {
            alerteExistante = vacheId != null
                ? alerteRepo.findTopByVacheIdAndType_CodeAndAcquitteeFalseOrderByCreatedAtDesc(vacheId, typeAlerte)
                : alerteRepo.findTopByVacheIdIsNullAndType_CodeAndAcquitteeFalseOrderByCreatedAtDesc(typeAlerte);
            }

            if (alerteExistante.isPresent()) {
                Alerte existante = alerteExistante.get();
                // Mettre à jour si le niveau ou la description a changé
                if (!existante.getNiveau().getCode().equals(niveauCode)
                        || !existante.getDescription().equals(description)) {
                    existante.setNiveau(niveau);
                    existante.setTitre(titre);
                    existante.setDescription(description);
                    alerteRepo.save(existante);
                    System.out.println("[Alertes] Mise à jour : " + typeAlerte + " → " + niveauCode);
                } else {
                    System.out.println("[Alertes] Doublon ignoré : " + typeAlerte);
                }
                return;
            }

            Optional<Alerte> alerteAcquitteeRecente = alimPrefix
                    ? (vacheId != null
                            ? alerteRepo.findTopByVacheIdAndType_CodeStartingWithAndAcquitteeTrueOrderByCreatedAtDesc(vacheId, typeAlerte)
                            : alerteRepo.findTopByVacheIdIsNullAndType_CodeStartingWithAndAcquitteeTrueOrderByCreatedAtDesc(typeAlerte))
                    : (vacheId != null
                            ? alerteRepo.findTopByVacheIdAndType_CodeAndAcquitteeTrueOrderByCreatedAtDesc(vacheId, typeAlerte)
                        : alerteRepo.findTopByVacheIdIsNullAndType_CodeAndAcquitteeTrueOrderByCreatedAtDesc(typeAlerte));

            if (alerteAcquitteeRecente.isPresent()) {
                Alerte ancienne = alerteAcquitteeRecente.get();
                ancienne.setNiveau(niveau);
                ancienne.setTitre(titre);
                ancienne.setDescription(description);
                ancienne.setAcquittee(false);
                ancienne.setCreatedAt(LocalDateTime.now());
                alerteRepo.save(ancienne);
                System.out.println("[Alertes] Réactivée : " + typeAlerte + " [" + niveauCode + "]");
                return;
            }

            // Créer une nouvelle alerte
            creerAlerte(typeAlerte, niveau.getId(), titre, description, vacheId);
            System.out.println("[Alertes] Créée : " + typeAlerte + " [" + niveauCode + "]");

        } catch (Exception e) {
            System.err.println("[Alertes] Alerte non envoyée : " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // acquitterAutomatiquement — appelé par les autres modules
    // ----------------------------------------------------------------
    public void acquitterAutomatiquement(String typeAlerte, Long vacheId) {
        try {
            boolean alimPrefix = typeAlerte != null && typeAlerte.startsWith(STOCK_ALIMENT_BAS_PREFIX);
            List<Alerte> alertes = vacheId != null
                    ? (alimPrefix
                        ? alerteRepo.findByType_CodeStartingWithAndVacheIdAndAcquitteeFalse(typeAlerte, vacheId)
                        : alerteRepo.findByType_CodeAndVacheIdAndAcquitteeFalse(typeAlerte, vacheId))
                    : (alimPrefix
                        ? alerteRepo.findByType_CodeStartingWithAndAcquitteeFalse(typeAlerte)
                        : alerteRepo.findByType_CodeAndAcquitteeFalse(typeAlerte));

            alertes.forEach(a -> {
                a.setAcquittee(true);
                alerteRepo.save(a);
                System.out.println("[Alertes] Acquittement auto : " + typeAlerte
                        + (vacheId != null ? " — vache " + vacheId : " — ferme"));
            });
        } catch (Exception e) {
            System.err.println("[Alertes] Acquittement auto échoué : " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // detacherVache — si une vache est supprimée
    // ----------------------------------------------------------------
    public void detacherVache(Long vacheId) {
        List<Alerte> alertes = alerteRepo.findByVacheId(vacheId);
        alertes.forEach(a -> a.setVacheId(null));
        alerteRepo.saveAll(alertes);
    }

    // ----------------------------------------------------------------
    // toDTO — conversion interne
    // ----------------------------------------------------------------
    private AlerteDTO toDTO(Alerte a) {
        AlerteDTO dto = new AlerteDTO();
        dto.setId(a.getId());
        dto.setTitre(a.getTitre());
        dto.setDescription(a.getDescription());
        dto.setNiveauCode(a.getNiveau().getCode());
        dto.setNiveauLibelle(a.getNiveau().getLibelle());
        String code = a.getType().getCode();
        dto.setTypeAlerte(code);
        dto.setModuleSource(
                code.startsWith(STOCK_ALIMENT_BAS_PREFIX)
                        ? "Alimentation"
                        : MODULE_PAR_TYPE.getOrDefault(code, "Système"));
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
}
