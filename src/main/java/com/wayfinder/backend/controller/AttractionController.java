package com.wayfinder.backend.controller;

import com.wayfinder.backend.service.AttractionService;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.wayfinder.backend.model.Attraction;
import com.wayfinder.backend.repository.AttractionRepository;
import com.wayfinder.backend.repository.CityRepository;
import com.wayfinder.backend.model.City;
import com.wayfinder.backend.service.CityService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/attractions")
public class AttractionController {

    private final AttractionRepository attractionRepository;
    private final CityRepository cityRepository;
    private final AttractionService attractionService;
    private final CityService cityService;

    public AttractionController(
            AttractionRepository attractionRepository,
            CityRepository cityRepository,
            AttractionService attractionService,
            CityService cityService
    ) {
        this.attractionRepository = attractionRepository;
        this.cityRepository = cityRepository;
        this.attractionService = attractionService;
        this.cityService = cityService;
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

    // GET attractions from OSM for a given city
    @GetMapping("/from-osm")
    public List<Attraction> getAttractionsFromOsm(@RequestParam String cityName) {

        final City city = cityRepository.findByName(cityName)
                .orElseGet(() -> cityService.getCityCoordinates(cityName));

        List<Attraction> existingAttractions = attractionRepository.findByCity(city);
        int needed = 100 - existingAttractions.size();
        if (needed <= 0) return existingAttractions;

        String bbox = getString(cityName, city);
        List<Map<String, Object>> osmAttractions = attractionService.getAttractions(bbox);

        List<Attraction> newAttractions = osmAttractions.stream()
                .map(data -> {
                    Long osmId = data.get("id") != null ? Long.parseLong(data.get("id").toString()) : null;
                    if (osmId == null || attractionRepository.existsByOsmId(osmId)) return null;

                    Attraction attraction = new Attraction();
                    attraction.setOsmId(osmId);
                    attraction.setCity(city);

                    if (data.get("tags") != null) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> tags = (Map<String, Object>) data.get("tags");
                        attraction.setName(tags.getOrDefault("name", "Unknown").toString());
                        attraction.setCategory(tags.getOrDefault("tourism", "Unknown").toString());
                        attraction.setWebsite(tags.getOrDefault("website", "").toString());
                        attraction.setWheelchair(tags.getOrDefault("wheelchair", "").toString());
                        attraction.setFee(tags.getOrDefault("fee", "").toString());
                    } else {
                        attraction.setName("Unknown");
                        attraction.setCategory("Unknown");
                    }

                    if (data.get("lat") != null) {
                        attraction.setLat(Double.parseDouble(data.get("lat").toString()));
                    } else if (data.get("center") != null) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> center = (Map<String, Object>) data.get("center");
                        attraction.setLat(Double.parseDouble(center.get("lat").toString()));
                        attraction.setLng(Double.parseDouble(center.get("lon").toString()));
                    }

                    if (data.get("lon") != null) {
                        attraction.setLng(Double.parseDouble(data.get("lon").toString()));
                    }

                    attraction.setCreatedAt(LocalDateTime.now());
                    return attraction;
                })
                .filter(Objects::nonNull)
                .limit(needed)
                .map(attractionRepository::save)
                .toList();

        existingAttractions.addAll(newAttractions);
        return existingAttractions;
    }

    private static @NonNull String getString(String cityName, City city) {
        String rawBbox = city.getBbox();
        if (rawBbox == null || rawBbox.isEmpty()) {
            throw new RuntimeException("City has no bounding box: " + cityName);
        }

        // normalization
        String[] parts = rawBbox.split(",");
        if (parts.length != 4) {
            throw new RuntimeException("Invalid bbox format for city: " + cityName);
        }

        double lat1 = Double.parseDouble(parts[0]);
        double lat2 = Double.parseDouble(parts[1]);
        double lon1 = Double.parseDouble(parts[2]);
        double lon2 = Double.parseDouble(parts[3]);

        double south = Math.min(lat1, lat2);
        double north = Math.max(lat1, lat2);
        double west  = Math.min(lon1, lon2);
        double east  = Math.max(lon1, lon2);

        String bbox = String.format("%f,%f,%f,%f", south, west, north, east);
        return bbox;
    }

    // DELETE all attractions
    @DeleteMapping("/all")
    public ResponseEntity<Map<String, Object>> deleteAllAttractions() {
        long count = attractionRepository.count();
        attractionRepository.deleteAll();
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "deletedCount", count,
                "message", "All attractions have been deleted"
        ));
    }
}