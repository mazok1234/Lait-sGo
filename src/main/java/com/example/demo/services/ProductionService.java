package com.example.demo.services;

import com.example.demo.entity.Production;
import com.example.demo.repository.ProductionRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductionService {

    private final ProductionRepository productionRepository;

    public ProductionService(ProductionRepository productionRepository) {
        this.productionRepository = productionRepository;
    }

    public List<Production> getAllProductions() {
        return productionRepository.findAll();
    }

    public void saveProduction(Production production) {
        productionRepository.save(production);
    }
}