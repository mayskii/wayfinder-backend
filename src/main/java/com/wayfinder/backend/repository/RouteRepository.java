package com.wayfinder.backend.repository;

import com.wayfinder.backend.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Integer> {

    List<Route> findByUserId(Integer userId);

}
