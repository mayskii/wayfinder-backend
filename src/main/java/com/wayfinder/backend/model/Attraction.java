package com.wayfinder.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "attractions")

public class Attraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // INTERNAL ID (Hibernate)

    @Column(name = "osm_id", unique = true)
    private Long osmId;  // OSM ID (external)

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "city_id") // связь с колонкой city_id в таблице
    private City city;

    private String name;
    private String category;
    private Double lat;
    private Double lng;
    private String website;
    private String wheelchair;
    private String fee;
    private LocalDateTime createdAt;

    public Attraction() {}
}
