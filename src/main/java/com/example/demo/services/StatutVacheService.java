package com.example.demo.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.RefStatutVache;
import com.example.demo.repository.RefStatutVacheRepository;

@Service
public class StatutVacheService {

    private final RefStatutVacheRepository statutVacheRepository;

    public StatutVacheService(RefStatutVacheRepository statutVacheRepository) {
        this.statutVacheRepository = statutVacheRepository;
    }

    public List<RefStatutVache> findAll() {
        return statutVacheRepository.findAll();
    }

    public RefStatutVache getById(Integer id) {
        return statutVacheRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Statut introuvable: " + id));
    }

    public RefStatutVache create(RefStatutVache statut) {
        return statutVacheRepository.save(statut);
    }

    public RefStatutVache update(Integer id, RefStatutVache statut) {
        RefStatutVache existing = getById(id);
        existing.setCode(statut.getCode());
        existing.setLibelle(statut.getLibelle());
        return statutVacheRepository.save(existing);
    }

    public void delete(Integer id) {
        statutVacheRepository.deleteById(id);
    }
}