package com.wayfinder.backend.model;

import jakarta.persistence.*;  // Импорт JPA (@Entity, @Table, @Id, @GeneratedValue...) JPA — это библиотека связывает классы с таблицами в базе данных (ORM)
import java.time.LocalDateTime;

@Entity  // Говорит Spring/Hibernate: это сущность, её нужно хранить в базе данных
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    public String name;
    public String email;
    public LocalDateTime createdAt;
}
