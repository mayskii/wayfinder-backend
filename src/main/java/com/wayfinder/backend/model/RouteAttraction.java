package com.wayfinder.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "route_attractions")

public class RouteAttraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;   // foreign key -  links to routes.id

    @ManyToOne
    @JoinColumn(name = "attraction_id", nullable = false) // связь с таблицей attractions
    private Attraction attraction;    //foreign key links to attractions.id

    private Integer position;
    private LocalDateTime createdAt;

    public RouteAttraction() {}

}
