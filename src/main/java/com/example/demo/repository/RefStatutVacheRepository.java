package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.RefStatutVache;
import java.util.Optional;

public interface RefStatutVacheRepository extends JpaRepository<RefStatutVache, Integer> {
	Optional<RefStatutVache> findByCode(String code);
}