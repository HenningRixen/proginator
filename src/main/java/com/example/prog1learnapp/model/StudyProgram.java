package com.example.prog1learnapp.model;

import java.util.Locale;

public enum StudyProgram {
    WINF,
    AINF;

    public static StudyProgram fromValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return null;
        }
        try {
            return StudyProgram.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
