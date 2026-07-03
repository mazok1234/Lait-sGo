package com.example.demo.services;

import com.example.demo.entity.RefStadePhysiologique;
import com.example.demo.repository.RefStadePhysiologiqueRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RefStadePhysiologiqueService {

    private final RefStadePhysiologiqueRepository stadeRepository;

    public RefStadePhysiologiqueService(RefStadePhysiologiqueRepository stadeRepository) {
        this.stadeRepository = stadeRepository;
    }

    public List<RefStadePhysiologique> findAll() {
        return stadeRepository.findAll();
    }

    public RefStadePhysiologique findById(Integer id) {
        Optional<RefStadePhysiologique> opt = stadeRepository.findById(id);
        return opt.orElse(null);
    }

    public RefStadePhysiologique save(RefStadePhysiologique stade) {
        return stadeRepository.save(stade);
    }

    public void deleteById(Integer id) {
        stadeRepository.deleteById(id);
    }

    public boolean deleteIfNotUsed(Integer id) {
        if (stadeRepository.isUsedByRation(id)) {
            return false;
        }

        try {
            stadeRepository.deleteById(id);
            return true;
        } catch (DataIntegrityViolationException ex) {
            // Protection supplémentaire en cas de concurrence.
            return false;
        }
    }
}
