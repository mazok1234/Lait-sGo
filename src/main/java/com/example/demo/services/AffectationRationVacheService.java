package com.example.demo.services;

import com.example.demo.entity.AffectationRationVache;
import com.example.demo.repository.AffectationRationVacheRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AffectationRationVacheService {

    private final AffectationRationVacheRepository affectationRepository;

    public AffectationRationVacheService(AffectationRationVacheRepository affectationRepository) {
        this.affectationRepository = affectationRepository;
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
}
