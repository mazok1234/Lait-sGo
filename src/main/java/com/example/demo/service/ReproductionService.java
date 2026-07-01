package com.example.demo.service;

import com.example.demo.entity.VReproductionSuivi;
import com.example.demo.repository.VReproductionSuiviRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReproductionService {

    @Autowired
    private VReproductionSuiviRepository repo;

    public List<VReproductionSuivi> getPlanningEvents(Long vacheId, LocalDate start, LocalDate end) {
        return repo.findEventsByPeriod(vacheId, start, end);
    }

    public long getAttenteGestationCount() {
        return repo.countEnAttenteGestation();
    }

    public double getTauxReussiteIA() {
        long total = repo.countTotalIA();
        if (total == 0) return 0.0;
        return ((double) repo.countGestationsReussies() / total) * 100;
    }
}