package com.example.prog1learnapp.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Lesson {
    @Id
    private Long id;

    private String title;
    private String shortDescription;

    @Lob
    private String content;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL)
    private List<Exercise> exercises = new ArrayList<>();

    public Lesson() {}

    public Lesson(Long id, String title, String shortDescription, String content) {
        this.id = id;
        this.title = title;
        this.shortDescription = shortDescription;
        this.content = content;
    }

    // Getter/Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<Exercise> getExercises() { return exercises; }
    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }
}