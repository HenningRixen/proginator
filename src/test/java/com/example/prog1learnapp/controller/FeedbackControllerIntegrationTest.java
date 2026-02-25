package com.example.prog1learnapp.controller;

import com.example.prog1learnapp.model.Feedback;
import com.example.prog1learnapp.model.StudyProgram;
import com.example.prog1learnapp.repository.FeedbackRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FeedbackControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Test
    @WithMockUser(username = "feedbackuser")
    void submitFeedback_withStudyProgram_persistsFeedback() throws Exception {
        mockMvc.perform(post("/feedback")
                        .with(csrf())
                        .param("text", "Sehr hilfreiche Plattform.")
                        .param("rating", "5")
                        .param("studyProgram", "WINF"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/feedback"))
                .andExpect(flash().attributeExists("success"));

        List<Feedback> feedbackList = feedbackRepository.findAll();
        assertEquals(1, feedbackList.size());
        Feedback saved = feedbackList.get(0);
        assertEquals("Sehr hilfreiche Plattform.", saved.getText());
        assertEquals(5, saved.getRating());
        assertEquals(StudyProgram.WINF, saved.getStudyProgram());
    }

    @Test
    @WithMockUser(username = "feedbackuser")
    void submitFeedback_withoutStudyProgram_rejectsSubmission() throws Exception {
        mockMvc.perform(post("/feedback")
                        .with(csrf())
                        .param("text", "Text vorhanden")
                        .param("rating", "4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/feedback"))
                .andExpect(flash().attributeExists("error"));

        assertTrue(feedbackRepository.findAll().isEmpty());
    }

    @Test
    @WithMockUser(username = "feedbackuser")
    void submitFeedback_withInvalidStudyProgram_rejectsSubmission() throws Exception {
        mockMvc.perform(post("/feedback")
                        .with(csrf())
                        .param("text", "Text vorhanden")
                        .param("rating", "4")
                        .param("studyProgram", "OTHER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/feedback"))
                .andExpect(flash().attributeExists("error"));

        assertTrue(feedbackRepository.findAll().isEmpty());
    }

    @Test
    @WithMockUser(username = "feedbackuser")
    void submitFeedback_withoutRating_rejectsSubmission() throws Exception {
        mockMvc.perform(post("/feedback")
                        .with(csrf())
                        .param("text", "Text vorhanden")
                        .param("studyProgram", "AINF"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/feedback"))
                .andExpect(flash().attributeExists("error"));

        assertTrue(feedbackRepository.findAll().isEmpty());
    }
}
