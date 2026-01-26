package com.example.prog1learnapp.repository;

import com.example.prog1learnapp.model.CodeExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CodeExecutionRepository extends JpaRepository<CodeExecution, Long> {
    List<CodeExecution> findByExerciseId(Long exerciseId);
    List<CodeExecution> findByUserId(Long userId);
    List<CodeExecution> findByExerciseIdAndUserId(Long exerciseId, Long userId);
    Optional<CodeExecution> findFirstByExerciseIdAndUserIdOrderByExecutionTimeDesc(Long exerciseId, Long userId);
    List<CodeExecution> findByStatus(String status);
}