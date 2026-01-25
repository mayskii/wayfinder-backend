package com.wayfinder.backend.service;

import com.wayfinder.backend.model.RouteAttraction;
import com.wayfinder.backend.repository.RouteAttractionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;


@Service
public class RouteOptimizationService {

    private final RouteAttractionRepository routeAttractionRepository;

    public RouteOptimizationService(RouteAttractionRepository routeAttractionRepository) {
        this.routeAttractionRepository = routeAttractionRepository;
    }

    public void optimizeRoute(Integer routeId) {

        List<RouteAttraction> routeAttractions =
                routeAttractionRepository.findByRouteIdOrderByPosition(routeId);

        if (routeAttractions.size() < 2) return;

        List<RouteAttraction> unvisited = new ArrayList<>(routeAttractions);
        List<RouteAttraction> optimized = new ArrayList<>();

        // стартовая точка
        RouteAttraction current = unvisited.remove(0);
        optimized.add(current);

        while (!unvisited.isEmpty()) {
            RouteAttraction nearest = null;
            double nearestDistance = Double.MAX_VALUE;

            for (RouteAttraction candidate : unvisited) {
                double distance = distanceBetween(current, candidate);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = candidate;
                }
            }

            current = nearest;
            optimized.add(nearest);
            unvisited.remove(nearest);
        }

        for (int i = 0; i < optimized.size(); i++) {
            optimized.get(i).setPosition(i + 1);
        }

        routeAttractionRepository.saveAll(optimized);
    }

    private double distanceBetween(RouteAttraction a, RouteAttraction b) {
        double lat1 = a.getAttraction().getLat();
        double lon1 = a.getAttraction().getLng();
        double lat2 = b.getAttraction().getLat();
        double lon2 = b.getAttraction().getLng();

        double R = 6371; // км
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double aa = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(aa), Math.sqrt(1 - aa));
        return R * c;
    }
}
