package com.example.demo.service;

import com.example.demo.entity.Reproduction;
import com.example.demo.repository.ReproductionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReproductionService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReproductionRepository reproductionRepository;

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

        sql += "ORDER BY date_evenement ASC";

        return jdbcTemplate.query(sql, suiviRowMapper(), params.toArray());
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
}
