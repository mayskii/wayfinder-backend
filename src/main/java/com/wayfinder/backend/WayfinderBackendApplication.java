package com.wayfinder.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.wayfinder.backend.model.User;
import com.wayfinder.backend.repository.UserRepository;

@SpringBootApplication
public class WayfinderBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(WayfinderBackendApplication.class, args);
    }

    @Bean // @Bean — говорит Spring: «Создай этот объект и управляй им».
    CommandLineRunner run(UserRepository userRepository) {
        return args -> {
            User user = new User();
            user.setName("Maya");
            user.setEmail("maya" + System.currentTimeMillis() + "@example.com");
            user.setCreatedAt(java.time.LocalDateTime.now());
            userRepository.save(user);  // сохраняем в базу
            System.out.println("User saved!");
        };
    }
}