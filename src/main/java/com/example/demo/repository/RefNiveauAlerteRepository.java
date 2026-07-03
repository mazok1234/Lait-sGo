package com.example.demo.repository;

import com.example.demo.entity.RefNiveauAlerte;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefNiveauAlerteRepository extends JpaRepository<RefNiveauAlerte, Integer> {

    Optional<RefNiveauAlerte> findByCode(String code); 
}