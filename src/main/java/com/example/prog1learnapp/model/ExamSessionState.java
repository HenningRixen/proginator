package com.example.prog1learnapp.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ExamSessionState implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String attemptId = UUID.randomUUID().toString();
    private List<Long> selectedExerciseIds = new ArrayList<>();
    private Set<Long> completedExerciseIds = new HashSet<>();
    private Instant createdAt = Instant.now();

    public ExamSessionState() {
    }

    public ExamSessionState(List<Long> selectedExerciseIds) {
        this.attemptId = UUID.randomUUID().toString();
        this.selectedExerciseIds = new ArrayList<>(selectedExerciseIds);
        this.completedExerciseIds = new HashSet<>();
        this.createdAt = Instant.now();
    }

    public String getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(String attemptId) {
        this.attemptId = attemptId;
    }

    public List<Long> getSelectedExerciseIds() {
        return selectedExerciseIds;
    }

    public void setSelectedExerciseIds(List<Long> selectedExerciseIds) {
        this.selectedExerciseIds = selectedExerciseIds;
    }

    public Set<Long> getCompletedExerciseIds() {
        return completedExerciseIds;
    }

    public void setCompletedExerciseIds(Set<Long> completedExerciseIds) {
        this.completedExerciseIds = completedExerciseIds;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isExerciseSelected(Long exerciseId) {
        return selectedExerciseIds.contains(exerciseId);
    }

    public void markCompleted(Long exerciseId) {
        completedExerciseIds.add(exerciseId);
    }
}
