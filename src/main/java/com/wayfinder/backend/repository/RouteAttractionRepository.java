package com.wayfinder.backend.repository;

import com.wayfinder.backend.model.RouteAttraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface RouteAttractionRepository extends JpaRepository<RouteAttraction, Integer> {

    List<RouteAttraction> findByRouteIdOrderByPosition(Integer routeId);

    @Query("SELECT MAX(ra.position) FROM RouteAttraction ra WHERE ra.route.id = :routeId")
    Integer findMaxPositionByRouteId(@Param("routeId") Integer routeId);
}

