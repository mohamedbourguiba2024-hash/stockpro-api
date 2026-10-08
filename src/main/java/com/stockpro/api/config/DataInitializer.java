package com.stockpro.api.config;

import com.stockpro.api.entity.Role;
import com.stockpro.api.entity.User;
import com.stockpro.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner createAdmin(UserRepository users,
                                  PasswordEncoder encoder,
                                  @Value("${app.admin.email}") String email,
                                  @Value("${app.admin.password}") String password) {
        return args -> {
            if (!users.existsByEmail(email)) {
                users.save(User.builder()
                        .nom("Administrateur")
                        .email(email)
                        .motDePasse(encoder.encode(password))
                        .role(Role.ADMIN)
                        .build());
            }
        };
    }
}