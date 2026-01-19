package com.example.prog1learnapp.controller;

import com.example.prog1learnapp.model.User;
import com.example.prog1learnapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_DISPLAY_NAME_LENGTH = 100;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String displayName,
                           Model model) {

        // Trim input
        username = username != null ? username.trim().toLowerCase() : "";
        displayName = displayName != null ? displayName.trim() : "";

        // Validierung: Username
        if (username.isEmpty() || username.length() < MIN_USERNAME_LENGTH) {
            model.addAttribute("error", "Benutzername muss mindestens " + MIN_USERNAME_LENGTH + " Zeichen haben");
            return "register";
        }

        if (username.length() > MAX_USERNAME_LENGTH) {
            model.addAttribute("error", "Benutzername darf maximal " + MAX_USERNAME_LENGTH + " Zeichen haben");
            return "register";
        }

        // Nur alphanumerische Zeichen und Punkt/Unterstrich erlaubt
        if (!username.matches("^[a-z0-9._]+$")) {
            model.addAttribute("error", "Benutzername darf nur Kleinbuchstaben, Zahlen, Punkte und Unterstriche enthalten");
            return "register";
        }

        // Validierung: Passwort
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            model.addAttribute("error", "Passwort muss mindestens " + MIN_PASSWORD_LENGTH + " Zeichen haben");
            return "register";
        }

        // Validierung: Display Name
        if (displayName.isEmpty()) {
            model.addAttribute("error", "Vollständiger Name darf nicht leer sein");
            return "register";
        }

        if (displayName.length() > MAX_DISPLAY_NAME_LENGTH) {
            model.addAttribute("error", "Name darf maximal " + MAX_DISPLAY_NAME_LENGTH + " Zeichen haben");
            return "register";
        }

        // Prüfen ob Username bereits existiert
        if (userRepository.existsByUsername(username)) {
            log.info("Registration attempt with existing username: {}", username);
            model.addAttribute("error", "Benutzername bereits vergeben");
            return "register";
        }

        // User erstellen und speichern
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setDisplayName(displayName);
        userRepository.save(user);

        log.info("New user registered: {}", username);
        return "redirect:/login?registered";
    }
}