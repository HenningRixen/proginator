package com.example.prog1learnapp.model;

/**
 * Schwierigkeitsgrade für Übungen.
 */
public enum Difficulty {
    EASY("Einfach"),
    MEDIUM("Mittel"),
    HARD("Schwer");

    private final String displayName;

    Difficulty(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Konvertiert einen String sicher in ein Difficulty-Enum.
     * Falls der String ungültig ist, wird EASY zurückgegeben.
     */
    public static Difficulty fromString(String value) {
        if (value == null) {
            return EASY;
        }
        try {
            return Difficulty.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return EASY;
        }
    }
}
