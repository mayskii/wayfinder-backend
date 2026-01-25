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

    // CREATE
    @PostMapping
    public Attraction createAttraction(@RequestBody Attraction attraction) {
        attraction.setCreatedAt(LocalDateTime.now());

        return saveAttractionWithCity(attraction, attraction);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateAttraction(@PathVariable Long id, @RequestBody Attraction updatedAttraction) {

        var optionalAttraction = attractionRepository.findById(id);

        return optionalAttraction
                .map(attraction -> {
                    Attraction saved = updateAttractionFields(attraction, updatedAttraction);
                    return ResponseEntity.ok((Object) saved);
                })
                .orElseGet(() -> ResponseEntity.status(404)
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

        return saveAttractionWithCity(attraction, updated);
    }

    private Attraction saveAttractionWithCity(Attraction attraction, Attraction updated) {
        if (updated.getCity() != null && updated.getCity().getId() != null) {
            City city = cityRepository.findById(updated.getCity().getId())
                    .orElseThrow(() -> new RuntimeException("City not found with id " + updated.getCity().getId()));
            attraction.setCity(city);
        }

        return attractionRepository.save(attraction);
    }

    // GET attractions from OSM for a given city
    @GetMapping("/from-osm")
    public List<Attraction> getAttractionsFromOsm(@RequestParam String cityName) {

        City city = cityRepository.findByName(cityName)
                .orElseGet(() -> cityService.getCityCoordinates(cityName));

        long existingCount = attractionRepository.countByCity(city); // считаем сколько у нас уже есть
        if (existingCount >= 50) {
            return attractionRepository.findTop50ByCity(city);
        }

        String bbox = getString(cityName, city);
        List<Map<String, Object>> osmAttractions = attractionService.getAttractions(bbox);

        return osmAttractions.stream()
                .map(data -> {

                    Long osmId = data.get("id") != null ? Long.parseLong(data.get("id").toString()) : null; // Получаем osmId
                    if (osmId == null) return null;

                    Attraction attraction = attractionRepository.findByOsmId(osmId)
                            .orElseGet(Attraction::new);

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
                .limit(50 - existingCount)
                .map(attractionRepository::save)
                .toList();
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

    // DELETE
    @DeleteMapping("/{id}")
    public void deleteAttraction(@PathVariable Long id) {
        attractionRepository.deleteById(id);
    }
}