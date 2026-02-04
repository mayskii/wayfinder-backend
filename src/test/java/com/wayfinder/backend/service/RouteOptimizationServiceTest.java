package com.wayfinder.backend.service;

import com.wayfinder.backend.model.Attraction;
import com.wayfinder.backend.model.RouteAttraction;
import com.wayfinder.backend.repository.RouteAttractionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

class RouteOptimizationServiceTest {

    private RouteAttractionRepository repository;
    private RouteOptimizationService service;

    @BeforeEach
    void setup() {
        repository = mock(RouteAttractionRepository.class);
        service = new RouteOptimizationService(repository);
    }

    @Test
    void testOptimizeRoute() {
        Attraction a1 = new Attraction(); a1.setLat(0.0); a1.setLng(0.0);
        Attraction a2 = new Attraction(); a2.setLat(0.0); a2.setLng(1.0);
        Attraction a3 = new Attraction(); a3.setLat(1.0); a3.setLng(0.0);

        RouteAttraction r1 = new RouteAttraction(); r1.setPosition(1); r1.setAttraction(a1);
        RouteAttraction r2 = new RouteAttraction(); r2.setPosition(2); r2.setAttraction(a2);
        RouteAttraction r3 = new RouteAttraction(); r3.setPosition(3); r3.setAttraction(a3);

        List<RouteAttraction> routeAttractions = List.of(r1, r2, r3);

        when(repository.findByRouteIdOrderByPosition(1)).thenReturn(routeAttractions);

        service.optimizeRoute(1);

        verify(repository, times(1)).saveAll(any());

        assert r1.getPosition() == 1;
        assert r2.getPosition() == 2 || r2.getPosition() == 3;
        assert r3.getPosition() == 2 || r3.getPosition() == 3;
    }
}