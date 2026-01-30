package com.wayfinder.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.wayfinder.backend.model.User;
import com.wayfinder.backend.repository.UserRepository;

import java.util.List;
import java.util.Map;

@RestController //Контроллер — это объект, который обрабатывает HTTP-запросы.
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository; //поле, которое хранит репозиторий для работы с базой (через Spring Data JPA).

    public UserController(UserRepository userRepository) { // внедрение зависимостей (Dependency Injection)
        this.userRepository = userRepository;
    }

    // READ ALL
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        return userRepository.findById(id)
                .<ResponseEntity<Object>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(Map.of(
                                "status", 404,
                                "message", "User not found with id " + id
                        )));
    }

    // CREATE USER OR GET EXISTING USER (from Firebase)
    @PostMapping("/create-or-get")
    public ResponseEntity<User> registerOrGetUser(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String name = body.get("name");

        if (email == null) {
            return ResponseEntity.badRequest().build();
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            return ResponseEntity.ok(user);
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName(name != null ? name : "Unknown");
        newUser.setCreatedAt(java.time.LocalDateTime.now());
        User savedUser = userRepository.save(newUser);

        return ResponseEntity.ok(savedUser);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody User updatedUser) {
        var optionalUser = userRepository.findById(id);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setName(updatedUser.getName());
            user.setEmail(updatedUser.getEmail());
            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(savedUser);
        } else {
            return ResponseEntity.status(404)
                    .body(Map.of(
                            "status", 404,
                            "message", "User not found with id " + id
                    )); // Map
        }
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Integer id) {
        userRepository.deleteById(id);
    }
}