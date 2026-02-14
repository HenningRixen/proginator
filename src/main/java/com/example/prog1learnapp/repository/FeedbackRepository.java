package com.example.prog1learnapp.repository;

import com.example.prog1learnapp.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    // No custom queries needed for now
}