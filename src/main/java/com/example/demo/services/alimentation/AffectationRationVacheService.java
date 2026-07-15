package com.example.demo.services.alimentation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.alimentation.AffectationRationVache;
import com.example.demo.entity.alimentation.Ration;
import com.example.demo.entity.cheptel.Vache;
import com.example.demo.repository.alimentation.AffectationRationVacheRepository;
import com.example.demo.repository.alimentation.RationRepository;
import com.example.demo.repository.cheptel.VacheRepository;

@Service
public class AffectationRationVacheService {
    private final AffectationRationVacheRepository affectationRepository;
    private final VacheRepository vacheRepository;
    private final RationRepository rationRepository;

    public AffectationRationVacheService(AffectationRationVacheRepository affectationRepository,
                                         VacheRepository vacheRepository,
                                         RationRepository rationRepository) {
        this.affectationRepository = affectationRepository;
        this.vacheRepository = vacheRepository;
        this.rationRepository = rationRepository;
    }

    public List<AffectationRationVache> findAll() {
        return affectationRepository.findAllByOrderByDateDebutDescIdDesc();
    }

    public AffectationRationVache findById(Long id) {
        Optional<AffectationRationVache> opt = affectationRepository.findById(id);
        return opt.orElse(null);
    }

    public AffectationRationVache save(AffectationRationVache affectation) {
        if (affectation.getActif() == null) {
            affectation.setActif(true);
        }
        return affectationRepository.save(affectation);
    }

    public void deleteById(Long id) {
        affectationRepository.deleteById(id);
    }

    @Transactional
    public AffectationRationVache appliquerSuggestion(Long vacheId, Long rationSuggereeId) {
        Vache vache = vacheRepository.findById(vacheId)
                .orElseThrow(() -> new IllegalArgumentException("Vache introuvable avec l'id " + vacheId));
        Ration ration = rationRepository.findById(rationSuggereeId)
                .orElseThrow(() -> new IllegalArgumentException("Ration introuvable avec l'id " + rationSuggereeId));

        List<AffectationRationVache> actives = affectationRepository.findAllByOrderByDateDebutDescIdDesc()
                .stream()
                .filter(a -> a.getVache().getId().equals(vacheId) && Boolean.TRUE.equals(a.getActif()))
                .toList();

        for (AffectationRationVache active : actives) {
            active.setActif(false);
            active.setDateFin(LocalDate.now());
            affectationRepository.save(active);
        }

        AffectationRationVache nouvelle = new AffectationRationVache();
        nouvelle.setVache(vache);
        nouvelle.setRation(ration);
        nouvelle.setDateDebut(LocalDate.now());
        nouvelle.setActif(true);

        return affectationRepository.save(nouvelle);
    }
}
