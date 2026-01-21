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
    @Column(name = "osm_id")
    private Long osmId;  // bigint в БД → Long в Java

    @Column(name = "city_id")
    private Long cityId;  //TODO надо добавить связи ибо это фореин ключ на cities.id

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
