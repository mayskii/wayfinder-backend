package com.wayfinder.backend.controller;

import com.wayfinder.backend.model.Attraction;
import com.wayfinder.backend.model.Route;
import com.wayfinder.backend.model.RouteAttraction;
import com.wayfinder.backend.repository.AttractionRepository;
import com.wayfinder.backend.repository.RouteAttractionRepository;
import com.wayfinder.backend.repository.RouteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/route-attractions")
public class RouteAttractionController {

    private final RouteAttractionRepository routeAttractionRepository;
    private final RouteRepository routeRepository;
    private final AttractionRepository attractionRepository;

    public RouteAttractionController(RouteAttractionRepository routeAttractionRepository,
                                     RouteRepository routeRepository,
                                     AttractionRepository attractionRepository) {
        this.routeAttractionRepository = routeAttractionRepository;
        this.routeRepository = routeRepository;
        this.attractionRepository = attractionRepository;
    }

    // READ ALL
    @GetMapping
    public List<RouteAttraction> getAll() {
        return routeAttractionRepository.findAll();
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Object> create(@RequestBody RouteAttraction ra) {
        try {
            ra.setCreatedAt(LocalDateTime.now());
            return ResponseEntity.ok(saveRouteAttractionWithRelations(ra, ra));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody RouteAttraction updatedRA) {
        return routeAttractionRepository.findById(id)
                .<ResponseEntity<Object>>map(ra -> ResponseEntity.ok(saveRouteAttractionWithRelations(ra, updatedRA)))
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(Map.of("status", 404, "message", "RouteAttraction not found with id " + id)));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        routeAttractionRepository.deleteById(id);
    }

    // Общий метод для POST и PUT
    private RouteAttraction saveRouteAttractionWithRelations(RouteAttraction ra, RouteAttraction updated) {

        if (updated.getRoute() == null || updated.getRoute().getId() == null)
            throw new RuntimeException("route.id is required");

        if (updated.getAttraction() == null || updated.getAttraction().getId() == null)
            throw new RuntimeException("attraction.id is required");

        Route route = routeRepository.findById(updated.getRoute().getId())
                .orElseThrow(() -> new RuntimeException("Route not found with id " + updated.getRoute().getId()));

        Attraction attraction = attractionRepository.findById(updated.getAttraction().getId())
                .orElseThrow(() -> new RuntimeException("Attraction not found with id " + updated.getAttraction().getId()));

        ra.setRoute(route);
        ra.setAttraction(attraction);

        if (updated.getPosition() != null) {
            ra.setPosition(updated.getPosition());
        }

        return routeAttractionRepository.save(ra);
    }
}