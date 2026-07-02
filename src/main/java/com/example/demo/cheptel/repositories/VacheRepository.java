package com.example.demo.cheptel.repositories;

import com.example.demo.cheptel.entities.Vache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VacheRepository extends JpaRepository<Vache, Long>, JpaSpecificationExecutor<Vache> {
}

