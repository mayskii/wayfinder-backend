package com.wayfinder.backend.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import com.wayfinder.backend.model.User;
import com.wayfinder.backend.repository.UserRepository;
import java.util.List;

@RestController
public class UserController {

    private final UserRepository userRepository; //поле, которое хранит репозиторий для работы с базой.

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/ping")
    public String ping() {
        return "hi from backend";
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
