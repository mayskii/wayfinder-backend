package com.wayfinder.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.wayfinder.backend.model.City;
import com.wayfinder.backend.repository.CityRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cities")
public class CityController {

    private final CityRepository cityRepository;

    public CityController(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    // READ ALL
    @GetMapping
    public List<City> getAllCities() {
        return cityRepository.findAll();
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<?> getCityById(@PathVariable Long id) {
        return cityRepository.findById(id)
                .<ResponseEntity<Object>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(Map.of(
                                "status", 404,
                                "message", "City not found with id " + id
                        )));
    }

    // CREATE
    @PostMapping
    public City createCity(@RequestBody City city) {
        return cityRepository.save(city);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCity(@PathVariable Long id, @RequestBody City updatedCity) {

        var optionalCity = cityRepository.findById(id);

        if (optionalCity.isPresent()) {
            City city = optionalCity.get();

            city.setName(updatedCity.getName());
            city.setCountry(updatedCity.getCountry());
            city.setLat(updatedCity.getLat());
            city.setLng(updatedCity.getLng());
            city.setBbox(updatedCity.getBbox());
            city.setLastUpdated(updatedCity.getLastUpdated());

            City savedCity = cityRepository.save(city);
            return ResponseEntity.ok(savedCity);

        } else {
            return ResponseEntity.status(404)
                    .body(Map.of(
                            "status", 404,
                            "message", "City not found with id " + id
                    ));
        }
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void deleteCity(@PathVariable Long id) {
        cityRepository.deleteById(id);
    }


}
