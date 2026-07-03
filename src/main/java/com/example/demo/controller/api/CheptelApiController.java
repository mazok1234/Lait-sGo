package com.example.demo.controller.api;

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

import com.example.demo.entity.Vache;
import com.example.demo.repository.VacheRepository;

@RestController
@RequestMapping("/api/cheptel")
public class CheptelApiController {

    private final VacheRepository vacheRepository;

    public CheptelApiController(VacheRepository vacheRepository) {
        this.vacheRepository = vacheRepository;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        long total = vacheRepository.count();
        long mereCount = vacheRepository.count((root, query, cb) -> cb.isNotNull(root.get("mere")));

        // origine: deduite de mere_id (mere != null => née à la ferme)
        long achetees = vacheRepository.count((root, query, cb) -> cb.isNull(root.get("mere")));

        Map<String, Object> payload = new HashMap<>();
        payload.put("totalVaches", total);
        payload.put("vachesAvecMere", mereCount);
        payload.put("vachesAchetees", achetees);

        // Répartition par statut
        Map<String, Long> repartition = new HashMap<>();
        repartition.put("en_lactation", vacheRepository.count((root, query, cb) ->
                cb.and(
                        cb.isNotNull(root.get("statut")),
                        cb.equal(root.get("statut").get("code"), "en_lactation")
                )));

        repartition.put("reformee", vacheRepository.count((root, query, cb) ->
                cb.and(
                        cb.isNotNull(root.get("statut")),
                        cb.equal(root.get("statut").get("code"), "reformee")
                )));

        // Statuts inconnus (optionnel)
        long autres = vacheRepository.count((root, query, cb) ->
                cb.and(
                        cb.isNotNull(root.get("statut")),
                        cb.not(root.get("statut").get("code").in("en_lactation", "reformee"))
                ));
        if (autres > 0) {
            repartition.put("autres", autres);
        }

        payload.put("repartitionParStatut", repartition);

        // Alertes non acquittées : table alerte existe, mais on ne la mappe pas pour cette phase.
        payload.put("alertesNonAcquittees", 0L);

        return payload;
    }

    @GetMapping("/vaches")
    public Map<String, Object> listVaches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer raceId,
            @RequestParam(required = false) Integer statutId,
            @RequestParam(required = false) String origine,
            @RequestParam(required = false) String etatSante
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Specification<Vache> spec = (root, query, cb) -> cb.conjunction();

        if (raceId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("race").get("id"), raceId));
        }
        if (statutId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("statut").get("id"), statutId));
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