package com.example.demo.repository;

import com.example.demo.entity.Vache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VacheRepository extends JpaRepository<Vache, Long> {
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.demo.entity.Vache;

public interface VacheRepository extends JpaRepository<Vache, Long>, JpaSpecificationExecutor<Vache> {
    Optional<Vache> findByNumeroBoucle(String numeroBoucle);

    long countByStatut_Code(String code);
}