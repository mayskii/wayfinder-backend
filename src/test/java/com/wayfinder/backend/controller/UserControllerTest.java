package com.wayfinder.backend.controller;

import com.wayfinder.backend.model.User;
import com.wayfinder.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private UserRepository userRepository;
    private UserController userController;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        userController = new UserController(userRepository);
    }

    @Test
    void testGetAllUsers() {
        User user = new User();
        user.setId(1);
        user.setName("John");
        user.setEmail("john@example.com");

        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> result = userController.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getName());
    }

    @Test
    void testGetUserByIdNotFound() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.getUserById(999);

        assertEquals(404, response.getStatusCode().value());
    }
}