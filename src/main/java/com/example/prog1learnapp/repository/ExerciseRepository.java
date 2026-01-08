package com.example.prog1learnapp.repository;

import com.example.prog1learnapp.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    List<Exercise> findByLessonId(Long lessonId);
    Optional<Exercise> findByTitleAndLessonId(String title, Long lessonId);
}