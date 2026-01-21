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

    @Column(name = "route_id")
    private Integer routeId;   //TODO надо добавить связи  - foreign key -  links to routes.id

    @Column(name = "attraction_id")
    private Long attractionId;    //TODO надо добавить связи  - foreign key links to attractions.id

    private Integer position;
    private LocalDateTime createdAt;

    public RouteAttraction() {}

}
