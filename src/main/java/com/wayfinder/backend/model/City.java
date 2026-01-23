package com.wayfinder.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "cities")
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // INTERNAL ID (Hibernate)

    @Column(name = "osm_id", unique = true)
    private Long osmId;  // OSM ID (optional, external)

    private String name;
    private String country;

    private Double lat;
    private Double lng;

    private String bbox;  //TODO надо поманять на правильный тип

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    public City() {}

}
