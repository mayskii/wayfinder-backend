package com.wayfinder.backend.model;

import jakarta.persistence.*;  // Импорт JPA (@Entity, @Table, @Id, @GeneratedValue...) JPA — это библиотека связывает классы с таблицами в базе данных (ORM)
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity  // Говорит Spring/Hibernate: это сущность(можель), её нужно хранить в базе данных
@Getter
@Setter
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String email;
    private LocalDateTime createdAt;

    public User() {}

}
