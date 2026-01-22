package com.wayfinder.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.wayfinder.backend.model.Attraction;
import com.wayfinder.backend.repository.AttractionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/attractions")
public class AttractionController {

    private final AttractionRepository attractionRepository;

    public AttractionController(AttractionRepository attractionRepository) {
        this.attractionRepository = attractionRepository;
    }

    // READ ALL
    @GetMapping
    public List<Attraction> getAllAttractions() {
        return attractionRepository.findAll();
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<?> getAttractionById(@PathVariable Long id) {
        return attractionRepository.findById(id)
                .<ResponseEntity<Object>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(Map.of(
                                "status", 404,
                                "message", "Attraction not found with id " + id
                        )));
    }

    // CREATE
    @PostMapping
    public Attraction createAttraction(@RequestBody Attraction attraction) {
        attraction.setCreatedAt(java.time.LocalDateTime.now());
        return attractionRepository.save(attraction);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateAttraction(@PathVariable Long id, @RequestBody Attraction updatedAttraction) {

        var optionalAttraction = attractionRepository.findById(id);

        return optionalAttraction.<ResponseEntity<Object>>map(attraction -> ResponseEntity.ok(updateAttractionFields(attraction, updatedAttraction))).orElseGet(() -> ResponseEntity.status(404)
                .body(Map.of(
                        "status", 404,
                        "message", "Attraction not found with id " + id
                )));
    }

    private Attraction updateAttractionFields(Attraction attraction, Attraction updated) {
        attraction.setName(updated.getName());
        attraction.setCategory(updated.getCategory());
        attraction.setLat(updated.getLat());
        attraction.setLng(updated.getLng());
        attraction.setWebsite(updated.getWebsite());
        attraction.setWheelchair(updated.getWheelchair());
        attraction.setFee(updated.getFee());
        attraction.setCity(updated.getCity()); // если связь с City есть
        return attractionRepository.save(attraction);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void deleteAttraction(@PathVariable Long id) {
        attractionRepository.deleteById(id);
    }
}