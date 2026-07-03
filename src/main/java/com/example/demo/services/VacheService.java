package com.example.demo.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.demo.entity.RefRace;
import com.example.demo.entity.RefStatutVache;
import com.example.demo.entity.Vache;
import com.example.demo.repository.RefRaceRepository;
import com.example.demo.repository.RefStatutVacheRepository;
import com.example.demo.repository.VacheRepository;

@Service
public class VacheService {

    private final VacheRepository vacheRepository;
    private final RefRaceRepository raceRepository;
    private final RefStatutVacheRepository statutVacheRepository;

    public VacheService(VacheRepository vacheRepository, RefRaceRepository raceRepository, RefStatutVacheRepository statutVacheRepository) {
        this.vacheRepository = vacheRepository;
        this.raceRepository = raceRepository;
        this.statutVacheRepository = statutVacheRepository;
    }

    public Vache getById(Long id) {
        return vacheRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Vache introuvable: " + id));
    }

    public Page<Vache> search(Specification<Vache> spec, Pageable pageable) {
        return vacheRepository.findAll(spec, pageable);
    }

    public Vache create(Vache vache) {
        return vacheRepository.save(vache);
    }

    public Vache update(Long id, Vache vache) {
        Vache existing = getById(id);
        existing.setNumeroBoucle(vache.getNumeroBoucle());
        existing.setDateNaissance(vache.getDateNaissance());
        existing.setPoidsKg(vache.getPoidsKg());
        existing.setScoreBcs(vache.getScoreBcs());
        existing.setScoreLocomotion(vache.getScoreLocomotion());

        RefRace race = raceRepository.findById(vache.getRace().getId())
                .orElseThrow(() -> new IllegalArgumentException("Race introuvable: " + vache.getRace().getId()));
        existing.setRace(race);

        RefStatutVache statut = statutVacheRepository.findById(vache.getStatut().getId())
                .orElseThrow(() -> new IllegalArgumentException("Statut introuvable: " + vache.getStatut().getId()));
        existing.setStatut(statut);

        existing.setMere(vache.getMere() != null ? getById(vache.getMere().getId()) : null);

        return vacheRepository.save(existing);
    }

    public void delete(Long id) {
        vacheRepository.deleteById(id);
    }
}