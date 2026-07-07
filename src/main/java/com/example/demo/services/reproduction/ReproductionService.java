package com.example.demo.services.reproduction;

import com.example.demo.services.cheptel.StatutSanteService;

import com.example.demo.services.cheptel.StatutLactationVacheService;

import com.example.demo.services.cheptel.StatutReproService;

import com.example.demo.services.cheptel.StatutVieService;

import com.example.demo.services.alerte.AlerteService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ReproductionDTO;
import com.example.demo.entity.reproduction.Reproduction;
import com.example.demo.entity.cheptel.Vache;
import com.example.demo.entity.cheptel.VacheHistoriqueRepro;
import com.example.demo.repository.reproduction.ReproductionRepository;
import com.example.demo.repository.cheptel.VacheRepository;

@Service
public class ReproductionService {
    @Autowired
    private ReproductionRepository reproductionRepository;

    @Autowired
    private VacheRepository vacheRepository;

    @Autowired
    private StatutVieService statutVieService;

    @Autowired
    private StatutReproService statutReproService;

    @Autowired
    private StatutLactationVacheService statutLactationService;

    @Autowired
    private StatutSanteService statutSanteService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AlerteService alerteService;

    @Transactional
    public ReproductionDTO enregistrerIA(ReproductionDTO dto) {
        Vache vache = vacheRepository.findById(dto.getVacheId())
                .orElseThrow(() -> new RuntimeException("Vache non trouvée"));

        Reproduction reproduction = new Reproduction();
        reproduction.setVache(vache);
        reproduction.setDateIA(dto.getDateIA());
        reproduction.setSemence(dto.getSemence());
        reproduction.setInséminateur(dto.getInséminateur());
        reproduction.setTypeInjection(dto.getTypeInjection());
        reproduction.setStatutIA("en_attente");

        Reproduction saved = reproductionRepository.save(reproduction);

        statutReproService.changerStatut(vache, statutReproService.getByLibelle("Inseminee").getId(), dto.getDateIA());

        return convertToDTO(saved);
    }

    public List<ReproductionDTO> getHistoriqueParVache(Long vacheId) {
        Vache vache = vacheRepository.findById(vacheId)
                .orElseThrow(() -> new RuntimeException("Vache non trouvée"));

        List<Reproduction> reproductions = reproductionRepository.findByVacheOrderByDateIADesc(vache);
        return reproductions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReproductionDTO updateStatutIA(Long reproductionId, String statut, LocalDate dateConfirmation) {
        Reproduction reproduction = reproductionRepository.findById(reproductionId)
                .orElseThrow(() -> new RuntimeException("IA non trouvée"));

        reproduction.setStatutIA(statut);
        LocalDate dateEffet = dateConfirmation != null ? dateConfirmation : LocalDate.now();

        if ("gestante".equals(statut)) {
            reproduction.setGestationConfirmee(true);
            reproduction.setDateConfirmationGest(dateEffet);
            statutReproService.changerStatut(reproduction.getVache(), statutReproService.getByLibelle("Gestante").getId(), dateEffet);
        }

        if ("echouee".equals(statut)) {
            reproduction.setGestationConfirmee(false);
            reproduction.setDateConfirmationGest(null);
            statutReproService.changerStatut(reproduction.getVache(), statutReproService.getByLibelle("Vide").getId(), dateEffet);
        }

        Reproduction updated = reproductionRepository.save(reproduction);
        return convertToDTO(updated);
    }

    public ReproductionDTO getReproductionById(Long id) {
        Reproduction reproduction = reproductionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("IA non trouvée"));
        return convertToDTO(reproduction);
    }

    public Vache getVacheDeReproduction(Long reproductionId) {
        return reproductionRepository.findById(reproductionId)
                .orElseThrow(() -> new IllegalArgumentException("Reproduction introuvable"))
                .getVache();
    }

    public List<ReproductionDTO> getIAEnAttente() {
        List<Reproduction> reproductions = reproductionRepository.findByStatutIA("en_attente");
        return reproductions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteIA(Long reproductionId) {
        reproductionRepository.deleteById(reproductionId);
    }

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

        String sqlAlertes = "SELECT COUNT(*) FROM alerte WHERE acquittee = FALSE and type_alerte = 'rappel_velage'";
        String sqlGestations = "SELECT COUNT(*) FROM reproduction WHERE gestation_confirmee = TRUE AND date_velage_reel IS NULL";

        counters.put("alertesActives", jdbcTemplate.queryForObject(sqlAlertes, Integer.class));
        counters.put("gestationsEnCours", jdbcTemplate.queryForObject(sqlGestations, Integer.class));

        counters.put("vachesEnLactation",
                statutLactationService.findVacheIdsByStatut(statutLactationService.getByLibelle("En_lactation").getId()).size());

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
                "SELECT (h.date_debut + INTERVAL '21 days')::date AS date_evenement " +
                "FROM vache_historique_repro h " +
                "JOIN ref_statut_repro st ON st.id = h.statut_id " +
                "WHERE h.date_fin IS NULL AND st.libelle = 'Vide' " +
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
                "SELECT v.id AS vache_id, v.numero_boucle, (h.date_debut + INTERVAL '21 days')::date AS date_evenement, " +
                "'Vigilance Retour Chaleurs (J+21)' AS type_evenement " +
                "FROM vache_historique_repro h " +
                "JOIN vache v ON v.id = h.vache_id " +
                "JOIN ref_statut_repro st ON st.id = h.statut_id " +
                "WHERE h.date_fin IS NULL AND st.libelle = 'Vide' " +
                "AND (h.date_debut + INTERVAL '21 days')::date BETWEEN ? AND ? ";

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
        Optional<VacheHistoriqueRepro> videActuel = statutReproService.getHistoriqueActuel(vacheId)
                .filter(h -> "Vide".equals(h.getStatut().getLibelle()));
        return videActuel.map(h -> getProchaineDateCycle(h.getDateDebut())).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getChaleurs(Integer mois, Integer annee, Long vacheId) {
        LocalDate now = LocalDate.now();
        boolean filtreActif = mois != null || annee != null;
        int selectedMonth = mois != null ? mois : now.getMonthValue();
        int selectedYear = annee != null ? annee : now.getYear();

        List<Map<String, Object>> result = new ArrayList<>();

        List<VacheHistoriqueRepro> videActuelles = getVidesActuelles(vacheId);
        for (VacheHistoriqueRepro h : videActuelles) {
            LocalDate dateChaleur;
            if (filtreActif) {
                dateChaleur = getDatesCycleDansLeMois(h.getDateDebut(), selectedMonth, selectedYear).stream().findFirst().orElse(null);
            } else {
                dateChaleur = getProchaineDateCycle(h.getDateDebut());
            }

            if (dateChaleur != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("vacheId", h.getVache().getId());
                item.put("numeroBoucle", h.getVache().getNumeroBoucle());
                item.put("dateChaleur", dateChaleur);
                item.put("statut", "Vide");
                long joursRestants = ChronoUnit.DAYS.between(now, dateChaleur);
                item.put("joursRestants", joursRestants < 0 ? 0 : joursRestants);
                result.add(item);
            }
        }

        List<VacheHistoriqueRepro> enChaleurActuelles = statutReproService.getHistoriqueOuvertParStatut(
                statutReproService.getByLibelle("En_chaleur").getId());
        for (VacheHistoriqueRepro h : enChaleurActuelles) {
            if (vacheId != null && !h.getVache().getId().equals(vacheId)) {
                continue;
            }

            LocalDate dateChaleur = h.getDateDebut();

            if (filtreActif) {
                if (dateChaleur.getMonthValue() != selectedMonth || dateChaleur.getYear() != selectedYear) {
                    continue;
                }
            }

            Map<String, Object> item = new HashMap<>();
            item.put("vacheId", h.getVache().getId());
            item.put("numeroBoucle", h.getVache().getNumeroBoucle());
            item.put("dateChaleur", dateChaleur);
            item.put("statut", "En chaleur");
            long joursRestants = ChronoUnit.DAYS.between(now, dateChaleur);
            item.put("joursRestants", joursRestants < 0 ? 0 : joursRestants);
            result.add(item);
        }

        result.sort(Comparator.comparing(item -> (LocalDate) item.get("dateChaleur")));
        return result;
    }

    @Transactional(readOnly = true)
    public List<Vache> getVachesEnChaleurActuellement() {
        LocalDate today = LocalDate.now();

        List<Vache> vachesVideEnChaleur = getVidesActuelles(null).stream()
                .filter(h -> today.equals(getProchaineDateCycle(h.getDateDebut())))
                .map(VacheHistoriqueRepro::getVache)
                .collect(Collectors.toList());

        List<Vache> vachesEnChaleurManuel = statutReproService.getHistoriqueOuvertParStatut(
                statutReproService.getByLibelle("En_chaleur").getId()).stream()
                .map(VacheHistoriqueRepro::getVache)
                .collect(Collectors.toList());

        return Stream.concat(vachesVideEnChaleur.stream(), vachesEnChaleurManuel.stream())
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LocalDate getProchaineChaleurGlobale() {
        return getVidesActuelles(null).stream()
                .map(h -> getProchaineDateCycle(h.getDateDebut()))
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    private List<VacheHistoriqueRepro> getVidesActuelles(Long vacheId) {
        Integer videId = statutReproService.getByLibelle("Vide").getId();
        List<VacheHistoriqueRepro> vides = statutReproService.getHistoriqueOuvertParStatut(videId);
        if (vacheId == null) {
            return vides;
        }
        return vides.stream().filter(h -> h.getVache().getId().equals(vacheId)).collect(Collectors.toList());
    }

    private LocalDate getProchaineDateCycle(LocalDate dateDebut) {
        if (dateDebut == null) {
            return null;
        }
        LocalDate today = LocalDate.now();
        if (dateDebut.isAfter(today)) {
            return dateDebut;
        }
        long joursDepuisDernierCycle = ChronoUnit.DAYS.between(dateDebut, today) % 21;
        return joursDepuisDernierCycle == 0 ? today : today.plusDays(21 - joursDepuisDernierCycle);
    }

    private List<LocalDate> getDatesCycleDansLeMois(LocalDate dateDebut, int mois, int annee) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate startOfTargetMonth = LocalDate.of(annee, mois, 1);
        LocalDate endOfTargetMonth = startOfTargetMonth.withDayOfMonth(startOfTargetMonth.lengthOfMonth());

        if (dateDebut == null || dateDebut.isAfter(endOfTargetMonth)) {
            return dates;
        }

        LocalDate cursor = dateDebut;
        while (cursor.isBefore(startOfTargetMonth)) {
            cursor = cursor.plusDays(21);
        }
        if (!cursor.isAfter(endOfTargetMonth) && cursor.getMonthValue() == mois && cursor.getYear() == annee) {
            dates.add(cursor);
        }
        return dates;
    }

    public List<Map<String, Object>> getVachesPourFiltre() {
        String sql = "SELECT v.id, v.numero_boucle FROM vache v ORDER BY v.numero_boucle ASC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> vache = new HashMap<>();
            vache.put("id", rs.getLong("id"));
            vache.put("numeroBoucle", rs.getString("numero_boucle"));
            return vache;
        });
    }

    @Transactional
    public Reproduction confirmerVelage(Long reproductionId, LocalDate dateVelageReel, String sexeVeau) {
        Reproduction reproduction = reproductionRepository.findById(reproductionId)
                .orElseThrow(() -> new IllegalArgumentException("Reproduction introuvable"));
 
        reproduction.setDateVelageReel(dateVelageReel);
        reproduction.setSexeVeau(sexeVeau);
        reproductionRepository.save(reproduction);
 
        // ← ACQUITTEMENT AUTO — vêlage confirmé, l'alerte rappel_velage disparaît
        if (reproduction.getVache() != null) {
            alerteService.acquitterAutomatiquement(
                "rappel_velage",
                reproduction.getVache().getId()
            );
        }
        // ← FIN ACQUITTEMENT
 
        mettreAJourMereApresVelage(reproduction.getVache(), dateVelageReel);
        return reproduction;
    }

    private void mettreAJourMereApresVelage(Vache mere, LocalDate dateVelageReel) {
        statutReproService.changerStatut(mere, statutReproService.getByLibelle("Vide").getId(), dateVelageReel);
        statutLactationService.changerStatut(mere, statutLactationService.getByLibelle("En_lactation").getId(), dateVelageReel);

        String vieActuel = statutVieService.getStatutActuel(mere.getId()).map(s -> s.getLibelle()).orElse(null);
        if ("Genisse".equals(vieActuel)) {
            statutVieService.changerStatut(mere, statutVieService.getByLibelle("Vache_active").getId(), dateVelageReel);
        }
    }

    private void creerVeau(Reproduction reproduction, LocalDate dateVelageReel, String sexeVeau) {
        Vache mere = reproduction.getVache();
        boolean estMale = "M".equalsIgnoreCase(sexeVeau);

        Vache veau = new Vache();
        veau.setNumeroBoucle(mere.getNumeroBoucle() + "-C" + reproduction.getId());
        veau.setRace(mere.getRace());
        veau.setDateNaissance(dateVelageReel);
        veau.setMere(mere);

        vacheRepository.save(veau);

        statutVieService.ouvrirInitial(veau, statutVieService.getByLibelle(estMale ? "Veau" : "Genisse").getId(), dateVelageReel);
        statutReproService.ouvrirInitial(veau, statutReproService.getByLibelle("Vide").getId(), dateVelageReel);
        statutLactationService.ouvrirInitial(veau, statutLactationService.getByLibelle("Tarie").getId(), dateVelageReel);
        statutSanteService.ouvrirInitial(veau, statutSanteService.getByLibelle("Saine").getId(), dateVelageReel);
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
