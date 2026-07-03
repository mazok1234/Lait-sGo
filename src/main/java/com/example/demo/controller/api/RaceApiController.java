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

import com.example.demo.entity.RefRace;
import com.example.demo.services.RaceService;

@RestController
@RequestMapping("/api/cheptel/races")
public class RaceApiController {

    private final RaceService raceService;

    public RaceApiController(RaceService raceService) {
        this.raceService = raceService;
    }

    @GetMapping
    public List<RefRace> getAll() {
        return raceService.findAll();
    }

    @PostMapping
    public RefRace create(@RequestBody RefRace race) {
        return raceService.create(race);
    }

    @PutMapping("/{id}")
    public RefRace update(@PathVariable Integer id, @RequestBody RefRace race) {
        return raceService.update(id, race);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        raceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}