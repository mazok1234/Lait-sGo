package com.example.demo.cheptel.services;

import com.example.demo.cheptel.entities.StatutVache;
import com.example.demo.cheptel.repositories.StatutVacheRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatutVacheService {

    private final StatutVacheRepository statutVacheRepository;

    public StatutVacheService(StatutVacheRepository statutVacheRepository) {
        this.statutVacheRepository = statutVacheRepository;
    }

    public List<StatutVache> findAll() {
        return statutVacheRepository.findAll();
    }

    public StatutVache getById(Long id) {
        return statutVacheRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Statut introuvable: " + id));
    }

    public StatutVache create(StatutVache statut) {
        return statutVacheRepository.save(statut);
    }

    public StatutVache update(Long id, StatutVache statut) {
        StatutVache existing = getById(id);
        existing.setCode(statut.getCode());
        existing.setLibelle(statut.getLibelle());
        return statutVacheRepository.save(existing);
    }

    public void delete(Long id) {
        statutVacheRepository.deleteById(id);
    }
}

