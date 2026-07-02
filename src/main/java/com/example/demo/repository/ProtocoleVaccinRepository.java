package com.example.demo.repository;

import com.example.demo.entity.ProtocoleVaccin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface ProtocoleVaccinRepository extends JpaRepository<ProtocoleVaccin, Integer> {
    Optional<ProtocoleVaccin> findByNomVaccin(String nomVaccin);
}