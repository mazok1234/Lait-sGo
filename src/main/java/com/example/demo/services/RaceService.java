package com.example.demo.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.RefRace;
import com.example.demo.repository.RefRaceRepository;

@Service
public class RaceService {

    private final RefRaceRepository raceRepository;

    public RaceService(RefRaceRepository raceRepository) {
        this.raceRepository = raceRepository;
    }

    public List<RefRace> findAll() {
        return raceRepository.findAll();
    }

    public RefRace getById(Integer id) {
        return raceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Race introuvable: " + id));
    }

    public RefRace create(RefRace race) {
        return raceRepository.save(race);
    }

    public RefRace update(Integer id, RefRace race) {
        RefRace existing = getById(id);
        existing.setCode(race.getCode());
        existing.setLibelle(race.getLibelle());
        return raceRepository.save(existing);
    }

    public void delete(Integer id) {
        raceRepository.deleteById(id);
    }
}