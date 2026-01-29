package com.example.prog1learnapp.controller;

import com.example.prog1learnapp.model.Feedback;
import com.example.prog1learnapp.repository.FeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FeedbackController {

    private static final Logger log = LoggerFactory.getLogger(FeedbackController.class);
    private static final int MAX_TEXT_LENGTH = 2000;

    private final FeedbackRepository feedbackRepository;

    public FeedbackController(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @GetMapping("/feedback")
    public String feedbackForm(Model model) {
        // Add empty feedback object for form binding (optional)
        model.addAttribute("maxLength", MAX_TEXT_LENGTH);
        return "feedback";
    }

    @PostMapping("/feedback")
    public String submitFeedback(
            @RequestParam String text,
            @RequestParam Integer rating,
            RedirectAttributes redirectAttributes) {

        // Basic validation
        if (text == null || text.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Bitte geben Sie Feedback-Text ein.");
            return "redirect:/feedback";
        }

        if (text.length() > MAX_TEXT_LENGTH) {
            redirectAttributes.addFlashAttribute("error", 
                "Feedback-Text darf maximal " + MAX_TEXT_LENGTH + " Zeichen haben.");
            return "redirect:/feedback";
        }

        if (rating == null || rating < 1 || rating > 5) {
            redirectAttributes.addFlashAttribute("error", "Bitte wählen Sie eine Bewertung zwischen 1 und 5.");
            return "redirect:/feedback";
        }

        // Save feedback
        Feedback feedback = new Feedback(text.trim(), rating);
        feedbackRepository.save(feedback);

        log.info("New feedback submitted with rating {}", rating);
        redirectAttributes.addFlashAttribute("success", 
            "Vielen Dank für Ihr Feedback! Ihre Bewertung wurde gespeichert.");
        
        return "redirect:/feedback";
    }
}