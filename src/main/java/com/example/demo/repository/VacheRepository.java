package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Vache;

@Repository
public interface VacheRepository extends JpaRepository<Vache, Long>, JpaSpecificationExecutor<Vache> {
    Optional<Vache> findByNumeroBoucle(String numeroBoucle);

    long countByStatut_Code(String code);
}
