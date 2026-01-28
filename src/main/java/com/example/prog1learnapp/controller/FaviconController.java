package com.example.prog1learnapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller für favicon.ico Anfragen.
 * Liefert eine leere Antwort, um Browser-Fehler zu vermeiden.
 */
@Controller
public class FaviconController {

    @GetMapping("favicon.ico")
    @ResponseBody
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
