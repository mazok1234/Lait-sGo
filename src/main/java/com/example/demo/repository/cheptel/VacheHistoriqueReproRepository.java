package com.example.demo.repository.cheptel;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.cheptel.VacheHistoriqueRepro;

public interface VacheHistoriqueReproRepository extends JpaRepository<VacheHistoriqueRepro, Long> {
    Optional<VacheHistoriqueRepro> findByVache_IdAndDateFinIsNull(Long vacheId);

    List<VacheHistoriqueRepro> findByStatut_IdAndDateFinIsNull(Integer statutId);

    void deleteByVache_Id(Long vacheId);
}
