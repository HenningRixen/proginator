package com.example.prog1learnapp.controller;

import com.example.prog1learnapp.model.ExamSessionState;
import com.example.prog1learnapp.model.Exercise;
import com.example.prog1learnapp.model.Lesson;
import com.example.prog1learnapp.model.User;
import com.example.prog1learnapp.repository.ExerciseRepository;
import com.example.prog1learnapp.repository.LessonRepository;
import com.example.prog1learnapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ExamControllerIntegrationTest {

    private static final AtomicLong LESSON_ID_SEQ = new AtomicLong(5000L);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(username = "examuser")
    void examStart_createsSessionStateAndRedirects() throws Exception {
        MvcResult result = mockMvc.perform(get("/exam/start"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exam"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);
        Object stateObj = session.getAttribute(ExamController.EXAM_SESSION_KEY);
        assertNotNull(stateObj);

        ExamSessionState state = (ExamSessionState) stateObj;
        assertEquals(3, state.getSelectedExerciseIds().size());
        assertNotNull(state.getAttemptId());
    }

    @Test
    @WithMockUser(username = "examuser")
    void exam_rendersThreeExercisesFromSession() throws Exception {
        MvcResult start = mockMvc.perform(get("/exam/start"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        MockHttpSession session = (MockHttpSession) start.getRequest().getSession(false);

        mockMvc.perform(get("/exam").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("exam"))
                .andExpect(model().attributeExists("examExercises"))
                .andExpect(model().attribute("examExercises", hasSize(3)))
                .andExpect(model().attributeExists("examAttemptId"))
                .andExpect(model().attribute("examCompletedCount", 0))
                .andExpect(model().attribute("examTotalCount", 3))
                .andExpect(model().attribute("canRevealSolutions", false))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"exam-solutions-container\" hidden")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Loesungen werden freigeschaltet")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/css/exercise.css")));
    }

    @Test
    @WithMockUser(username = "examuser")
    void dashboard_withActiveExamAttempt_resumesInsteadOfRestarting() throws Exception {
        createUserIfMissing("examuser");

        MvcResult start = mockMvc.perform(get("/exam/start"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        MockHttpSession session = (MockHttpSession) start.getRequest().getSession(false);

        mockMvc.perform(get("/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("hasActiveExamAttempt", true))
                .andExpect(model().attribute("examCompletedCount", 0))
                .andExpect(model().attribute("examTotalCount", 3))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Exam fortsetzen")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/exam\"")));
    }

    @Test
    @WithMockUser(username = "examuser")
    void exam_reloadKeepsSameSelectedIds() throws Exception {
        MvcResult start = mockMvc.perform(get("/exam/start"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        MockHttpSession session = (MockHttpSession) start.getRequest().getSession(false);
        ExamSessionState initialState = (ExamSessionState) session.getAttribute(ExamController.EXAM_SESSION_KEY);
        List<Long> initialIds = new ArrayList<>(initialState.getSelectedExerciseIds());
        String initialAttemptId = initialState.getAttemptId();

        mockMvc.perform(get("/exam").session(session))
                .andExpect(status().isOk());

        ExamSessionState afterFirstLoad = (ExamSessionState) session.getAttribute(ExamController.EXAM_SESSION_KEY);
        assertEquals(initialIds, afterFirstLoad.getSelectedExerciseIds());
        assertEquals(initialAttemptId, afterFirstLoad.getAttemptId());

        mockMvc.perform(get("/exam").session(session))
                .andExpect(status().isOk());

        ExamSessionState afterSecondLoad = (ExamSessionState) session.getAttribute(ExamController.EXAM_SESSION_KEY);
        assertEquals(initialIds, afterSecondLoad.getSelectedExerciseIds());
        assertEquals(initialAttemptId, afterSecondLoad.getAttemptId());
    }

    @Test
    @WithMockUser(username = "examuser")
    void examStart_explicitRestartCreatesNewAttemptId() throws Exception {
        MvcResult firstStart = mockMvc.perform(get("/exam/start"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        MockHttpSession session = (MockHttpSession) firstStart.getRequest().getSession(false);
        ExamSessionState firstState = (ExamSessionState) session.getAttribute(ExamController.EXAM_SESSION_KEY);
        String firstAttemptId = firstState.getAttemptId();

        mockMvc.perform(get("/exam/start").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exam"));

        ExamSessionState secondState = (ExamSessionState) session.getAttribute(ExamController.EXAM_SESSION_KEY);
        assertNotNull(secondState.getAttemptId());
        assertNotEquals(firstAttemptId, secondState.getAttemptId());
    }

    @Test
    @WithMockUser(username = "examuser")
    void exam_withInvalidSelectedIds_doesNotReshuffleAndShowsError() throws Exception {
        ExamSessionState brokenState = new ExamSessionState(List.of(999999L));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(ExamController.EXAM_SESSION_KEY, brokenState);

        mockMvc.perform(get("/exam").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("exam"))
                .andExpect(model().attributeExists("examError"))
                .andExpect(model().attribute("examAttemptId", brokenState.getAttemptId()));

        ExamSessionState stateAfter = (ExamSessionState) session.getAttribute(ExamController.EXAM_SESSION_KEY);
        assertEquals(brokenState.getAttemptId(), stateAfter.getAttemptId());
        assertEquals(List.of(999999L), stateAfter.getSelectedExerciseIds());
    }

    @Test
    @WithMockUser(username = "examuser")
    void examComplete_acceptsSelectedExerciseId() throws Exception {
        MvcResult start = mockMvc.perform(get("/exam/start"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        MockHttpSession session = (MockHttpSession) start.getRequest().getSession(false);
        ExamSessionState state = (ExamSessionState) session.getAttribute(ExamController.EXAM_SESSION_KEY);
        Long selectedId = state.getSelectedExerciseIds().get(0);

        mockMvc.perform(post("/exam/{exerciseId}/complete", selectedId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.completedCount").value(1))
                .andExpect(jsonPath("$.totalCount").value(3))
                .andExpect(jsonPath("$.canRevealSolutions").value(false));

        ExamSessionState updatedState = (ExamSessionState) session.getAttribute(ExamController.EXAM_SESSION_KEY);
        assertTrue(updatedState.getCompletedExerciseIds().contains(selectedId));
    }

    @Test
    @WithMockUser(username = "examuser")
    void examComplete_rejectsNonSelectedExerciseId() throws Exception {
        MvcResult start = mockMvc.perform(get("/exam/start"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        MockHttpSession session = (MockHttpSession) start.getRequest().getSession(false);

        mockMvc.perform(post("/exam/{exerciseId}/complete", 999999L).session(session))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @WithMockUser(username = "examuser")
    void examSolutions_unlockOnlyAfterAllThreeCompleted() throws Exception {
        MvcResult start = mockMvc.perform(get("/exam/start"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        MockHttpSession session = (MockHttpSession) start.getRequest().getSession(false);
        ExamSessionState state = (ExamSessionState) session.getAttribute(ExamController.EXAM_SESSION_KEY);

        for (Long id : state.getSelectedExerciseIds()) {
            mockMvc.perform(post("/exam/{exerciseId}/complete", id).session(session))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"));
        }

        mockMvc.perform(get("/exam").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("examCompletedCount", 3))
                .andExpect(model().attribute("examTotalCount", 3))
                .andExpect(model().attribute("canRevealSolutions", true));
    }

    @Test
    @WithMockUser(username = "alice")
    void exerciseComplete_stillUpdatesUserCompletedExercises() throws Exception {
        createUserIfMissing("alice");
        Exercise exercise = createExerciseForNewLesson("regression-normal-complete");

        mockMvc.perform(post("/exercise/{id}/complete", exercise.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        User updated = userRepository.findByUsername("alice").orElseThrow();
        assertTrue(updated.getCompletedExercises().contains(exercise.getId()));
    }

    @Test
    @WithMockUser(username = "bob")
    void examComplete_doesNotAffectUserCompletedExercises() throws Exception {
        createUserIfMissing("bob");
        Exercise exercise = createExerciseForNewLesson("regression-exam-complete");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(ExamController.EXAM_SESSION_KEY, new ExamSessionState(List.of(exercise.getId())));

        mockMvc.perform(post("/exam/{exerciseId}/complete", exercise.getId()).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        User updated = userRepository.findByUsername("bob").orElseThrow();
        assertTrue(updated.getCompletedExercises().isEmpty());
    }

    private void createUserIfMissing(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("password"));
        user.setDisplayName(username);
        userRepository.save(user);
    }

    private Exercise createExerciseForNewLesson(String suffix) {
        long lessonId = LESSON_ID_SEQ.incrementAndGet();

        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setTitle("Test Lesson " + suffix);
        lesson.setShortDescription("Short " + suffix);
        lesson.setContent("Content " + suffix);
        lessonRepository.save(lesson);

        Exercise exercise = new Exercise();
        exercise.setTitle("Test Exercise " + suffix);
        exercise.setDescription("Description " + suffix);
        exercise.setStarterCode("class Main {}");
        exercise.setSolution("class Main {}");
        exercise.setDifficulty("EASY");
        exercise.setLesson(lesson);
        return exerciseRepository.save(exercise);
    }
}
