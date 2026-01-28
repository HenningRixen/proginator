package com.example.prog1learnapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.NoSuchElementException;

/**
 * Globaler Exception Handler für die Anwendung.
 * Fängt Exceptions ab und leitet auf entsprechende Fehlerseiten um.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Behandelt NoSuchElementException (z.B. wenn Entity nicht gefunden wird).
     */
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoSuchElementException ex, Model model) {
        log.warn("Resource not found: {}", ex.getMessage());
        model.addAttribute("error", "Die angeforderte Ressource wurde nicht gefunden.");
        return "error/404";
    }

    /**
     * Behandelt NoHandlerFoundException (404 für unbekannte URLs).
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoHandlerFound(NoHandlerFoundException ex) {
        log.warn("No handler found for: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        return "error/404";
    }

    /**
     * Behandelt NoResourceFoundException (fehlende statische Ressourcen).
     * Ignoriert favicon.ico-Anfragen, die von Browsern automatisch gestellt werden.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResourceFound(NoResourceFoundException ex) {
        String resourcePath = ex.getResourcePath();
        // Ignoriere favicon.ico - Browser fragt das automatisch an
        if (resourcePath.contains("favicon.ico")) {
            log.debug("Favicon not found - this is expected if no favicon is configured");
        } else {
            log.warn("Static resource not found: {}", resourcePath);
        }
        return "error/404";
    }

    /**
     * Behandelt IllegalArgumentException (ungültige Parameter).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException ex, Model model) {
        log.warn("Bad request: {}", ex.getMessage());
        model.addAttribute("error", "Ungültige Anfrage: " + ex.getMessage());
        return "error/404";
    }

    /**
     * Behandelt alle anderen unerwarteten Exceptions.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model) {
        log.error("Unexpected error occurred", ex);
        model.addAttribute("error", "Ein unerwarteter Fehler ist aufgetreten. Bitte versuche es später erneut.");
        return "error/500";
    }
}
