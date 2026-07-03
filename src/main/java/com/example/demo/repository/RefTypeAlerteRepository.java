package com.example.demo.repository;

import com.example.demo.entity.RefTypeAlerte;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefTypeAlerteRepository extends JpaRepository<RefTypeAlerte, Integer> {

    Optional<RefTypeAlerte> findByCode(String code); 
}