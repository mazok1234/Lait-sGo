package com.example.demo.repository.sante;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.sante.MedicamentFille;

@Repository
public interface MedicamentFilleRepository extends JpaRepository<MedicamentFille, Long> {
    List<MedicamentFille> findByMedicamentId(Long medicamentId);
}