package com.wayfinder.backend.controller;

import com.wayfinder.backend.model.City;
import com.wayfinder.backend.service.CityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CityControllerTest {

    private CityService cityService;
    private CityController cityController;

    @BeforeEach
    void setup() {
        // Mock service
        cityService = mock(CityService.class);
        cityController = new CityController(cityService);
    }

    @Test
    void testGetAllCities() {
        City city = new City();
        city.setId(1L);
        city.setName("London");
        city.setCountry("UK");

        when(cityService.getAllCities()).thenReturn(List.of(city));

        List<City> result = cityController.getAllCities();

        assertEquals(1, result.size());
        assertEquals("London", result.get(0).getName());
    }

    @Test
    void testGetCityByIdFound() {
        City city = new City();
        city.setId(1L);
        city.setName("Paris");
        city.setCountry("France");

        when(cityService.getCityById(1L)).thenReturn(Optional.of(city));

        ResponseEntity<?> response = cityController.getCityById(1L);

        assertEquals(200, response.getStatusCode().value());
        assertInstanceOf(City.class, response.getBody());
        City responseCity = (City) response.getBody();
        assertEquals("Paris", responseCity.getName());
    }

    @Test
    void testGetCityByIdNotFound() {
        when(cityService.getCityById(999L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = cityController.getCityById(999L);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testCreateCity() {
        City city = new City();
        city.setName("Tokyo");
        city.setCountry("Japan");

        when(cityService.save(city)).thenReturn(city);

        City result = cityController.createCity(city);

        assertEquals("Tokyo", result.getName());
        assertEquals("Japan", result.getCountry());
    }

    @Test
    void testDeleteCity() {
        doNothing().when(cityService).deleteCity(1L);

        cityController.deleteCity(1L);

        verify(cityService, times(1)).deleteCity(1L);
    }
}