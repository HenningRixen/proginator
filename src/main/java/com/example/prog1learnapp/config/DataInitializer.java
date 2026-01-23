package com.example.prog1learnapp.config;

import com.example.prog1learnapp.config.lessons.LessonDataInitializer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final List<LessonDataInitializer> lessonInitializers;

    public DataInitializer(List<LessonDataInitializer> lessonInitializers) {
        this.lessonInitializers = lessonInitializers;
    }

    @Override
    public void run(String... args) {
        lessonInitializers.forEach(LessonDataInitializer::init);
    }
}
