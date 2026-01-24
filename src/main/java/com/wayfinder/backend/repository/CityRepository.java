package com.wayfinder.backend.repository;

import com.wayfinder.backend.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findByName(String name);

    Optional<City> findByOsmId(Long osmId);

}
