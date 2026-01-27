package com.example.prog1learnapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;

@SpringBootApplication(exclude = {HttpClientAutoConfiguration.class, RestClientAutoConfiguration.class})
public class Prog1LearnApp {
    public static void main(String[] args) {
        SpringApplication.run(Prog1LearnApp.class, args);
    }
}