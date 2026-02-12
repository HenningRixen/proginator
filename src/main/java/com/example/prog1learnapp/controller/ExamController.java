package com.example.prog1learnapp.controller;

import com.example.prog1learnapp.model.ExamSessionState;
import com.example.prog1learnapp.model.Exercise;
import com.example.prog1learnapp.service.ExamSelectionException;
import com.example.prog1learnapp.service.ExamSelectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpSession;

@Controller
public class ExamController {

    public static final String EXAM_SESSION_KEY = "EXAM_SESSION_STATE";

    private final ExamSelectionService examSelectionService;

    public ExamController(ExamSelectionService examSelectionService) {
        this.examSelectionService = examSelectionService;
    }

    @GetMapping("/exam/start")
    public String startExam(Principal principal, HttpSession session) {
        if (principal == null) {
            return "redirect:/login";
        }

        ExamSessionState examState = examSelectionService.startNewExam();
        session.setAttribute(EXAM_SESSION_KEY, examState);
        return "redirect:/exam";
    }

    @GetMapping("/exam")
    public String exam(Model model, Principal principal, HttpSession session) {
        if (principal == null) {
            return "redirect:/login";
        }

        Object stateObj = session.getAttribute(EXAM_SESSION_KEY);
        if (!(stateObj instanceof ExamSessionState examState)) {
            return "redirect:/exam/start";
        }

        try {
            List<Exercise> examExercises = examSelectionService.resolveSelectedExercises(examState);
            Set<Long> examCompletedIds = new HashSet<>(examState.getCompletedExerciseIds());
            model.addAttribute("examExercises", examExercises);
            model.addAttribute("examCompletedIds", examCompletedIds);
            return "exam";
        } catch (ExamSelectionException ex) {
            return "redirect:/exam/start";
        }
    }

    @PostMapping("/exam/{exerciseId}/complete")
    @ResponseBody
    public ResponseEntity<Map<String, String>> completeExamExercise(@PathVariable Long exerciseId,
                                                                    Principal principal,
                                                                    HttpSession session) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "Nicht autorisiert"));
        }

        Object stateObj = session.getAttribute(EXAM_SESSION_KEY);
        if (!(stateObj instanceof ExamSessionState examState)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Keine aktive Exam-Session gefunden"));
        }

        try {
            examSelectionService.markCompleted(examState, exerciseId);
            session.setAttribute(EXAM_SESSION_KEY, examState);
            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", ex.getMessage()));
        }
    }
}
