package com.example.demo.cheptel.controllers.api;

import com.example.demo.cheptel.entities.Race;
import com.example.demo.cheptel.services.RaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cheptel/races")
public class RaceApiController {

    private final RaceService raceService;

    public RaceApiController(RaceService raceService) {
        this.raceService = raceService;
    }

    @GetMapping
    public List<Race> getAll() {
        return raceService.findAll();
    }

    @PostMapping
    public Race create(@RequestBody Race race) {
        return raceService.create(race);
    }

    @PutMapping("/{id}")
    public Race update(@PathVariable Long id, @RequestBody Race race) {
        return raceService.update(id, race);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        raceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

