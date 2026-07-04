package com.example.demo.repository.cheptel;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.cheptel.VacheHistoriqueVie;

public interface VacheHistoriqueVieRepository extends JpaRepository<VacheHistoriqueVie, Long> {
    Optional<VacheHistoriqueVie> findByVache_IdAndDateFinIsNull(Long vacheId);

    List<VacheHistoriqueVie> findByStatut_IdAndDateFinIsNull(Integer statutId);

    void deleteByVache_Id(Long vacheId);
}
