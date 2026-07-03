package com.example.demo.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.RefStatutVache;
import com.example.demo.services.StatutVacheService;

@RestController
@RequestMapping("/api/cheptel/etats-sante")
public class StatutVacheApiController {

    private final StatutVacheService statutVacheService;

    public StatutVacheApiController(StatutVacheService statutVacheService) {
        this.statutVacheService = statutVacheService;
    }

    @GetMapping
    public List<RefStatutVache> getAll() {
        return statutVacheService.findAll();
    }

    @PostMapping
    public RefStatutVache create(@RequestBody RefStatutVache statut) {
        return statutVacheService.create(statut);
    }

    @PutMapping("/{id}")
    public RefStatutVache update(@PathVariable Integer id, @RequestBody RefStatutVache statut) {
        return statutVacheService.update(id, statut);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        statutVacheService.delete(id);
        return ResponseEntity.noContent().build();
    }
}