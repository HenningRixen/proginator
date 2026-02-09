package com.example.prog1learnapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Prog1LearnApp {
    public static void main(String[] args) {
        SpringApplication.run(Prog1LearnApp.class, args);
    }
}
