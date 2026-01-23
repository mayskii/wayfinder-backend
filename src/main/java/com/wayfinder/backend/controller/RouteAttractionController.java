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
        if (ra.getRoute() == null || ra.getRoute().getId() == null) {
            return ResponseEntity.badRequest().body("route.id is required");
        }

        if (ra.getAttraction() == null || ra.getAttraction().getOsmId() == null) {
            return ResponseEntity.badRequest().body("attraction.osmId is required");
        }

        // Получаем существующие сущности из базы
        Route route = routeRepository.findById(ra.getRoute().getId())
                .orElseThrow(() -> new RuntimeException("Route not found with id " + ra.getRoute().getId()));

        Attraction attraction = attractionRepository.findById(ra.getAttraction().getOsmId())
                .orElseThrow(() -> new RuntimeException("Attraction not found with osmId " + ra.getAttraction().getOsmId()));

        ra.setRoute(route);
        ra.setAttraction(attraction);
        ra.setCreatedAt(LocalDateTime.now());

        return ResponseEntity.ok(routeAttractionRepository.save(ra));
    }

    // UPDATE position
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePosition(@PathVariable Integer id, @RequestBody RouteAttraction updatedRA) {
        var optionalRA = routeAttractionRepository.findById(id);

        if (optionalRA.isPresent()) {
            RouteAttraction ra = optionalRA.get();
            if (updatedRA.getPosition() != null) {
                ra.setPosition(updatedRA.getPosition());
            }
            RouteAttraction savedRA = routeAttractionRepository.save(ra);
            return ResponseEntity.ok(savedRA);
        } else {
            return ResponseEntity.status(404)
                    .body(Map.of(
                            "status", 404,
                            "message", "RouteAttraction not found with id " + id
                    ));
        }
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        routeAttractionRepository.deleteById(id);
    }
}