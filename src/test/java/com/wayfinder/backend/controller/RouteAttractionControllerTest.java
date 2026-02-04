package com.wayfinder.backend.controller;

import com.wayfinder.backend.model.Attraction;
import com.wayfinder.backend.model.Route;
import com.wayfinder.backend.model.RouteAttraction;
import com.wayfinder.backend.repository.AttractionRepository;
import com.wayfinder.backend.repository.RouteAttractionRepository;
import com.wayfinder.backend.repository.RouteRepository;
import com.wayfinder.backend.service.RouteOptimizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RouteAttractionControllerTest {

    private RouteAttractionRepository routeAttractionRepository;
    private RouteRepository routeRepository;
    private AttractionRepository attractionRepository;
    private RouteOptimizationService routeOptimizationService;
    private RouteAttractionController controller;

    @BeforeEach
    void setup() {
        routeAttractionRepository = mock(RouteAttractionRepository.class);
        routeRepository = mock(RouteRepository.class);
        attractionRepository = mock(AttractionRepository.class);
        routeOptimizationService = mock(RouteOptimizationService.class);
        controller = new RouteAttractionController(
                routeAttractionRepository, routeRepository, attractionRepository, routeOptimizationService
        );
    }

    @Test
    void testGetAll() {
        RouteAttraction ra = new RouteAttraction();
        ra.setPosition(1);
        when(routeAttractionRepository.findAll()).thenReturn(List.of(ra));

        List<RouteAttraction> result = controller.getAll();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getPosition());
    }

    @Test
    void testGetByRoute() {
        RouteAttraction ra = new RouteAttraction();
        ra.setPosition(1);
        when(routeAttractionRepository.findByRouteIdOrderByPosition(10)).thenReturn(List.of(ra));

        List<RouteAttraction> result = controller.getByRoute(10);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getPosition());
    }

    @Test
    void testCreateRouteAttraction() {
        Route route = new Route();
        route.setId(1);
        Attraction attraction = new Attraction();
        attraction.setId(2L);
        RouteAttraction ra = new RouteAttraction();
        ra.setRoute(route);
        ra.setAttraction(attraction);

        when(routeRepository.findById(1)).thenReturn(Optional.of(route));
        when(attractionRepository.findById(2L)).thenReturn(Optional.of(attraction));
        when(routeAttractionRepository.findMaxPositionByRouteId(1)).thenReturn(0);
        when(routeAttractionRepository.save(any(RouteAttraction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = controller.create(ra);

        assertEquals(200, response.getStatusCode().value());
        assertInstanceOf(RouteAttraction.class, response.getBody());
        RouteAttraction saved = (RouteAttraction) response.getBody();
        assertEquals(route, saved.getRoute());
        assertEquals(attraction, saved.getAttraction());
        assertEquals(1, saved.getPosition());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void testUpdateRouteAttractionFound() {
        Route route = new Route();
        route.setId(1);
        Attraction attraction = new Attraction();
        attraction.setId(2L);

        RouteAttraction existing = new RouteAttraction();
        existing.setId(5);
        existing.setRoute(route);
        existing.setAttraction(attraction);

        RouteAttraction updated = new RouteAttraction();
        updated.setRoute(route);
        updated.setAttraction(attraction);
        updated.setPosition(3);

        when(routeAttractionRepository.findById(5)).thenReturn(Optional.of(existing));
        when(routeRepository.findById(1)).thenReturn(Optional.of(route));
        when(attractionRepository.findById(2L)).thenReturn(Optional.of(attraction));
        when(routeAttractionRepository.save(any(RouteAttraction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = controller.update(5, updated);

        assertEquals(200, response.getStatusCode().value());
        assertInstanceOf(RouteAttraction.class, response.getBody());
        RouteAttraction saved = (RouteAttraction) response.getBody();
        assertEquals(3, saved.getPosition());
        assertEquals(route, saved.getRoute());
        assertEquals(attraction, saved.getAttraction());
    }

    @Test
    void testUpdateRouteAttractionNotFound() {
        RouteAttraction updated = new RouteAttraction();
        ResponseEntity<Object> response = controller.update(999, updated);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testUpdatePositionValid() {
        Route route = new Route();
        route.setId(1);

        RouteAttraction ra1 = new RouteAttraction();
        ra1.setId(1);
        ra1.setRoute(route);
        ra1.setPosition(1);

        RouteAttraction ra2 = new RouteAttraction();
        ra2.setId(2);
        ra2.setRoute(route);
        ra2.setPosition(2);

        List<RouteAttraction> mutableList = new ArrayList<>();
        mutableList.add(ra1);
        mutableList.add(ra2);

        when(routeAttractionRepository.findById(1)).thenReturn(Optional.of(ra1));
        when(routeAttractionRepository.findByRouteIdOrderByPosition(1))
                .thenReturn(mutableList);

        ResponseEntity<Object> response = controller.updatePosition(1, 2);

        assertEquals(200, response.getStatusCode().value());
        verify(routeAttractionRepository, times(1)).saveAll(anyList());
    }
    @Test
    void testUpdatePositionInvalid() {
        Route route = new Route();
        route.setId(1);
        RouteAttraction ra = new RouteAttraction();
        ra.setId(1);
        ra.setRoute(route);

        when(routeAttractionRepository.findById(1)).thenReturn(Optional.of(ra));
        when(routeAttractionRepository.findByRouteIdOrderByPosition(1)).thenReturn(List.of(ra));

        ResponseEntity<Object> response = controller.updatePosition(1, 5);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testDelete() {
        doNothing().when(routeAttractionRepository).deleteById(1);
        controller.delete(1);
        verify(routeAttractionRepository, times(1)).deleteById(1);
    }

    @Test
    void testOptimizeRoute() {
        RouteAttraction ra1 = new RouteAttraction();
        ra1.setId(1);
        RouteAttraction ra2 = new RouteAttraction();
        ra2.setId(2);

        when(routeAttractionRepository.findByRouteIdOrderByPosition(100))
                .thenReturn(List.of(ra1, ra2));

        ResponseEntity<List<RouteAttraction>> response = controller.optimizeRoute(100);

        assertEquals(200, response.getStatusCode().value());
        assert response.getBody() != null;
        assertEquals(2, response.getBody().size());

        verify(routeOptimizationService, times(1)).optimizeRoute(100);
    }
}

