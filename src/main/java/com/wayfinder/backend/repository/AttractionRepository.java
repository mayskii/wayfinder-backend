package com.wayfinder.backend.repository;

import com.wayfinder.backend.model.Attraction;
import com.wayfinder.backend.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttractionRepository extends JpaRepository<Attraction, Long> {

    boolean existsByOsmId(Long osmId);

    long countByCity(City city);

    List<Attraction> findTop50ByCity(City city);

    Optional<Attraction> findByOsmId(Long osmId);
}
