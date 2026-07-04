package com.example.demo.repository.sante;

import com.example.demo.entity.sante.ProtocoleVaccin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProtocoleVaccinRepository extends JpaRepository<ProtocoleVaccin, Integer> {
    Optional<ProtocoleVaccin> findByNomVaccin(String nomVaccin);
}
