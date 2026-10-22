package com.wayfinder.backend.controller;

import com.wayfinder.backend.model.Attraction;
import com.wayfinder.backend.repository.AttractionRepository;
import com.wayfinder.backend.repository.CityRepository;
import com.wayfinder.backend.service.AttractionService;
import com.wayfinder.backend.service.CityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttractionControllerTest {

    private AttractionRepository attractionRepository;

    private AttractionController attractionController;

    @BeforeEach
    void setup() {
        attractionRepository = mock(AttractionRepository.class);

        CityRepository cityRepository = mock(CityRepository.class);
        AttractionService attractionService = mock(AttractionService.class);
        CityService cityService = mock(CityService.class);

        attractionController = new AttractionController(
                attractionRepository,
                cityRepository,
                attractionService,
                cityService
        );
    }

    @Test
    void testGetAllAttractions() {
        Attraction attraction = new Attraction();
        attraction.setId(1L);
        attraction.setName("Museum");

        when(attractionRepository.findAll()).thenReturn(List.of(attraction));

        List<Attraction> result = attractionController.getAllAttractions();

        assertEquals(1, result.size());
        assertEquals("Museum", result.get(0).getName());
    }

    @Test
    void testGetAttractionByIdFound() {
        Attraction attraction = new Attraction();
        attraction.setId(1L);
        attraction.setName("Park");

        when(attractionRepository.findById(1L))
                .thenReturn(Optional.of(attraction));

        ResponseEntity<?> response = attractionController.getAttractionById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(Attraction.class, response.getBody());

        Attraction body = (Attraction) response.getBody();
        assertEquals("Park", body.getName());
    }

    @Test
    void testGetAttractionByIdNotFound() {
        when(attractionRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response =
                attractionController.getAttractionById(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());
    }

}