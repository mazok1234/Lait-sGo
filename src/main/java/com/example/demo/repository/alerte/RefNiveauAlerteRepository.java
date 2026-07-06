package com.example.demo.repository.alerte;

import com.example.demo.entity.alerte.RefNiveauAlerte;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RefNiveauAlerteRepository extends JpaRepository<RefNiveauAlerte, Integer> {

    Optional<RefNiveauAlerte> findByCode(String code);

    // Triés par ordre pour les filtres du dashboard
    List<RefNiveauAlerte> findAllByOrderByOrdreAsc();
}