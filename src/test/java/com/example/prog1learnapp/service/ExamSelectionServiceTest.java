package com.example.prog1learnapp.service;

import com.example.prog1learnapp.model.ExamSessionState;
import com.example.prog1learnapp.model.Exercise;
import com.example.prog1learnapp.model.Lesson;
import com.example.prog1learnapp.repository.ExerciseRepository;
import com.example.prog1learnapp.repository.LessonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(ExamSelectionService.class)
class ExamSelectionServiceTest {

    @Autowired
    private ExamSelectionService examSelectionService;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @BeforeEach
    void clean() {
        exerciseRepository.deleteAll();
        lessonRepository.deleteAll();
    }

    @Test
    void startNewExam_selectsExactlyThreeExercises_onePerBand_inBandOrder() {
        createLessonWithExercise(3L, "Band 1");
        createLessonWithExercise(6L, "Band 2");
        createLessonWithExercise(9L, "Band 3");

        ExamSessionState state = examSelectionService.startNewExam();

        assertNotNull(state);
        assertEquals(3, state.getSelectedExerciseIds().size());
        assertNotNull(state.getAttemptId());

        List<Exercise> selected = examSelectionService.resolveSelectedExercises(state);
        assertEquals(3, selected.size());
        assertTrue(selected.get(0).getLesson().getId() >= 3L && selected.get(0).getLesson().getId() <= 5L);
        assertTrue(selected.get(1).getLesson().getId() >= 6L && selected.get(1).getLesson().getId() <= 8L);
        assertTrue(selected.get(2).getLesson().getId() >= 9L && selected.get(2).getLesson().getId() <= 11L);
    }

    @Test
    void startNewExam_throwsWhenAnyBandHasNoCandidates() {
        createLessonWithExercise(3L, "Band 1");
        createLessonWithExercise(9L, "Band 3");

        ExamSelectionException ex = assertThrows(ExamSelectionException.class, () -> examSelectionService.startNewExam());
        assertTrue(ex.getMessage().contains("6-8"));
    }

    private void createLessonWithExercise(Long lessonId, String suffix) {
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setTitle("Lesson " + suffix);
        lesson.setShortDescription("Desc " + suffix);
        lesson.setContent("Content " + suffix);
        lessonRepository.save(lesson);

        Exercise exercise = new Exercise();
        exercise.setTitle("Exercise " + suffix);
        exercise.setDescription("Description " + suffix);
        exercise.setStarterCode("class Main {}");
        exercise.setSolution("class Main {}");
        exercise.setDifficulty("EASY");
        exercise.setLesson(lesson);
        exerciseRepository.save(exercise);
    }
}
