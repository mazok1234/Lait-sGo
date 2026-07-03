package com.example.demo.cheptel.controllers.api;

import com.example.demo.cheptel.entities.StatutVache;
import com.example.demo.cheptel.services.StatutVacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cheptel/etats-sante")
public class StatutVacheApiController {

    private final StatutVacheService statutVacheService;

    public StatutVacheApiController(StatutVacheService statutVacheService) {
        this.statutVacheService = statutVacheService;
    }

    @GetMapping
    public List<StatutVache> getAll() {
        return statutVacheService.findAll();
    }

    @PostMapping
    public StatutVache create(@RequestBody StatutVache statut) {
        return statutVacheService.create(statut);
    }

    @PutMapping("/{id}")
    public StatutVache update(@PathVariable Long id, @RequestBody StatutVache statut) {
        return statutVacheService.update(id, statut);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        statutVacheService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

