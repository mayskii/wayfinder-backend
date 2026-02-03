package com.wayfinder.backend.controller;

import com.wayfinder.backend.model.City;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CityControllerTest {

    // Простая заглушка вместо настоящего сервиса
    static class CityServiceStub extends com.wayfinder.backend.service.CityService {
        private final Map<Long, City> db = new HashMap<>();

        CityServiceStub() {
            super(null, null); // не используем настоящий репозиторий и RestTemplate
            City city = new City();
            city.setId(1L);
            city.setName("TestCity");
            db.put(1L, city);
        }

        @Override
        public Optional<City> getCityById(Long id) {
            return Optional.ofNullable(db.get(id));
        }
    }

    @Test
    void testGetCityById_found() {
        CityController cityController = new CityController(new CityServiceStub());

        ResponseEntity<?> response = cityController.getCityById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(City.class, response.getBody());

        City city = (City) response.getBody();
        assertEquals(1L, city.getId());
        assertEquals("TestCity", city.getName());
    }

    @Test
    void testGetCityById_notFound() {
        CityController cityController = new CityController(new CityServiceStub());

        ResponseEntity<?> response = cityController.getCityById(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(404, body.get("status"));
        assertEquals("City not found with id 999", body.get("message"));
    }
}