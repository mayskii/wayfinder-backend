package com.wayfinder.backend.controller;

import com.wayfinder.backend.model.RouteAttraction;
import com.wayfinder.backend.repository.RouteAttractionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/route-attractions")
public class RouteAttractionController {

    private final RouteAttractionRepository routeAttractionRepository;

    public RouteAttractionController(RouteAttractionRepository routeAttractionRepository) {
        this.routeAttractionRepository = routeAttractionRepository;
    }

    // READ ALL
    @GetMapping
    public List<RouteAttraction> getAll() {
        return routeAttractionRepository.findAll();
    }

    // CREATE
    @PostMapping
    public RouteAttraction create(@RequestBody RouteAttraction ra) {
        ra.setCreatedAt(LocalDateTime.now());
        return routeAttractionRepository.save(ra);
    }

    // UPDATE position
    @PutMapping("/{id}")
    public ResponseEntity<Object> updatePosition(@PathVariable Integer id, @RequestBody RouteAttraction updatedRA) {
        return routeAttractionRepository.findById(id)
                .<ResponseEntity<Object>>map(ra -> ResponseEntity.ok(updateFields(ra, updatedRA)))
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(Map.of(
                                "status", 404,
                                "message", "RouteAttraction not found with id " + id
                        )));
    }

    private RouteAttraction updateFields(RouteAttraction ra, RouteAttraction updated) {
        if (updated.getPosition() != null) ra.setPosition(updated.getPosition());
        return routeAttractionRepository.save(ra);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        routeAttractionRepository.deleteById(id);
    }
}