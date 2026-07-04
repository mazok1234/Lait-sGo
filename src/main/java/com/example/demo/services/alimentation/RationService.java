package com.example.demo.services.alimentation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.alimentation.Ration;
import com.example.demo.entity.alimentation.RationAliment;
import com.example.demo.repository.alimentation.RationAlimentRepository;
import com.example.demo.repository.alimentation.RationRepository;

@Service
public class RationService {
    private final RationRepository rationRepository;
    private final RationAlimentRepository rationAlimentRepository;

    public RationService(RationRepository rationRepository,
                         RationAlimentRepository rationAlimentRepository) {
        this.rationRepository = rationRepository;
        this.rationAlimentRepository = rationAlimentRepository;
    }

    public List<RationCardVm> getRationCards() {
        return rationRepository.findAllCards().stream()
                .map(card -> new RationCardVm(
                        card.getId(),
                        card.getNom(),
                        card.getStade(),
                        rationRepository.countVachesForRation(card.getId())
                ))
                .toList();
    }

    public Ration findById(Long id) {
        return rationRepository.findById(id).orElse(null);
    }

    public Ration saveRation(Ration ration) {
        return rationRepository.save(ration);
    }

    public List<StadeVm> getStades() {
        return rationRepository.findAllStades().stream()
                .map(s -> new StadeVm(s.getId(), s.getLibelle(), s.getJourMin(), s.getJourMax()))
                .toList();
    }

    public List<StadeVm> getStadesDisponibles() {
        return rationRepository.findAvailableStades().stream()
                .map(s -> new StadeVm(s.getId(), s.getLibelle(), s.getJourMin(), s.getJourMax()))
                .toList();
    }

    public boolean isPhaseDejaAssocieARation(Integer phaseId, Long rationId) {
        if (phaseId == null) {
            return false;
        }
        return rationRepository.existsByPhaseForOtherRation(phaseId, rationId);
    }

    public List<StadeVm> getPhasesPourEdition(Long rationId) {
        List<StadeVm> phases = new ArrayList<>(getStadesDisponibles());
        Ration ration = findById(rationId);
        if (ration == null || ration.getIdPhaseLactation() == null) {
            return phases;
        }

        boolean present = phases.stream().anyMatch(s -> s.id().equals(ration.getIdPhaseLactation()));
        if (!present) {
            getStades().stream()
                    .filter(s -> s.id().equals(ration.getIdPhaseLactation()))
                    .findFirst()
                    .ifPresent(phases::add);
        }
        return phases;
    }

    public List<RationAliment> findAlimentsByRationId(Long rationId) {
        return rationAlimentRepository.findByRationIdOrderByIdAsc(rationId);
    }

    public void saveRationAliment(RationAliment rationAliment) {
        rationAlimentRepository.save(rationAliment);
    }

    public void deleteRationAliment(Long id) {
        rationAlimentRepository.deleteById(id);
    }

    public void deleteRationById(Long rationId) {
        rationAlimentRepository.deleteByRationId(rationId);
        rationRepository.deleteById(rationId);
    }

    public NutritionVm calculateNutrition(Long rationId) {
        RationRepository.NutritionProjection nutrition = rationRepository.findNutritionByRationId(rationId);
        if (nutrition == null) {
            return new NutritionVm(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        return new NutritionVm(
            nutrition.getUflTotal() != null ? nutrition.getUflTotal() : BigDecimal.ZERO,
            nutrition.getCoutTotalAr() != null ? nutrition.getCoutTotalAr() : BigDecimal.ZERO
        );
        }

        public List<RationActiveVacheVm> getRationsActivesVaches() {
        return rationRepository.findRationsActivesVaches().stream()
            .map(v -> new RationActiveVacheVm(
                v.getVacheId(),
                v.getNumeroBoucle(),
                v.getJoursEnLait(),
                v.getPhaseActuelle(),
                v.getRationId(),
                v.getRationRecommandee()
            ))
            .toList();
    }

    public record RationCardVm(Long id, String nom, String stade, long nbVaches) {
    }

    public record StadeVm(Integer id, String libelle, Integer jourMin, Integer jourMax) {
    }

    public record NutritionVm(BigDecimal uflTotal, BigDecimal coutTotalAr) {
    }

    public record RationActiveVacheVm(Long vacheId,
                                      String numeroBoucle,
                                      Integer joursEnLait,
                                      String phaseActuelle,
                                      Long rationId,
                                      String rationRecommandee) {
    }
}
