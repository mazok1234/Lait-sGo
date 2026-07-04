package com.example.demo.services.alimentation;

import com.example.demo.entity.alimentation.Aliment;
import com.example.demo.repository.alimentation.AlimentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlimentService {
    private final AlimentRepository alimentRepository;

    public AlimentService(AlimentRepository alimentRepository) {
        this.alimentRepository = alimentRepository;
    }

    public List<Aliment> findAll() {
        return alimentRepository.findAll();
    }

    public Aliment findById(Long id) {
        Optional<Aliment> opt = alimentRepository.findById(id);
        return opt.orElse(null);
    }

    public Aliment save(Aliment aliment) {
        return alimentRepository.save(aliment);
    }

    public void deleteById(Long id) {
        alimentRepository.deleteById(id);
    }
}
