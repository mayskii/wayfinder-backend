package com.wayfinder.backend.service;

import com.wayfinder.backend.exception.CityNotFoundException;
import com.wayfinder.backend.model.City;
import com.wayfinder.backend.repository.CityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CityServiceTest {

    private CityRepository cityRepository;
    private CityService cityService;

    @BeforeEach
    void setup() {
        cityRepository = mock(CityRepository.class);
        cityService = new CityService(cityRepository, mock(RestTemplate.class));
    }

    @Test
    void getCityCoordinates_cityExistsInDb() {
        City city = new City();
        city.setName("Paris");

        when(cityRepository.findByName("Paris"))
                .thenReturn(Optional.of(city));

        City result = cityService.getCityCoordinates("Paris");

        assertEquals("Paris", result.getName());
    }

    @Test
    void getCityCoordinates_cityNotFound() {
        when(cityRepository.findByName("Nowhere"))
                .thenReturn(Optional.empty());

        assertThrows(
                CityNotFoundException.class,
                () -> cityService.getCityCoordinates("Nowhere")
        );
    }

    @Test
    void getAllCities() {
        when(cityRepository.findAll())
                .thenReturn(List.of(new City()));

        assertEquals(1, cityService.getAllCities().size());
    }
}