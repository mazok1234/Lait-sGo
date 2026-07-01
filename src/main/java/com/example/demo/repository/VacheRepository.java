package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Vache;

public interface VacheRepository extends JpaRepository<Vache, Long> {
}