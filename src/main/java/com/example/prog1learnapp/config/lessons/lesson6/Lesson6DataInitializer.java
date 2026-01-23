package com.example.prog1learnapp.config.lessons.lesson6;

import com.example.prog1learnapp.config.lessons.LessonDataInitializer;
import com.example.prog1learnapp.config.seed.ExerciseSeedService;
import com.example.prog1learnapp.model.Exercise;
import com.example.prog1learnapp.model.Lesson;
import com.example.prog1learnapp.repository.LessonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Lesson6DataInitializer implements LessonDataInitializer {

    private static final Logger log =
            LoggerFactory.getLogger(Lesson6DataInitializer.class);

    private final LessonRepository lessonRepository;
    private final ExerciseSeedService exerciseSeedService;

    public Lesson6DataInitializer(LessonRepository lessonRepository,
                                  ExerciseSeedService exerciseSeedService) {
        this.lessonRepository = lessonRepository;
        this.exerciseSeedService = exerciseSeedService;
    }
    public void init() {

        Lesson lesson6 = lessonRepository.findById(6L)
                .orElseGet(() -> lessonRepository.save(
                        new Lesson(
                                6L,
                                "Methoden verstehen",
                                "Parameter, Rückgabewerte, Überladung & Rekursion",
                                "<h2>Was sind Methoden?</h2>\n" +
                                        "<p>Methoden sind wiederverwendbare Codeblöcke, die eine bestimmte Aufgabe erfüllen. " +
                                        "Sie helfen dabei, Code zu strukturieren und Wiederholungen zu vermeiden.</p>\n" +
                                        "\n" +
                                        "<h3>Aufbau einer Methode</h3>\n" +
                                        "<pre><code>public static int addiere(int a, int b) {\n" +
                                        "    return a + b;\n" +
                                        "}</code></pre>\n" +
                                        "<ul>\n" +
                                        "    <li><code>public</code> – Sichtbarkeit (öffentlich zugänglich)</li>\n" +
                                        "    <li><code>static</code> – Klassenmethode (ohne Objekt aufrufbar)</li>\n" +
                                        "    <li><code>int</code> – Rückgabetyp</li>\n" +
                                        "    <li><code>addiere</code> – Methodenname</li>\n" +
                                        "    <li><code>(int a, int b)</code> – Parameter</li>\n" +
                                        "</ul>\n" +
                                        "\n" +
                                        "<h2>Arten von Methoden</h2>\n" +
                                        "\n" +
                                        "<h3>Statische Methoden vs. Instanzmethoden</h3>\n" +
                                        "<ul>\n" +
                                        "    <li><b>static:</b> Gehört zur Klasse, Aufruf: <code>Klasse.methode()</code></li>\n" +
                                        "    <li><b>nicht-static:</b> Gehört zum Objekt, Aufruf: <code>objekt.methode()</code></li>\n" +
                                        "</ul>\n" +
                                        "\n" +
                                        "<h3>Methodenüberladung (Overloading)</h3>\n" +
                                        "<p>Mehrere Methoden mit gleichem Namen, aber unterschiedlichen Parametern:</p>\n" +
                                        "<pre><code>int addiere(int a, int b) { ... }\n" +
                                        "double addiere(double a, double b) { ... }</code></pre>\n" +
                                        "\n" +
                                        "<h3>Rekursion</h3>\n" +
                                        "<p>Eine Methode, die sich selbst aufruft. Wichtig: <b>Abbruchbedingung</b> nicht vergessen!</p>\n" +
                                        "<pre><code>int fakultaet(int n) {\n" +
                                        "    if (n == 0) return 1;  // Abbruchbedingung\n" +
                                        "    return n * fakultaet(n - 1);\n" +
                                        "}</code></pre>\n" +
                                        "\n" +
                                        "<h2>Wichtige Konzepte</h2>\n" +
                                        "<ul>\n" +
                                        "    <li><b>Scope:</b> Lokale Variablen gelten nur innerhalb ihrer Methode</li>\n" +
                                        "    <li><b>Stack:</b> Speichert Methodenaufrufe und lokale Variablen</li>\n" +
                                        "    <li><b>Heap:</b> Speichert Objekte (mit <code>new</code> erstellt)</li>\n" +
                                        "</ul>"
                        )
                ));

        if (!lessonRepository.existsById(6L)) {
            lessonRepository.save(lesson6);
        }
        Exercise ex6 = new Exercise();
        ex6.setTitle("Methoden üben: statisch, überladen, rekursiv, Instanz");
        ex6.setDescription("Implementiere die geforderten Methoden im Beispielcode. Beachte die Hinweise in den Kommentaren.");
        ex6.setStarterCode(
                "public class MethodenUebung {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        // Statische Methode aufrufen\n" +
                        "        int summe = addiere(3, 5);\n" +
                        "        System.out.println(\"Summe: \" + summe);\n\n" +
                        "        // Überladene Methode aufrufen\n" +
                        "        double summeDouble = addiere(2.5, 4.5);\n" +
                        "        System.out.println(\"Summe (double): \" + summeDouble);\n\n" +
                        "        // Rekursive Methode aufrufen\n" +
                        "        int fakultaet = berechneFakultaet(5);\n" +
                        "        System.out.println(\"Fakultät von 5: \" + fakultaet);\n\n" +
                        "        // Instanzmethode aufrufen\n" +
                        "        MethodenUebung objekt = new MethodenUebung();\n" +
                        "        objekt.gruessen(\"Alex\");\n" +
                        "    }\n\n" +
                        "    // TODO 1: Statische Methode, die zwei int-Werte addiert und zurückgibt\n" +
                        "    // TODO 2: Überladene Methode, die zwei double-Werte addiert\n" +
                        "    // TODO 3: Rekursive Methode zur Berechnung der Fakultät\n" +
                        "    // Hinweis: Fakultät von 0 ist 1\n" +
                        "    // TODO 4: Instanzmethode, die eine Begrüßung ausgibt\n" +
                        "}"
        );
        ex6.setSolution(
                "public class MethodenUebung {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        int summe = addiere(3, 5);\n" +
                        "        System.out.println(\"Summe: \" + summe);\n\n" +
                        "        double summeDouble = addiere(2.5, 4.5);\n" +
                        "        System.out.println(\"Summe (double): \" + summeDouble);\n\n" +
                        "        int fakultaet = berechneFakultaet(5);\n" +
                        "        System.out.println(\"Fakultät von 5: \" + fakultaet);\n\n" +
                        "        MethodenUebung objekt = new MethodenUebung();\n" +
                        "        objekt.gruessen(\"Alex\");\n" +
                        "    }\n\n" +
                        "    // Statische Methode mit Rückgabewert\n" +
                        "    public static int addiere(int a, int b) {\n" +
                        "        return a + b;\n" +
                        "    }\n\n" +
                        "    // Überladene Methode (gleicher Name, andere Parameter)\n" +
                        "    public static double addiere(double a, double b) {\n" +
                        "        return a + b;\n" +
                        "    }\n\n" +
                        "    // Rekursive Methode\n" +
                        "    public static int berechneFakultaet(int zahl) {\n" +
                        "        if (zahl == 0) {\n" +
                        "            return 1; // Abbruchbedingung\n" +
                        "        }\n" +
                        "        return zahl * berechneFakultaet(zahl - 1);\n" +
                        "    }\n\n" +
                        "    // Instanzmethode\n" +
                        "    public void gruessen(String name) {\n" +
                        "        System.out.println(\"Hallo \" + name + \"!\");\n" +
                        "    }\n" +
                        "}"
        );
        ex6.setDifficulty("MEDIUM");
        ex6.setLesson(lesson6);
        exerciseSeedService.saveExerciseIfNotExists(ex6);
    }


}
