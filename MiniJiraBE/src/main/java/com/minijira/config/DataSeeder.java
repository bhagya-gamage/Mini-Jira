package com.minijira.config;

import com.minijira.model.Role;
import com.minijira.model.User;
import com.minijira.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedAdmin(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            if (!users.existsByRole(Role.ADMIN)) {
                User u = new User();
                u.setFullName("System Admin");
                u.setEmail("admin@minijira.com");
                u.setPassword(encoder.encode("Admin@1234"));
                u.setRole(Role.ADMIN);
                users.save(u);
            }
        };
    }
}
