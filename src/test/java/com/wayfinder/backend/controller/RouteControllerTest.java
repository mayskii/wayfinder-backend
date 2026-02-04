package com.wayfinder.backend.controller;

import com.wayfinder.backend.model.Route;
import com.wayfinder.backend.model.User;
import com.wayfinder.backend.repository.RouteAttractionRepository;
import com.wayfinder.backend.repository.RouteRepository;
import com.wayfinder.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RouteControllerTest {

    private RouteRepository routeRepository;
    private UserRepository userRepository;
    private RouteAttractionRepository routeAttractionRepository;
    private RouteController routeController;

    @BeforeEach
    void setup() {
        routeRepository = mock(RouteRepository.class);
        userRepository = mock(UserRepository.class);
        routeAttractionRepository = mock(RouteAttractionRepository.class);
        routeController = new RouteController(routeRepository, userRepository, routeAttractionRepository);
    }

    @Test
    void testGetAllRoutes() {
        Route route = new Route();
        route.setName("Route 1");

        when(routeRepository.findAll()).thenReturn(List.of(route));

        List<Route> result = routeController.getAllRoutes();

        assertEquals(1, result.size());
        assertEquals("Route 1", result.get(0).getName());
    }

    @Test
    void testGetRouteByIdFound() {
        Route route = new Route();
        route.setId(1);
        route.setName("Route 1");

        when(routeRepository.findById(1)).thenReturn(Optional.of(route));

        ResponseEntity<?> response = routeController.getRouteById(1);

        assertEquals(200, response.getStatusCode().value());
        assertInstanceOf(Route.class, response.getBody());
        Route responseRoute = (Route) response.getBody();
        assertEquals("Route 1", responseRoute.getName());
        assertEquals(1, responseRoute.getId());
    }

    @Test
    void testGetRouteByIdNotFound() {
        when(routeRepository.findById(999)).thenReturn(Optional.empty());

        ResponseEntity<?> response = routeController.getRouteById(999);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testGetRoutesByUserFound() {
        User user = new User();
        user.setId(1);
        Route route = new Route();
        route.setUser(user);
        route.setName("User Route");

        when(userRepository.existsById(1)).thenReturn(true);
        when(routeRepository.findByUserId(1)).thenReturn(List.of(route));

        ResponseEntity<?> response = routeController.getRoutesByUser(1);

        assertEquals(200, response.getStatusCode().value());
        assertInstanceOf(List.class, response.getBody());
        List<?> routes = (List<?>) response.getBody();
        assertEquals(1, routes.size());
        assertInstanceOf(Route.class, routes.get(0));
        assertEquals("User Route", ((Route) routes.get(0)).getName());
    }

    @Test
    void testGetRoutesByUserNotFound() {
        when(userRepository.existsById(999)).thenReturn(false);

        ResponseEntity<?> response = routeController.getRoutesByUser(999);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testCreateRouteWithUser() {
        User user = new User();
        user.setId(1);
        Route route = new Route();
        route.setUser(user);
        route.setName("New Route");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(routeRepository.save(any(Route.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = routeController.createRoute(route);

        assertEquals(200, response.getStatusCode().value());
        assertInstanceOf(Route.class, response.getBody());
        Route savedRoute = (Route) response.getBody();
        assertEquals("New Route", savedRoute.getName());
        assertEquals(user, savedRoute.getUser());
        assertNotNull(savedRoute.getCreatedAt());
    }

    @Test
    void testUpdateRouteFound() {
        Route existing = new Route();
        existing.setId(1);
        existing.setName("Old Name");

        Route updated = new Route();
        updated.setName("New Name");

        when(routeRepository.findById(1)).thenReturn(Optional.of(existing));
        when(routeRepository.save(any(Route.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = routeController.updateRoute(1, updated);

        assertEquals(200, response.getStatusCode().value());
        assertInstanceOf(Route.class, response.getBody());
        Route responseRoute = (Route) response.getBody();
        assertEquals("New Name", responseRoute.getName());
    }

    @Test
    void testUpdateRouteNotFound() {
        Route updated = new Route();
        when(routeRepository.findById(999)).thenReturn(Optional.empty());

        ResponseEntity<?> response = routeController.updateRoute(999, updated);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteRouteFound() {
        when(routeRepository.existsById(1)).thenReturn(true);
        doNothing().when(routeAttractionRepository).deleteByRouteId(1);
        doNothing().when(routeRepository).deleteById(1);

        ResponseEntity<?> response = routeController.deleteRoute(1);

        assertEquals(200, response.getStatusCode().value());
        verify(routeAttractionRepository, times(1)).deleteByRouteId(1);
        verify(routeRepository, times(1)).deleteById(1);
    }

    @Test
    void testDeleteRouteNotFound() {
        when(routeRepository.existsById(999)).thenReturn(false);

        ResponseEntity<?> response = routeController.deleteRoute(999);

        assertEquals(404, response.getStatusCode().value());
    }
}