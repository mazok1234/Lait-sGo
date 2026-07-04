package com.example.demo.services.alimentation;

import com.example.demo.entity.alimentation.RefTypeAliment;
import com.example.demo.repository.alimentation.RefTypeAlimentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RefTypeAlimentService {
    private final RefTypeAlimentRepository refTypeAlimentRepository;

    public RefTypeAlimentService(RefTypeAlimentRepository refTypeAlimentRepository) {
        this.refTypeAlimentRepository = refTypeAlimentRepository;
    }

    public List<RefTypeAliment> findAll() {
        return refTypeAlimentRepository.findAll();
    }

    public RefTypeAliment findById(Integer id) {
        Optional<RefTypeAliment> opt = refTypeAlimentRepository.findById(id);
        return opt.orElse(null);
    }

    public RefTypeAliment save(RefTypeAliment typeAliment) {
        return refTypeAlimentRepository.save(typeAliment);
    }

    public void deleteById(Integer id) {
        refTypeAlimentRepository.deleteById(id);
    }
}
