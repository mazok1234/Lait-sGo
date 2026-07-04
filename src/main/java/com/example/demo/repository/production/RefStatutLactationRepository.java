package com.example.demo.repository.production;

import java.util.Optional;

import com.example.demo.entity.production.RefStatutLactation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefStatutLactationRepository extends JpaRepository<RefStatutLactation, Integer> {
    Optional<RefStatutLactation> findByCode(String code);
}
