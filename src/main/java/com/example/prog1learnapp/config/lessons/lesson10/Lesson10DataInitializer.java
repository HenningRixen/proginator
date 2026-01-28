package com.example.prog1learnapp.config.lessons.lesson10;

import com.example.prog1learnapp.config.lessons.LessonDataInitializer;
import com.example.prog1learnapp.config.seed.ExerciseSeedService;
import com.example.prog1learnapp.model.Exercise;
import com.example.prog1learnapp.model.Lesson;
import com.example.prog1learnapp.repository.LessonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Lesson10DataInitializer implements LessonDataInitializer {

    private static final Logger log =
            LoggerFactory.getLogger(Lesson10DataInitializer.class);

    private final LessonRepository lessonRepository;
    private final ExerciseSeedService exerciseSeedService;

    public Lesson10DataInitializer(LessonRepository lessonRepository,
                                   ExerciseSeedService exerciseSeedService) {
        this.lessonRepository = lessonRepository;
        this.exerciseSeedService = exerciseSeedService;
    }

    @Override
    public void init() {

        Lesson lesson10 = lessonRepository.findById(10L)
                .orElseGet(() -> lessonRepository.save(
                        new Lesson(
                                10L,
                                "Pakete & Ausnahmen",
                                "package, import, try/catch/finally, throws, eigene Exceptions",
                                "<h2>Pakete (Packages)</h2>\n" +
                                        "<p>Pakete dienen der Strukturierung von Java-Programmen und vermeiden Namenskonflikte.</p>\n" +
                                        "\n" +
                                        "<h3>Package-Deklaration</h3>\n" +
                                        "<pre><code>package de.dhsh.prog1.model;\n\n" +
                                        "public class Student {\n" +
                                        "    // ...\n" +
                                        "}</code></pre>\n" +
                                        "\n" +
                                        "<h3>Import-Anweisungen</h3>\n" +
                                        "<pre><code>import java.util.ArrayList;  // Einzelne Klasse\n" +
                                        "import java.util.*;           // Alle Klassen des Pakets</code></pre>\n" +
                                        "\n" +
                                        "<h3>Paket-Konventionen</h3>\n" +
                                        "<ul>\n" +
                                        "    <li>Kleinbuchstaben verwenden</li>\n" +
                                        "    <li>Umgekehrte Domain: <code>de.dhsh.projekt</code></li>\n" +
                                        "    <li>Unterverzeichnisse entsprechen der Paketstruktur</li>\n" +
                                        "</ul>\n" +
                                        "\n" +
                                        "<h2>Ausnahmebehandlung (Exceptions)</h2>\n" +
                                        "<p>Exceptions sind Fehler, die zur Laufzeit auftreten können.</p>\n" +
                                        "\n" +
                                        "<h3>try-catch-finally</h3>\n" +
                                        "<pre><code>try {\n" +
                                        "    int result = 10 / 0;\n" +
                                        "} catch (ArithmeticException e) {\n" +
                                        "    System.out.println(\"Fehler: \" + e.getMessage());\n" +
                                        "} finally {\n" +
                                        "    System.out.println(\"Wird immer ausgeführt\");\n" +
                                        "}</code></pre>\n" +
                                        "\n" +
                                        "<h3>Checked vs. Unchecked Exceptions</h3>\n" +
                                        "<ul>\n" +
                                        "    <li><b>Checked:</b> Müssen behandelt werden (IOException, SQLException)</li>\n" +
                                        "    <li><b>Unchecked:</b> RuntimeException (NullPointerException, ArrayIndexOutOfBoundsException)</li>\n" +
                                        "</ul>\n" +
                                        "\n" +
                                        "<h3>throws-Klausel</h3>\n" +
                                        "<pre><code>public void leseDatei(String pfad) throws IOException {\n" +
                                        "    // Methode kann IOException werfen\n" +
                                        "}</code></pre>\n" +
                                        "\n" +
                                        "<h3>Eigene Exceptions</h3>\n" +
                                        "<pre><code>public class UngueltigeMatrikelnummerException extends Exception {\n" +
                                        "    public UngueltigeMatrikelnummerException(String message) {\n" +
                                        "        super(message);\n" +
                                        "    }\n" +
                                        "}</code></pre>\n" +
                                        "\n" +
                                        "<h3>Exception werfen</h3>\n" +
                                        "<pre><code>if (matrikelnummer < 0) {\n" +
                                        "    throw new UngueltigeMatrikelnummerException(\"Matrikelnummer darf nicht negativ sein\");\n" +
                                        "}</code></pre>"
                        )
                ));

        // Aufgabe 10.1: Exception Handling
        Exercise ex10_1 = new Exercise();
        ex10_1.setTitle("Sichere Eingabeverarbeitung mit Exception Handling");
        ex10_1.setDescription(
                "Bei der Verarbeitung von Benutzereingaben können Fehler auftreten. " +
                        "Du sollst diese sicher mit try-catch behandeln.\n\n" +
                        "Konzepte:\n" +
                        "• try-catch-finally Blöcke\n" +
                        "• NumberFormatException abfangen\n" +
                        "• Sinnvolle Fehlermeldungen ausgeben\n\n" +
                        "Aufgaben:\n" +
                        "• Wandle einen String sicher in eine Zahl um\n" +
                        "• Fange mögliche Exceptions ab\n" +
                        "• Gib bei Fehlern eine Fehlermeldung aus"
        );
        ex10_1.setStarterCode(
                "public class EingabeVerarbeitung {\n\n" +
                        "    public static int parseMatrikelnummer(String eingabe) {\n" +
                        "        // TODO 1: Implementiere try-catch\n" +
                        "        // Wandle den String in eine Zahl um\n" +
                        "        // Bei Fehler: gib -1 zurück und eine Fehlermeldung aus\n" +
                        "        return 0;\n" +
                        "    }\n\n" +
                        "    public static double berechneNotenschnitt(int[] noten) {\n" +
                        "        // TODO 2: Implementiere try-catch\n" +
                        "        // Berechne den Durchschnitt\n" +
                        "        // Bei leerem Array: ArithmeticException abfangen\n" +
                        "        return 0.0;\n" +
                        "    }\n\n" +
                        "    public static void main(String[] args) {\n" +
                        "        // Test parseMatrikelnummer\n" +
                        "        System.out.println(\"Gültige Eingabe: \" + parseMatrikelnummer(\"12345\"));\n" +
                        "        System.out.println(\"Ungültige Eingabe: \" + parseMatrikelnummer(\"abc\"));\n" +
                        "        System.out.println(\"Null Eingabe: \" + parseMatrikelnummer(null));\n\n" +
                        "        // Test berechneNotenschnitt\n" +
                        "        int[] noten = {1, 2, 3, 2, 1};\n" +
                        "        int[] leereNoten = {};\n" +
                        "        System.out.println(\"Notenschnitt: \" + berechneNotenschnitt(noten));\n" +
                        "        System.out.println(\"Leeres Array: \" + berechneNotenschnitt(leereNoten));\n" +
                        "    }\n" +
                        "}"
        );
        ex10_1.setSolution(
                "public class EingabeVerarbeitung {\n\n" +
                        "    public static int parseMatrikelnummer(String eingabe) {\n" +
                        "        try {\n" +
                        "            return Integer.parseInt(eingabe);\n" +
                        "        } catch (NumberFormatException e) {\n" +
                        "            System.out.println(\"Fehler: Ungültiges Zahlenformat\");\n" +
                        "            return -1;\n" +
                        "        } catch (NullPointerException e) {\n" +
                        "            System.out.println(\"Fehler: Eingabe ist null\");\n" +
                        "            return -1;\n" +
                        "        }\n" +
                        "    }\n\n" +
                        "    public static double berechneNotenschnitt(int[] noten) {\n" +
                        "        try {\n" +
                        "            if (noten.length == 0) {\n" +
                        "                throw new ArithmeticException(\"Keine Noten vorhanden\");\n" +
                        "            }\n" +
                        "            int summe = 0;\n" +
                        "            for (int note : noten) {\n" +
                        "                summe += note;\n" +
                        "            }\n" +
                        "            return (double) summe / noten.length;\n" +
                        "        } catch (ArithmeticException e) {\n" +
                        "            System.out.println(\"Fehler: \" + e.getMessage());\n" +
                        "            return 0.0;\n" +
                        "        }\n" +
                        "    }\n\n" +
                        "    public static void main(String[] args) {\n" +
                        "        System.out.println(\"Gültige Eingabe: \" + parseMatrikelnummer(\"12345\"));\n" +
                        "        System.out.println(\"Ungültige Eingabe: \" + parseMatrikelnummer(\"abc\"));\n" +
                        "        System.out.println(\"Null Eingabe: \" + parseMatrikelnummer(null));\n\n" +
                        "        int[] noten = {1, 2, 3, 2, 1};\n" +
                        "        int[] leereNoten = {};\n" +
                        "        System.out.println(\"Notenschnitt: \" + berechneNotenschnitt(noten));\n" +
                        "        System.out.println(\"Leeres Array: \" + berechneNotenschnitt(leereNoten));\n" +
                        "    }\n" +
                        "}"
        );
        ex10_1.setDifficulty("MEDIUM");
        ex10_1.setLesson(lesson10);
        exerciseSeedService.saveExerciseIfNotExists(ex10_1);

        // Aufgabe 10.2: Eigene Exception erstellen
        Exercise ex10_2 = new Exercise();
        ex10_2.setTitle("DHSH-Prüfungsamt: Eigene Exception-Klasse");
        ex10_2.setDescription(
                "Das Prüfungsamt der DHSH benötigt spezifische Fehlermeldungen " +
                        "bei ungültigen Studentendaten.\n\n" +
                        "Konzepte:\n" +
                        "• Eigene Exception-Klasse erstellen\n" +
                        "• throws-Klausel verwenden\n" +
                        "• Exception werfen (throw)\n\n" +
                        "Aufgaben:\n" +
                        "• Erstelle eine UngueltigerStudentException\n" +
                        "• Validiere Studentendaten\n" +
                        "• Wirf die Exception bei ungültigen Daten"
        );
        ex10_2.setStarterCode(
                "// TODO 1: Erstelle die Exception-Klasse\n" +
                        "class UngueltigerStudentException extends Exception {\n" +
                        "    // Konstruktor mit Fehlermeldung\n" +
                        "}\n\n" +
                        "public class Pruefungsamt {\n\n" +
                        "    // TODO 2: Methode zur Validierung\n" +
                        "    public static void validiereStudent(String name, int matrikelnummer, int semester)\n" +
                        "            throws UngueltigerStudentException {\n" +
                        "        // Prüfe:\n" +
                        "        // - Name nicht leer\n" +
                        "        // - Matrikelnummer > 0\n" +
                        "        // - Semester zwischen 1 und 14\n" +
                        "        // Bei Fehler: throw new UngueltigerStudentException(...)\n" +
                        "    }\n\n" +
                        "    public static void main(String[] args) {\n" +
                        "        // TODO 3: Teste die Validierung mit try-catch\n" +
                        "        try {\n" +
                        "            validiereStudent(\"Alex\", 12345, 3);\n" +
                        "            System.out.println(\"Student ist gültig!\");\n" +
                        "        } catch (UngueltigerStudentException e) {\n" +
                        "            System.out.println(\"Fehler: \" + e.getMessage());\n" +
                        "        }\n\n" +
                        "        // Teste mit ungültigen Daten\n" +
                        "        try {\n" +
                        "            validiereStudent(\"\", -1, 20);\n" +
                        "        } catch (UngueltigerStudentException e) {\n" +
                        "            System.out.println(\"Fehler: \" + e.getMessage());\n" +
                        "        }\n" +
                        "    }\n" +
                        "}"
        );
        ex10_2.setSolution(
                "class UngueltigerStudentException extends Exception {\n" +
                        "    public UngueltigerStudentException(String message) {\n" +
                        "        super(message);\n" +
                        "    }\n" +
                        "}\n\n" +
                        "public class Pruefungsamt {\n\n" +
                        "    public static void validiereStudent(String name, int matrikelnummer, int semester)\n" +
                        "            throws UngueltigerStudentException {\n" +
                        "        if (name == null || name.trim().isEmpty()) {\n" +
                        "            throw new UngueltigerStudentException(\"Name darf nicht leer sein\");\n" +
                        "        }\n" +
                        "        if (matrikelnummer <= 0) {\n" +
                        "            throw new UngueltigerStudentException(\"Matrikelnummer muss positiv sein\");\n" +
                        "        }\n" +
                        "        if (semester < 1 || semester > 14) {\n" +
                        "            throw new UngueltigerStudentException(\"Semester muss zwischen 1 und 14 liegen\");\n" +
                        "        }\n" +
                        "    }\n\n" +
                        "    public static void main(String[] args) {\n" +
                        "        try {\n" +
                        "            validiereStudent(\"Alex\", 12345, 3);\n" +
                        "            System.out.println(\"Student ist gültig!\");\n" +
                        "        } catch (UngueltigerStudentException e) {\n" +
                        "            System.out.println(\"Fehler: \" + e.getMessage());\n" +
                        "        }\n\n" +
                        "        try {\n" +
                        "            validiereStudent(\"\", -1, 20);\n" +
                        "        } catch (UngueltigerStudentException e) {\n" +
                        "            System.out.println(\"Fehler: \" + e.getMessage());\n" +
                        "        }\n" +
                        "    }\n" +
                        "}"
        );
        ex10_2.setDifficulty("MEDIUM");
        ex10_2.setLesson(lesson10);
        exerciseSeedService.saveExerciseIfNotExists(ex10_2);

        log.info("Lesson 10 initialized with 2 exercises");
    }
}
