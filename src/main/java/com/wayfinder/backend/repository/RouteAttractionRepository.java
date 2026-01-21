package com.wayfinder.backend.repository;

import com.wayfinder.backend.model.RouteAttraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteAttractionRepository extends JpaRepository<RouteAttraction, Integer> {
}
