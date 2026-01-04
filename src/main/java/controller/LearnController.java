package controller;

import model.User;
import model.Lesson;
import model.Exercise;
import repository.UserRepository;
import repository.LessonRepository;
import repository.ExerciseRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@Controller
public class LearnController {

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
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
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

        return "dashboard";
    }

    @GetMapping("/lesson/{id}")
    public String lesson(@PathVariable Long id, Model model, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        Lesson lesson = lessonRepository.findById(id).orElseThrow();
        List<Exercise> exercises = exerciseRepository.findByLessonId(id);

        Set<Long> completedIds = new HashSet<>(user.getCompletedExercises());

        model.addAttribute("lesson", lesson);
        model.addAttribute("exercises", exercises);
        model.addAttribute("completedIds", completedIds);

        return "lesson";
    }

    @GetMapping("/exercise/{id}")
    public String exercise(@PathVariable Long id, Model model, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        Exercise exercise = exerciseRepository.findById(id).orElseThrow();

        boolean isCompleted = user.getCompletedExercises().contains(id);

        model.addAttribute("exercise", exercise);
        model.addAttribute("completed", isCompleted);
        model.addAttribute("nextExercise", getNextExerciseId(exercise));

        return "exercise";
    }

    @PostMapping("/exercise/{id}/complete")
    @ResponseBody
    public String completeExercise(@PathVariable Long id, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        if (!user.getCompletedExercises().contains(id)) {
            user.getCompletedExercises().add(id);
            userRepository.save(user);
        }
        return "{\"status\":\"success\"}";
    }

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