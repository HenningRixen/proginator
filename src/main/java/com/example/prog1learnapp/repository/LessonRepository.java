package com.example.prog1learnapp.repository;

import com.example.prog1learnapp.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
}