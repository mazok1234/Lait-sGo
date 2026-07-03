package com.example.demo.services;

import com.example.demo.dto.ReproductionDTO;
import com.example.demo.entity.Reproduction;
import com.example.demo.entity.Vache;
import com.example.demo.entity.VacheStatus;
import com.example.demo.repository.ReproductionRepository;
import com.example.demo.repository.VacheRepository;
import com.example.demo.repository.VacheStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReproductionService {

    @Autowired
    private ReproductionRepository reproductionRepository;

    @Autowired
    private VacheRepository vacheRepository;

    @Autowired
    private VacheStatusRepository vacheStatusRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Enregistrer une nouvelle insémination artificielle
     */
    @Transactional
    public ReproductionDTO enregistrerIA(ReproductionDTO dto) {
        // Récupérer la vache
        Vache vache = vacheRepository.findById(dto.getVacheId())
                .orElseThrow(() -> new RuntimeException("Vache non trouvée"));

        // Créer l'entité Reproduction
        Reproduction reproduction = new Reproduction();
        reproduction.setVache(vache);
        reproduction.setDateIA(dto.getDateIA());
        reproduction.setSemence(dto.getSemence());
        reproduction.setInséminateur(dto.getInséminateur());
        reproduction.setTypeInjection(dto.getTypeInjection());
        reproduction.setStatutIA("en_attente");

        // Sauvegarder
        Reproduction saved = reproductionRepository.save(reproduction);

        return convertToDTO(saved);
    }

    /**
     * Récupérer l'historique des IA d'une vache
     */
    public List<ReproductionDTO> getHistoriqueParVache(Long vacheId) {
        Vache vache = vacheRepository.findById(vacheId)
                .orElseThrow(() -> new RuntimeException("Vache non trouvée"));

        List<Reproduction> reproductions = reproductionRepository.findByVacheOrderByDateIADesc(vache);
        return reproductions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Mettre à jour le statut d'une IA
     */
    @Transactional
    public ReproductionDTO updateStatutIA(Long reproductionId, String statut, LocalDate dateConfirmation) {
        Reproduction reproduction = reproductionRepository.findById(reproductionId)
                .orElseThrow(() -> new RuntimeException("IA non trouvée"));

        reproduction.setStatutIA(statut);

        // Si statut "gestante", enregistrer la date de confirmation
        if ("gestante".equals(statut)) {
            reproduction.setGestationConfirmee(true);
            reproduction.setDateConfirmationGest(dateConfirmation != null ? dateConfirmation : LocalDate.now());
        }

        // Si statut "échouée", réinitialiser la gestation
        if ("echouee".equals(statut)) {
            reproduction.setGestationConfirmee(false);
            reproduction.setDateConfirmationGest(null);
        }

        Reproduction updated = reproductionRepository.save(reproduction);
        return convertToDTO(updated);
    }

    /**
     * Récupérer une IA par ID
     */
    public ReproductionDTO getReproductionById(Long id) {
        Reproduction reproduction = reproductionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("IA non trouvée"));
        return convertToDTO(reproduction);
    }

    /**
     * Récupérer toutes les IA en attente
     */
    public List<ReproductionDTO> getIAEnAttente() {
        List<Reproduction> reproductions = reproductionRepository.findByStatutIA("en_attente");
        return reproductions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Supprimer une IA (logique métier)
     */
    @Transactional
    public void deleteIA(Long reproductionId) {
        reproductionRepository.deleteById(reproductionId);
    }

    /**
     * Convertir Reproduction en DTO
     */
    private ReproductionDTO convertToDTO(Reproduction reproduction) {
        ReproductionDTO dto = new ReproductionDTO();
        dto.setId(reproduction.getId());
        dto.setVacheId(reproduction.getVache().getId());
        dto.setNumeroBoucleVache(reproduction.getVache().getNumeroBoucle());
        dto.setDateIA(reproduction.getDateIA());
        dto.setSemence(reproduction.getSemence());
        dto.setInséminateur(reproduction.getInséminateur());
        dto.setTypeInjection(reproduction.getTypeInjection());
        dto.setStatutIA(reproduction.getStatutIA());
        dto.setGestationConfirmee(reproduction.getGestationConfirmee());
        dto.setDateConfirmationGest(reproduction.getDateConfirmationGest());
        dto.setDateVelageReel(reproduction.getDateVelageReel());
        dto.setSexeVeau(reproduction.getSexeVeau());
        return dto;
    }


    public Map<String, Object> getDashboardCounters() {
        Map<String, Object> counters = new HashMap<>();

        String sqlAlertes = "SELECT COUNT(*) FROM alerte WHERE acquittee = FALSE";
        String sqlGestations = "SELECT COUNT(*) FROM reproduction WHERE gestation_confirmee = TRUE AND date_velage_reel IS NULL";
        String sqlLactations = "SELECT COUNT(*) FROM lactation WHERE id_statut = (SELECT id FROM ref_statut_lactation WHERE code = 'active')";

        counters.put("alertesActives", jdbcTemplate.queryForObject(sqlAlertes, Integer.class));
        counters.put("gestationsEnCours", jdbcTemplate.queryForObject(sqlGestations, Integer.class));
        counters.put("vachesEnLactation", jdbcTemplate.queryForObject(sqlLactations, Integer.class));

        return counters;
    }

    public List<Map<String, Object>> getSuiviSemaine(LocalDate startOfWeek) {
        return getSuiviSemaine(startOfWeek, null);
    }

    public LocalDate getDateEvenementPertinent(LocalDate referenceDate) {
        String sql = "SELECT date_evenement " +
                "FROM ( " +
                "SELECT r.date_ia AS date_evenement " +
                "FROM reproduction r " +
                "WHERE r.date_velage_reel IS NULL " +
                "UNION ALL " +
                "SELECT (r.date_ia + INTERVAL '21 days')::date AS date_evenement " +
                "FROM reproduction r " +
                "WHERE r.date_velage_reel IS NULL " +
                "AND (r.gestation_confirmee IS NULL OR r.gestation_confirmee = FALSE) " +
                "UNION ALL " +
                "SELECT (s.date_debut + INTERVAL '21 days')::date AS date_evenement " +
                "FROM vache_statut s " +
                "JOIN ref_statut_vache st ON st.id = s.statut_id " +
                "WHERE s.date_debut = (SELECT MAX(ss.date_debut) FROM vache_statut ss WHERE ss.vache_id = s.vache_id) " +
                "AND lower(st.code) IN ('chaleur', 'velage') " +
                ") evenements " +
                "ORDER BY " +
                "CASE WHEN date_evenement >= ? THEN 0 ELSE 1 END, " +
                "CASE WHEN date_evenement >= ? THEN date_evenement END ASC, " +
                "date_evenement DESC " +
                "LIMIT 1";

        List<LocalDate> dates = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getDate("date_evenement").toLocalDate(),
                java.sql.Date.valueOf(referenceDate),
                java.sql.Date.valueOf(referenceDate)
        );

        return dates.isEmpty() ? null : dates.get(0);
    }

    public List<Map<String, Object>> getSuiviSemaine(LocalDate startOfWeek, Long vacheId) {
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        String sql = "SELECT v.id AS vache_id, v.numero_boucle, r.date_ia AS date_evenement, " +
                "'Insemination Artificielle' AS type_evenement " +
                "FROM reproduction r " +
                "JOIN vache v ON v.id = r.vache_id " +
                "WHERE r.date_velage_reel IS NULL " +
                "AND r.date_ia BETWEEN ? AND ? ";

        List<Object> params = new ArrayList<>();
        params.add(java.sql.Date.valueOf(startOfWeek));
        params.add(java.sql.Date.valueOf(endOfWeek));

        if (vacheId != null) {
            sql += "AND v.id = ? ";
            params.add(vacheId);
        }

        sql += "UNION ALL " +
                "SELECT v.id AS vache_id, v.numero_boucle, (r.date_ia + INTERVAL '21 days')::date AS date_evenement, " +
                "'Vigilance Retour Chaleurs (J+21)' AS type_evenement " +
                "FROM reproduction r " +
                "JOIN vache v ON v.id = r.vache_id " +
                "WHERE r.date_velage_reel IS NULL " +
                "AND (r.gestation_confirmee IS NULL OR r.gestation_confirmee = FALSE) " +
                "AND (r.date_ia + INTERVAL '21 days')::date BETWEEN ? AND ? ";

        params.add(java.sql.Date.valueOf(startOfWeek));
        params.add(java.sql.Date.valueOf(endOfWeek));

        if (vacheId != null) {
            sql += "AND v.id = ? ";
            params.add(vacheId);
        }

        sql += "UNION ALL " +
                "SELECT v.id AS vache_id, v.numero_boucle, (s.date_debut + INTERVAL '21 days')::date AS date_evenement, " +
                "'Vigilance Retour Chaleurs (J+21)' AS type_evenement " +
                "FROM vache_statut s " +
                "JOIN vache v ON v.id = s.vache_id " +
                "JOIN ref_statut_vache st ON st.id = s.statut_id " +
                "WHERE s.date_debut = (SELECT MAX(ss.date_debut) FROM vache_statut ss WHERE ss.vache_id = s.vache_id) " +
                "AND lower(st.code) IN ('chaleur', 'velage') " +
                "AND (s.date_debut + INTERVAL '21 days')::date BETWEEN ? AND ? ";

        params.add(java.sql.Date.valueOf(startOfWeek));
        params.add(java.sql.Date.valueOf(endOfWeek));

        if (vacheId != null) {
            sql += "AND v.id = ? ";
            params.add(vacheId);
        }

        sql += "ORDER BY date_evenement ASC";

        return jdbcTemplate.query(sql, suiviRowMapper(), params.toArray());
    }

    public LocalDate calculerProchaineChaleur(Long vacheId) {
        List<VacheStatus> statusHistorique = vacheStatusRepository.findByVacheIdOrderByDateDebutDesc(vacheId);
        for (VacheStatus status : statusHistorique) {
            if (status.getStatut() == null || status.getStatut().getCode() == null) {
                continue;
            }
            String code = status.getStatut().getCode().trim().toLowerCase(Locale.ROOT);
            if ("velage".equals(code) || "chaleur".equals(code)) {
                return status.getDateDebut().plusDays(21);
            }
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getChaleurs(Integer mois, Integer annee, Long vacheId) {
        LocalDate now = LocalDate.now();
        boolean filtreActif = mois != null || annee != null;
        int selectedMonth = mois != null ? mois : now.getMonthValue();
        int selectedYear = annee != null ? annee : now.getYear();

        List<VacheStatus> statusHistorique = vacheStatusRepository.findAll();
        Map<Long, VacheStatus> derniersStatuts = statusHistorique.stream()
                .filter(this::isChaleurOuVelage)
                .filter(status -> vacheId == null || status.getVache().getId().equals(vacheId))
                .collect(Collectors.toMap(
                        status -> status.getVache().getId(),
                        status -> status,
                        (first, second) -> first.getDateDebut().isAfter(second.getDateDebut()) ? first : second
                ));

        List<Map<String, Object>> result = new ArrayList<>();
        for (VacheStatus status : derniersStatuts.values()) {
            LocalDate dateChaleur;
            String origine;

            if (filtreActif) {
                List<LocalDate> dates = getDateChaleur(selectedMonth, selectedYear, List.of(status));
                dateChaleur = dates.stream().findFirst().orElse(null);
                origine = "Chaleur filtrée";
            } else {
                dateChaleur = getProchaineChaleur(status.getDateDebut());
                origine = "Prochaine chaleur";
            }

            if (dateChaleur != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("vacheId", status.getVache().getId());
                item.put("numeroBoucle", status.getVache().getNumeroBoucle());
                item.put("dateChaleur", dateChaleur);
                item.put("origine", origine);
                item.put("statut", status.getStatut().getCode());
                result.add(item);
            }
        }

        result.sort(Comparator.comparing(item -> (LocalDate) item.get("dateChaleur")));
        return result;
    }

    private boolean isChaleurOuVelage(VacheStatus status) {
        if (status == null || status.getStatut() == null || status.getStatut().getCode() == null) {
            return false;
        }
        String code = status.getStatut().getCode().trim().toLowerCase(Locale.ROOT);
        return "chaleur".equals(code) || "velage".equals(code);
    }

    private LocalDate getProchaineChaleur(LocalDate dateDebut) {
        if (dateDebut == null) {
            return null;
        }
        LocalDate dateSuivante = dateDebut.plusDays(21);
        LocalDate aujourdHui = LocalDate.now();
        while (dateSuivante.isBefore(aujourdHui)) {
            dateSuivante = dateSuivante.plusDays(21);
        }
        return dateSuivante;
    }

    public List<Map<String, Object>> getVachesPourFiltre() {
        String sql = "SELECT v.id, v.numero_boucle, s.libelle AS statut " +
                "FROM vache v " +
                "JOIN ref_statut_vache s ON s.id = v.id_statut " +
                "ORDER BY v.numero_boucle ASC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> vache = new HashMap<>();
            vache.put("id", rs.getLong("id"));
            vache.put("numeroBoucle", rs.getString("numero_boucle"));
            vache.put("statut", rs.getString("statut"));
            return vache;
        });
    }

    public List<Reproduction> findReproductionsActivesEtConfirmees() {
        return reproductionRepository.findByGestationConfirmeeTrueAndDateVelageReelIsNull();
    }

    public List<Reproduction> findByGestationConfirmeeTrueAndDateVelageReelIsNull() {
        return reproductionRepository.findByGestationConfirmeeTrueAndDateVelageReelIsNull();
    }

    public List<Map<String, Object>> getReproductionsGestantesPourVelage() {
        String sql = "SELECT r.id, v.numero_boucle, r.date_ia " +
                "FROM reproduction r " +
                "JOIN vache v ON v.id = r.vache_id " +
                "WHERE r.gestation_confirmee = TRUE " +
                "AND r.date_velage_reel IS NULL " +
                "ORDER BY r.date_ia ASC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> reproduction = new HashMap<>();
            reproduction.put("id", rs.getLong("id"));
            reproduction.put("numeroBoucle", rs.getString("numero_boucle"));
            reproduction.put("dateIa", rs.getDate("date_ia").toLocalDate());
            return reproduction;
        });
    }

    public void enregistrerVelage(Long reproductionId, LocalDate dateVelageReel, String sexeVeau) {
        Reproduction reproduction = reproductionRepository.findById(reproductionId)
                .orElseThrow(() -> new IllegalArgumentException("Reproduction introuvable"));

        reproduction.setDateVelageReel(dateVelageReel);
        reproduction.setSexeVeau(sexeVeau);
        reproductionRepository.save(reproduction);
    }

    private RowMapper<Map<String, Object>> suiviRowMapper() {
        return (rs, rowNum) -> {
            Map<String, Object> evenement = new HashMap<>();
            evenement.put("vacheId", rs.getLong("vache_id"));
            evenement.put("numeroBoucle", rs.getString("numero_boucle"));
            evenement.put("dateEvenement", rs.getDate("date_evenement").toLocalDate());
            evenement.put("typeEvenement", rs.getString("type_evenement"));
            return evenement;
        };
    }

    public List<LocalDate> getDateChaleur(int mois , int Annee , List<VacheStatus> vacheStatusList) {
        List<LocalDate> datesChaleur = new ArrayList<>();
        if (vacheStatusList == null || vacheStatusList.isEmpty()) {
            return datesChaleur;
        }

        LocalDate startOfTargetMonth = LocalDate.of(Annee, mois, 1);
        LocalDate endOfTargetMonth = startOfTargetMonth.withDayOfMonth(startOfTargetMonth.lengthOfMonth());

        for (VacheStatus vacheStatus : vacheStatusList) {
            LocalDate dateDebut = vacheStatus.getDateDebut();
            if (dateDebut == null || dateDebut.isAfter(endOfTargetMonth)) {
                continue;
            }

            while (dateDebut.isBefore(startOfTargetMonth)) {
                dateDebut = dateDebut.plusDays(21);
            }

            if (!dateDebut.isAfter(endOfTargetMonth)
                    && dateDebut.getMonthValue() == mois
                    && dateDebut.getYear() == Annee) {
                datesChaleur.add(dateDebut);
            }
        }

        return datesChaleur;
    }
}
