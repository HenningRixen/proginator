package com.example.prog1learnapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 2000, nullable = false)
    private String text;

    @Column(nullable = false)
    private Integer rating; // 1-5

    @Enumerated(EnumType.STRING)
    @Column(name = "study_program", length = 4)
    private StudyProgram studyProgram;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Feedback() {
    }

    public Feedback(String text, Integer rating, StudyProgram studyProgram) {
        this.text = text;
        this.rating = rating;
        this.studyProgram = studyProgram;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getter/Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public StudyProgram getStudyProgram() { return studyProgram; }
    public void setStudyProgram(StudyProgram studyProgram) { this.studyProgram = studyProgram; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
