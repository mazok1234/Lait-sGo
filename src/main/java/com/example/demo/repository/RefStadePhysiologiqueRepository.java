package com.example.demo.repository;

import com.example.demo.entity.RefStadePhysiologique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefStadePhysiologiqueRepository extends JpaRepository<RefStadePhysiologique, Integer> {
}
