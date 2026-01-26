package com.example.prog1learnapp.config;

import com.example.prog1learnapp.model.User;
import com.example.prog1learnapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class TestUserInitializer {

    private static final Logger log = LoggerFactory.getLogger(TestUserInitializer.class);

    @Bean
    public CommandLineRunner initTestUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if test user already exists
            if (userRepository.findByUsername("test").isEmpty()) {
                User testUser = new User();
                testUser.setUsername("test");
                testUser.setPassword(passwordEncoder.encode("test123"));
                testUser.setDisplayName("Test User");
                
                userRepository.save(testUser);
                log.info("Created test user: test/test123");
            }
            
            // Check if admin user exists
            if (userRepository.findByUsername("admin").isEmpty()) {
                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setDisplayName("Administrator");
                
                userRepository.save(adminUser);
                log.info("Created admin user: admin/admin123");
            }
        };
    }
}