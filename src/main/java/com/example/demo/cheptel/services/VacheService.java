package com.example.demo.cheptel.services;

import com.example.demo.cheptel.entities.Race;
import com.example.demo.cheptel.entities.StatutVache;
import com.example.demo.cheptel.entities.Vache;
import com.example.demo.cheptel.repositories.RaceRepository;
import com.example.demo.cheptel.repositories.StatutVacheRepository;
import com.example.demo.cheptel.repositories.VacheRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class VacheService {

    private final VacheRepository vacheRepository;
    private final RaceRepository raceRepository;
    private final StatutVacheRepository statutVacheRepository;

    public VacheService(VacheRepository vacheRepository, RaceRepository raceRepository, StatutVacheRepository statutVacheRepository) {
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
        // id doit venir de la DB (mais ddl-auto=none). Si insert manuel requis, on laisse JPA gérer.
        // Ici on suppose que l'insert est possible avec id null.
        return vacheRepository.save(vache);
    }

    public Vache update(Long id, Vache vache) {
        Vache existing = getById(id);
        existing.setNumeroBoucle(vache.getNumeroBoucle());
        existing.setDateNaissance(vache.getDateNaissance());
        existing.setPoidsKg(vache.getPoidsKg());
        existing.setScoreBcs(vache.getScoreBcs());
        existing.setScoreLocomotion(vache.getScoreLocomotion());

        Race race = raceRepository.findById(vache.getRace().getId())
                .orElseThrow(() -> new IllegalArgumentException("Race introuvable: " + vache.getRace().getId()));
        existing.setRace(race);

        StatutVache statut = statutVacheRepository.findById(vache.getStatut().getId())
                .orElseThrow(() -> new IllegalArgumentException("Statut introuvable: " + vache.getStatut().getId()));
        existing.setStatut(statut);

        existing.setMere(vache.getMere() != null ? getById(vache.getMere().getId()) : null);

        return vacheRepository.save(existing);
    }

    public void delete(Long id) {
        vacheRepository.deleteById(id);
    }
}

