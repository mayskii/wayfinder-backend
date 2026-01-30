package com.wayfinder.backend.controller;

import com.wayfinder.backend.model.Route;
import com.wayfinder.backend.model.User;
import com.wayfinder.backend.repository.RouteRepository;
import com.wayfinder.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import com.wayfinder.backend.repository.RouteAttractionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/routes")
public class RouteController {

    private final RouteRepository routeRepository;
    private final UserRepository userRepository;
    private final RouteAttractionRepository routeAttractionRepository;

    public RouteController(RouteRepository routeRepository, UserRepository userRepository, RouteAttractionRepository routeAttractionRepository) {
        this.routeRepository = routeRepository;
        this.userRepository = userRepository;
        this.routeAttractionRepository = routeAttractionRepository;
    }

    // READ ALL
    @GetMapping
    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<?> getRouteById(@PathVariable Integer id) {
        return routeRepository.findById(id)
                .<ResponseEntity<Object>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(Map.of("status", 404, "message", "Route not found with id " + id)));
    }

    // READ ALL ROUTES BY USER
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<?> getRoutesByUser(@PathVariable Integer userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.status(404)
                    .body(Map.of("status", 404, "message", "User not found with id " + userId));
        }
        List<Route> routes = routeRepository.findByUserId(userId);
        return ResponseEntity.ok(routes);
    }

    // CREATE
    @PostMapping
    public ResponseEntity<?> createRoute(@RequestBody Route route) {
        if (route.getUser() != null) {
            User user = userRepository.findById(route.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("User not found with id " + route.getUser().getId()));
            route.setUser(user);
        }
        route.setCreatedAt(LocalDateTime.now());
        return ResponseEntity.ok(routeRepository.save(route));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateRoute(@PathVariable Integer id, @RequestBody Route updatedRoute) {
        return routeRepository.findById(id)
                .<ResponseEntity<Object>>map(route -> ResponseEntity.ok(updateRouteFields(route, updatedRoute)))
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(Map.of(
                                "status", 404,
                                "message", "Route not found with id " + id
                        )));
    }

    private Route updateRouteFields(Route route, Route updated) {
        route.setName(updated.getName());
        if (updated.getUser() != null) {
            User user = userRepository.findById(updated.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("User not found with id " + updated.getUser().getId()));
            route.setUser(user);
        }
        return routeRepository.save(route);
    }

    // DELETE
    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoute(@PathVariable Integer id) {
        if (!routeRepository.existsById(id)) {
            return ResponseEntity.status(404)
                    .body(Map.of("status", 404, "message", "Route not found"));
        }

        routeAttractionRepository.deleteByRouteId(id);
        routeRepository.deleteById(id);

        return ResponseEntity.ok(Map.of("message", "Route deleted"));
    }

}

