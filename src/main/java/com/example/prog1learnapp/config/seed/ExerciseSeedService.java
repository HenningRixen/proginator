package com.example.prog1learnapp.config.seed;

import com.example.prog1learnapp.model.Exercise;
import com.example.prog1learnapp.repository.ExerciseRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExerciseSeedService {

    private static final Logger log =
            LoggerFactory.getLogger(ExerciseSeedService.class);

    private final ExerciseRepository exerciseRepository;

    public ExerciseSeedService(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    /**
     * Speichert eine Übung nur, wenn sie noch nicht existiert.
     * Muss public sein, damit @Transactional greift.
     */
    @Transactional
    public void saveExerciseIfNotExists(Exercise exercise) {

        boolean exists = exerciseRepository
                .findByLessonId(exercise.getLesson().getId())
                .stream()
                .anyMatch(e -> e.getTitle().equals(exercise.getTitle()));

        if (!exists) {
            exerciseRepository.save(exercise);
            log.debug(
                    "Exercise '{}' saved for lesson {}",
                    exercise.getTitle(),
                    exercise.getLesson().getId()
            );
        }
    }
}
