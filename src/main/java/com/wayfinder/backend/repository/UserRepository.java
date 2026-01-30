package com.wayfinder.backend.repository;

import com.wayfinder.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;  //интерфейс JPA, который предоставляет готовые методы для работы с базой
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Репозиторий — это объект, который умеет общаться с базой.
public interface UserRepository extends JpaRepository<User, Integer>{

    Optional<User> findByEmail(String email);
}
