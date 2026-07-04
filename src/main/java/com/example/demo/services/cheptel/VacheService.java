package com.example.demo.services.cheptel;

import com.example.demo.services.alerte.AlerteService;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.cheptel.RefRace;
import com.example.demo.entity.cheptel.Vache;
import com.example.demo.repository.cheptel.RefRaceRepository;
import com.example.demo.repository.cheptel.VacheRepository;

@Service
public class VacheService {
    private static final BigDecimal BCS_MIN = new BigDecimal("2.5");
    private static final BigDecimal BCS_MAX = new BigDecimal("4.0");

    private final VacheRepository vacheRepository;
    private final RefRaceRepository raceRepository;
    private final AlerteService alerteService;
    private final StatutVieService statutVieService;
    private final StatutReproService statutReproService;
    private final StatutLactationVacheService statutLactationService;
    private final StatutSanteService statutSanteService;

    public VacheService(VacheRepository vacheRepository, RefRaceRepository raceRepository,
            AlerteService alerteService, StatutVieService statutVieService, StatutReproService statutReproService,
            StatutLactationVacheService statutLactationService, StatutSanteService statutSanteService) {
        this.vacheRepository = vacheRepository;
        this.raceRepository = raceRepository;
        this.alerteService = alerteService;
        this.statutVieService = statutVieService;
        this.statutReproService = statutReproService;
        this.statutLactationService = statutLactationService;
        this.statutSanteService = statutSanteService;
    }

    public Vache getById(Long id) {
        return vacheRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Vache introuvable: " + id));
    }

    public Page<Vache> search(Specification<Vache> spec, Pageable pageable) {
        return vacheRepository.findAll(spec, pageable);
    }

    public Vache create(Vache vache) {
        vacheRepository.findByNumeroBoucle(vache.getNumeroBoucle()).ifPresent(v -> {
            throw new IllegalArgumentException(
                    "Le numéro de boucle \"" + vache.getNumeroBoucle() + "\" est déjà utilisé par une autre vache.");
        });
        return vacheRepository.save(vache);
    }

    public Vache update(Long id, Vache vache) {
        vacheRepository.findByNumeroBoucle(vache.getNumeroBoucle())
                .filter(v -> !v.getId().equals(id))
                .ifPresent(v -> {
                    throw new IllegalArgumentException(
                            "Le numéro de boucle \"" + vache.getNumeroBoucle() + "\" est déjà utilisé par une autre vache.");
                });

        Vache existing = getById(id);
        existing.setNumeroBoucle(vache.getNumeroBoucle());
        existing.setDateNaissance(vache.getDateNaissance());
        existing.setPoidsKg(vache.getPoidsKg());
        existing.setScoreBcs(vache.getScoreBcs());
        existing.setScoreLocomotion(vache.getScoreLocomotion());

        RefRace race = raceRepository.findById(vache.getRace().getId())
                .orElseThrow(() -> new IllegalArgumentException("Race introuvable: " + vache.getRace().getId()));
        existing.setRace(race);

        existing.setMere(vache.getMere() != null ? getById(vache.getMere().getId()) : null);

        return vacheRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        long filles = vacheRepository.countByMere_Id(id);
        if (filles > 0) {
            throw new IllegalArgumentException(
                    "Impossible de supprimer cette vache : elle est la mère de " + filles + " autre(s) vache(s).");
        }

        alerteService.detacherVache(id);
        statutVieService.supprimerHistorique(id);
        statutReproService.supprimerHistorique(id);
        statutLactationService.supprimerHistorique(id);
        statutSanteService.supprimerHistorique(id);
        vacheRepository.deleteById(id);
    }

    public long count() {
        return vacheRepository.count();
    }

    public long countAvecMere() {
        return vacheRepository.countByMereIsNotNull();
    }

    public long countEnSurveillance() {
        return vacheRepository.countASurveiller(BCS_MIN, BCS_MAX);
    }

    public void declencherAlertesBcs(List<Vache> vaches) {
        for (Vache v : vaches) {
            if (v.getScoreBcs() == null) {
                continue;
            }
            if (v.getScoreBcs().compareTo(BCS_MIN) < 0 || v.getScoreBcs().compareTo(BCS_MAX) > 0) {
                alerteService.envoyerAlerte(
                        "bcs_hors_plage",
                        "attention",
                        "BCS hors plage — " + v.getNumeroBoucle(),
                        "Score BCS de " + v.getScoreBcs() + " en dehors de la plage recommandée ("
                                + BCS_MIN + " - " + BCS_MAX + ").",
                        v.getId());
            }
        }
    }
}
