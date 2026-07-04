package com.example.demo.controller.cheptel;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.cheptel.Vache;
import com.example.demo.repository.cheptel.VacheRepository;
import com.example.demo.services.cheptel.StatutLactationVacheService;
import com.example.demo.services.cheptel.StatutVieService;

@RestController
@RequestMapping("/api/cheptel")
public class CheptelApiController {
    private final VacheRepository vacheRepository;
    private final StatutVieService statutVieService;
    private final StatutLactationVacheService statutLactationService;

    public CheptelApiController(VacheRepository vacheRepository, StatutVieService statutVieService,
            StatutLactationVacheService statutLactationService) {
        this.vacheRepository = vacheRepository;
        this.statutVieService = statutVieService;
        this.statutLactationService = statutLactationService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        long total = vacheRepository.count();
        long mereCount = vacheRepository.count((root, query, cb) -> cb.isNotNull(root.get("mere")));
        long achetees = vacheRepository.count((root, query, cb) -> cb.isNull(root.get("mere")));

        Map<String, Object> payload = new HashMap<>();
        payload.put("totalVaches", total);
        payload.put("vachesAvecMere", mereCount);
        payload.put("vachesAchetees", achetees);

        Map<String, Long> repartition = new HashMap<>();
        repartition.put("en_lactation",
                (long) statutLactationService.findVacheIdsByStatut(statutLactationService.getByLibelle("En_lactation").getId()).size());
        repartition.put("reformee",
                (long) statutVieService.findVacheIdsByStatut(statutVieService.getByLibelle("Reformee").getId()).size());
        payload.put("repartitionParStatut", repartition);

        payload.put("alertesNonAcquittees", 0L);

        return payload;
    }

    @GetMapping("/vaches")
    public Map<String, Object> listVaches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer raceId,
            @RequestParam(required = false) String origine
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Specification<Vache> spec = (root, query, cb) -> cb.conjunction();

        if (raceId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("race").get("id"), raceId));
        }
        if (origine != null) {
            if (origine.equalsIgnoreCase("NEE_A_LA_FERME")) {
                spec = spec.and((root, query, cb) -> cb.isNotNull(root.get("mere")));
            } else if (origine.equalsIgnoreCase("ACHETEE")) {
                spec = spec.and((root, query, cb) -> cb.isNull(root.get("mere")));
            }
        }
        if (q != null && !q.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("numeroBoucle")), "%" + q.toLowerCase() + "%"));
        }

        Page<Vache> p = vacheRepository.findAll(spec, pageable);

        Map<String, Object> payload = new HashMap<>();
        payload.put("content", p.getContent());
        payload.put("page", p.getNumber());
        payload.put("size", p.getSize());
        payload.put("totalElements", p.getTotalElements());
        payload.put("totalPages", p.getTotalPages());
        return payload;
    }

    @PostMapping("/vaches")
    public Vache create(@RequestBody Vache vache) {
        return vacheRepository.save(vache);
    }

    @PutMapping("/vaches/{id}")
    public Vache update(@PathVariable Long id, @RequestBody Vache vache) {
        vache.setId(id);
        return vacheRepository.save(vache);
    }

    @DeleteMapping("/vaches/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vacheRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
