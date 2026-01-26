package com.example.prog1learnapp.controller;

import com.example.prog1learnapp.model.User;
import com.example.prog1learnapp.model.Lesson;
import com.example.prog1learnapp.model.Exercise;
import com.example.prog1learnapp.repository.UserRepository;
import com.example.prog1learnapp.repository.LessonRepository;
import com.example.prog1learnapp.repository.ExerciseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@Controller
public class LearnController {

    private static final Logger log = LoggerFactory.getLogger(LearnController.class);

    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final ExerciseRepository exerciseRepository;

    public LearnController(UserRepository userRepository,
                           LessonRepository lessonRepository,
                           ExerciseRepository exerciseRepository) {
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User user = findUserByPrincipal(principal);
        if (user == null) {
            log.warn("User not found for principal: {}", principal.getName());
            return "redirect:/login";
        }

        List<Lesson> lessons = lessonRepository.findAll();
        List<Exercise> allExercises = exerciseRepository.findAll();

        Set<Long> completedIds = new HashSet<>(user.getCompletedExercises());

        // Calculate progress per lesson
        Map<Long, Integer> lessonProgress = new HashMap<>();
        for (Lesson lesson : lessons) {
            int total = (int) allExercises.stream()
                    .filter(e -> e.getLesson().getId().equals(lesson.getId()))
                    .count();
            int completed = (int) allExercises.stream()
                    .filter(e -> e.getLesson().getId().equals(lesson.getId()))
                    .filter(e -> completedIds.contains(e.getId()))
                    .count();
            lessonProgress.put(lesson.getId(), total > 0 ? (completed * 100) / total : 0);
        }

        int totalExercises = allExercises.size();
        int completedExercises = completedIds.size();
        int overallProgress = totalExercises > 0 ? (completedExercises * 100) / totalExercises : 0;

        model.addAttribute("user", user);
        model.addAttribute("lessons", lessons);
        model.addAttribute("lessonProgress", lessonProgress);
        model.addAttribute("overallProgress", overallProgress);
        model.addAttribute("completedCount", completedExercises);
        model.addAttribute("totalCount", totalExercises);

        log.debug("Dashboard loaded for user '{}' with {}% progress", user.getUsername(), overallProgress);
        return "dashboard";
    }

    @GetMapping("/lesson/{id}")
    public String lesson(@PathVariable Long id, Model model, Principal principal) {
        User user = findUserByPrincipal(principal);
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Lesson> lessonOpt = lessonRepository.findById(id);
        if (lessonOpt.isEmpty()) {
            log.warn("Lesson with id {} not found", id);
            return "error/404";
        }

        Lesson lesson = lessonOpt.get();
        List<Exercise> exercises = exerciseRepository.findByLessonId(id);
        Set<Long> completedIds = new HashSet<>(user.getCompletedExercises());

        model.addAttribute("lesson", lesson);
        model.addAttribute("exercises", exercises);
        model.addAttribute("completedIds", completedIds);

        log.debug("Lesson {} loaded with {} exercises", id, exercises.size());
        return "lesson";
    }

    @GetMapping("/exercise/{id}")
    public String exercise(@PathVariable Long id, Model model, Principal principal) {
        User user = findUserByPrincipal(principal);
        boolean isCompleted = false;
        
        Optional<Exercise> exerciseOpt = exerciseRepository.findById(id);
        if (exerciseOpt.isEmpty()) {
            log.warn("Exercise with id {} not found", id);
            return "error/404";
        }

        Exercise exercise = exerciseOpt.get();
        
        if (user != null) {
            isCompleted = user.getCompletedExercises().contains(id);
        }

        model.addAttribute("exercise", exercise);
        model.addAttribute("completed", isCompleted);
        model.addAttribute("nextExercise", getNextExerciseId(exercise));
        model.addAttribute("authenticated", user != null);

        return "exercise";
    }

    @PostMapping("/exercise/{id}/complete")
    @ResponseBody
    public ResponseEntity<Map<String, String>> completeExercise(@PathVariable Long id, Principal principal) {
        User user = findUserByPrincipal(principal);
        if (user == null) {
            log.warn("Unauthorized attempt to complete exercise {}", id);
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "Nicht autorisiert"));
        }

        Optional<Exercise> exerciseOpt = exerciseRepository.findById(id);
        if (exerciseOpt.isEmpty()) {
            log.warn("Exercise {} not found for completion", id);
            return ResponseEntity.status(404).body(Map.of("status", "error", "message", "Aufgabe nicht gefunden"));
        }

        if (!user.getCompletedExercises().contains(id)) {
            user.getCompletedExercises().add(id);
            userRepository.save(user);
            log.info("User '{}' completed exercise {}", user.getUsername(), id);
        }

        return ResponseEntity.ok(Map.of("status", "success"));
    }

    /**
     * Findet einen Benutzer anhand des Principal-Objekts.
     */
    private User findUserByPrincipal(Principal principal) {
        if (principal == null) {
            return null;
        }
        return userRepository.findByUsername(principal.getName()).orElse(null);
    }

    /**
     * Ermittelt die ID der nächsten Übung in derselben Lektion.
     */
    private Long getNextExerciseId(Exercise current) {
        List<Exercise> exercises = exerciseRepository.findByLessonId(current.getLesson().getId());
        for (int i = 0; i < exercises.size(); i++) {
            if (exercises.get(i).getId().equals(current.getId()) && i + 1 < exercises.size()) {
                return exercises.get(i + 1).getId();
            }
        }
        return null;
    }
}