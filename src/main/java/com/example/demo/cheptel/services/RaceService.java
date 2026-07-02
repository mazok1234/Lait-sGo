package com.example.demo.cheptel.services;

import com.example.demo.cheptel.entities.Race;
import com.example.demo.cheptel.repositories.RaceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RaceService {

    private final RaceRepository raceRepository;

    public RaceService(RaceRepository raceRepository) {
        this.raceRepository = raceRepository;
    }

    public List<Race> findAll() {
        return raceRepository.findAll();
    }

    public Race getById(Long id) {
        return raceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Race introuvable: " + id));
    }

    public Race create(Race race) {
        return raceRepository.save(race);
    }

    public Race update(Long id, Race race) {
        Race existing = getById(id);
        existing.setCode(race.getCode());
        existing.setLibelle(race.getLibelle());
        return raceRepository.save(existing);
    }

    public void delete(Long id) {
        raceRepository.deleteById(id);
    }
}

