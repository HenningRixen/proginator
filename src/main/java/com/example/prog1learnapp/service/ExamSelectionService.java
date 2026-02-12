package com.example.prog1learnapp.service;

import com.example.prog1learnapp.model.ExamSessionState;
import com.example.prog1learnapp.model.Exercise;
import com.example.prog1learnapp.repository.ExerciseRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ExamSelectionService {

    private static final List<Long> BAND_3_TO_5 = List.of(3L, 4L, 5L);
    private static final List<Long> BAND_6_TO_8 = List.of(6L, 7L, 8L);
    private static final List<Long> BAND_9_TO_11 = List.of(9L, 10L, 11L);

    private final ExerciseRepository exerciseRepository;

    public ExamSelectionService(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    public ExamSessionState startNewExam() {
        List<Long> selectedIds = new ArrayList<>(3);
        selectedIds.add(selectRandomExerciseForBand(BAND_3_TO_5, "3-5").getId());
        selectedIds.add(selectRandomExerciseForBand(BAND_6_TO_8, "6-8").getId());
        selectedIds.add(selectRandomExerciseForBand(BAND_9_TO_11, "9-11").getId());
        return new ExamSessionState(selectedIds);
    }

    public List<Exercise> resolveSelectedExercises(ExamSessionState state) {
        if (state == null || state.getSelectedExerciseIds() == null || state.getSelectedExerciseIds().isEmpty()) {
            throw new ExamSelectionException("Keine aktive Exam-Session gefunden.");
        }

        List<Long> selectedIds = state.getSelectedExerciseIds();
        List<Exercise> fetched = exerciseRepository.findAllById(selectedIds);
        Map<Long, Exercise> byId = new HashMap<>();
        for (Exercise exercise : fetched) {
            byId.put(exercise.getId(), exercise);
        }

        List<Exercise> orderedExercises = new ArrayList<>(selectedIds.size());
        for (Long id : selectedIds) {
            Exercise exercise = byId.get(id);
            if (exercise == null) {
                throw new ExamSelectionException("Ausgewaehlte Aufgabe mit ID " + id + " wurde nicht gefunden.");
            }
            orderedExercises.add(exercise);
        }
        return orderedExercises;
    }

    public void markCompleted(ExamSessionState state, Long exerciseId) {
        if (state == null || exerciseId == null) {
            throw new IllegalArgumentException("Exam-Session und Exercise-ID muessen gesetzt sein.");
        }
        if (!state.isExerciseSelected(exerciseId)) {
            throw new IllegalArgumentException("Aufgabe ist nicht Teil der aktiven Exam-Session.");
        }
        state.markCompleted(exerciseId);
    }

    private Exercise selectRandomExerciseForBand(List<Long> lessonIds, String bandLabel) {
        List<Exercise> candidates = exerciseRepository.findByLessonIdIn(lessonIds);
        if (candidates.isEmpty()) {
            throw new ExamSelectionException(
                    "Keine Aufgaben verfuegbar im Bereich " + bandLabel + ". Exam kann nicht gestartet werden.");
        }
        int randomIndex = ThreadLocalRandom.current().nextInt(candidates.size());
        return candidates.get(randomIndex);
    }
}
