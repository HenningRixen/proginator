package com.example.prog1learnapp.config;

import com.example.prog1learnapp.model.User;
import com.example.prog1learnapp.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Benutzername zu Kleinbuchstaben konvertieren für Konsistenz mit Registrierung
        String normalizedUsername = username != null ? username.trim().toLowerCase() : "";

        User user = userRepository.findByUsername(normalizedUsername)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + normalizedUsername));
        return org.springframework.security.core.userdetails.User
            .withUsername(user.getUsername())
            .password(user.getPassword())
            .roles("USER")
            .build();
    }
}