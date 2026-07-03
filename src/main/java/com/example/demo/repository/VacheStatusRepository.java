package com.example.demo.repository;

import com.example.demo.entity.VacheStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VacheStatusRepository extends JpaRepository<VacheStatus, Long> {

    List<VacheStatus> findByVacheIdOrderByDateDebutDesc(Long vacheId);
}
