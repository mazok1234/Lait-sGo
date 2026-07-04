package com.example.demo.repository.cheptel;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.cheptel.VacheHistoriqueSante;

public interface VacheHistoriqueSanteRepository extends JpaRepository<VacheHistoriqueSante, Long> {
    Optional<VacheHistoriqueSante> findByVache_IdAndDateFinIsNull(Long vacheId);

    List<VacheHistoriqueSante> findByStatut_IdAndDateFinIsNull(Integer statutId);

    void deleteByVache_Id(Long vacheId);
}
