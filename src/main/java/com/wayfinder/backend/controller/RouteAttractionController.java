package com.wayfinder.backend.controller;

import com.wayfinder.backend.model.Attraction;
import com.wayfinder.backend.model.Route;
import com.wayfinder.backend.model.RouteAttraction;
import com.wayfinder.backend.repository.AttractionRepository;
import com.wayfinder.backend.repository.RouteAttractionRepository;
import com.wayfinder.backend.repository.RouteRepository;
import com.wayfinder.backend.service.RouteOptimizationService;
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
    private final RouteOptimizationService routeOptimizationService;

    public RouteAttractionController(
            RouteAttractionRepository routeAttractionRepository,
            RouteRepository routeRepository,
            AttractionRepository attractionRepository,
            RouteOptimizationService routeOptimizationService
    ) {
        this.routeAttractionRepository = routeAttractionRepository;
        this.routeRepository = routeRepository;
        this.attractionRepository = attractionRepository;
        this.routeOptimizationService = routeOptimizationService;
    }

    // READ ALL
    @GetMapping
    public List<RouteAttraction> getAll() {
        return routeAttractionRepository.findAll();
    }

    // READ BY ROUTE
    @GetMapping("/by-route/{routeId}")
    public List<RouteAttraction> getByRoute(@PathVariable Integer routeId) {
        return routeAttractionRepository.findByRouteIdOrderByPosition(routeId);
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Object> create(@RequestBody RouteAttraction ra) {
        try {
            ra.setCreatedAt(LocalDateTime.now());
            return ResponseEntity.ok(saveRouteAttractionWithRelations(ra, ra, true));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody RouteAttraction updatedRA) {
        return routeAttractionRepository.findById(id)
                .<ResponseEntity<Object>>map(ra -> ResponseEntity.ok(saveRouteAttractionWithRelations(ra, updatedRA, false)))
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(Map.of("status", 404, "message", "RouteAttraction not found with id " + id)));
    }

    // --- UPDATE POSITION ---
    @PatchMapping("/{id}/position")
    public ResponseEntity<Object> updatePosition(@PathVariable Integer id, @RequestParam Integer newPosition) {
        RouteAttraction ra = routeAttractionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RouteAttraction not found with id " + id));

        List<RouteAttraction> routeAttractions = routeAttractionRepository.findByRouteIdOrderByPosition(ra.getRoute().getId());

        int minPos = 1;
        int maxPos = routeAttractions.size();

        if (newPosition < minPos || newPosition > maxPos)
            return ResponseEntity.badRequest().body(Map.of("error", "Position must be between 1 and " + maxPos));

        routeAttractions.remove(ra);
        routeAttractions.add(newPosition - 1, ra);

        for (int i = 0; i < routeAttractions.size(); i++) {
            routeAttractions.get(i).setPosition(i + 1);
        }

        routeAttractionRepository.saveAll(routeAttractions);
        return ResponseEntity.ok(Map.of("message", "Position updated"));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        routeAttractionRepository.deleteById(id);
    }

    // Общий метод для POST и PUT
    private RouteAttraction saveRouteAttractionWithRelations(RouteAttraction ra, RouteAttraction updated, boolean autoPosition) {

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

        if (autoPosition) {
            Integer maxPosition = routeAttractionRepository.findMaxPositionByRouteId(route.getId());
            ra.setPosition(maxPosition != null ? maxPosition + 1 : 1);
        } else if (updated.getPosition() != null) {
            ra.setPosition(updated.getPosition());
        }

        return routeAttractionRepository.save(ra);
    }

    // Optimization
    @PostMapping("/optimize/{routeId}")
    public ResponseEntity<?> optimizeRoute(@PathVariable Integer routeId) {
        routeOptimizationService.optimizeRoute(routeId);
        return ResponseEntity.ok(
                java.util.Map.of("message", "Route optimization started")
        );
    }
}