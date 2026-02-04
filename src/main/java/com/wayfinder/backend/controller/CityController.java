package com.wayfinder.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.wayfinder.backend.model.City;
import com.wayfinder.backend.service.CityService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cities")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    // 🔍 LOOKUP: BD → API
    @GetMapping("/lookup")
    public ResponseEntity<City> lookupCity(@RequestParam String name) {
        City city = cityService.getCityCoordinates(name);
        return ResponseEntity.ok(city);
    }

    // READ ALL
    @GetMapping
    public List<City> getAllCities() {
        return cityService.getAllCities();
    }

    // READ ONE by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getCityById(@PathVariable Long id) {
        return cityService.getCityById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(Map.of(
                                "status", 404,
                                "message", "City not found with id " + id
                        )));
    }

    // CREATE
    @PostMapping
    public City createCity(@RequestBody City city) {
        return cityService.save(city);
    }


    // DELETE
    @DeleteMapping("/{id}")
    public void deleteCity(@PathVariable Long id) {
        cityService.deleteCity(id);
    }
}
