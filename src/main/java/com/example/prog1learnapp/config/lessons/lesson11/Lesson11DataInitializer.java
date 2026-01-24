package com.example.prog1learnapp.config.lessons.lesson11;

import com.example.prog1learnapp.config.lessons.LessonDataInitializer;
import com.example.prog1learnapp.config.seed.ExerciseSeedService;
import com.example.prog1learnapp.model.Exercise;
import com.example.prog1learnapp.model.Lesson;
import com.example.prog1learnapp.repository.LessonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Lesson11DataInitializer implements LessonDataInitializer {

    private static final Logger log =
            LoggerFactory.getLogger(Lesson11DataInitializer.class);

    private final LessonRepository lessonRepository;
    private final ExerciseSeedService exerciseSeedService;

    public Lesson11DataInitializer(LessonRepository lessonRepository,
                                   ExerciseSeedService exerciseSeedService) {
        this.lessonRepository = lessonRepository;
        this.exerciseSeedService = exerciseSeedService;
    }

    @Override
    public void init() {

        Lesson lesson11 = lessonRepository.findById(11L)
                .orElseGet(() -> lessonRepository.save(
                        new Lesson(
                                11L,
                                "Ausblick: Weiterführende Themen",
                                "Generics, Lambdas, Streams, I/O – Ausblick auf Prog2",
                                "<h2>Ausblick auf Programmierung II</h2>\n" +
                                        "<p>Diese Themen werden typischerweise in Programmierung II behandelt. " +
                                        "Hier ein kurzer Überblick zur Vorbereitung.</p>\n" +
                                        "\n" +
                                        "<h3>Generics</h3>\n" +
                                        "<p>Generics ermöglichen typsichere Klassen und Methoden:</p>\n" +
                                        "<pre><code>public class Box&lt;T&gt; {\n" +
                                        "    private T inhalt;\n" +
                                        "    public void setInhalt(T inhalt) { this.inhalt = inhalt; }\n" +
                                        "    public T getInhalt() { return inhalt; }\n" +
                                        "}</code></pre>\n" +
                                        "\n" +
                                        "<h3>Lambda-Ausdrücke (Java 8+)</h3>\n" +
                                        "<p>Kurze Schreibweise für funktionale Interfaces:</p>\n" +
                                        "<pre><code>// Statt anonymer Klasse:\n" +
                                        "Comparator&lt;String&gt; comp = (s1, s2) -&gt; s1.length() - s2.length();\n\n" +
                                        "// Für Listen:\n" +
                                        "liste.forEach(s -&gt; System.out.println(s));</code></pre>\n" +
                                        "\n" +
                                        "<h3>Streams (Java 8+)</h3>\n" +
                                        "<p>Funktionale Verarbeitung von Collections:</p>\n" +
                                        "<pre><code>List&lt;String&gt; namen = studenten.stream()\n" +
                                        "    .filter(s -&gt; s.getSemester() &gt; 2)\n" +
                                        "    .map(Student::getName)\n" +
                                        "    .sorted()\n" +
                                        "    .collect(Collectors.toList());</code></pre>\n" +
                                        "\n" +
                                        "<h3>Datei-I/O</h3>\n" +
                                        "<p>Lesen und Schreiben von Dateien:</p>\n" +
                                        "<pre><code>// Moderne Variante (Java 7+)\n" +
                                        "List&lt;String&gt; zeilen = Files.readAllLines(Path.of(\"datei.txt\"));\n" +
                                        "Files.writeString(Path.of(\"out.txt\"), \"Inhalt\");</code></pre>\n" +
                                        "\n" +
                                        "<h3>Weitere Themen in Prog2</h3>\n" +
                                        "<ul>\n" +
                                        "    <li><b>Multithreading:</b> Parallele Programmierung</li>\n" +
                                        "    <li><b>GUI:</b> Grafische Oberflächen (JavaFX, Swing)</li>\n" +
                                        "    <li><b>Netzwerk:</b> Sockets, HTTP-Kommunikation</li>\n" +
                                        "    <li><b>Datenbanken:</b> JDBC, SQL-Anbindung</li>\n" +
                                        "    <li><b>Serialisierung:</b> Objekte speichern/laden</li>\n" +
                                        "</ul>"
                        )
                ));

        // Aufgabe 11.1: Lambda und forEach
        Exercise ex11_1 = new Exercise();
        ex11_1.setTitle("DHSH-Studentenliste: Lambda-Ausdrücke");
        ex11_1.setDescription(
                "Lerne Lambda-Ausdrücke kennen, um Code kürzer und lesbarer zu schreiben.\n\n" +
                        "Konzepte:\n" +
                        "• Lambda-Syntax: (parameter) -> ausdruck\n" +
                        "• forEach() mit Lambda\n" +
                        "• Comparator mit Lambda\n\n" +
                        "Aufgaben:\n" +
                        "• Gib alle Studenten mit forEach und Lambda aus\n" +
                        "• Sortiere Studenten nach Name mit Lambda-Comparator"
        );
        ex11_1.setStarterCode(
                "import java.util.ArrayList;\n" +
                        "import java.util.Collections;\n" +
                        "import java.util.Comparator;\n\n" +
                        "public class LambdaBeispiel {\n\n" +
                        "    public static void main(String[] args) {\n" +
                        "        ArrayList<String> studenten = new ArrayList<>();\n" +
                        "        studenten.add(\"Maria\");\n" +
                        "        studenten.add(\"Alex\");\n" +
                        "        studenten.add(\"Tim\");\n" +
                        "        studenten.add(\"Anna\");\n\n" +
                        "        // TODO 1: Gib alle Studenten mit forEach und Lambda aus\n" +
                        "        // Syntax: liste.forEach(element -> ...)\n" +
                        "        System.out.println(\"Alle Studenten:\");\n\n" +
                        "        // TODO 2: Sortiere nach Länge des Namens mit Lambda\n" +
                        "        // Syntax: Collections.sort(liste, (a, b) -> ...)\n" +
                        "        System.out.println(\"\\nNach Namenslänge sortiert:\");\n\n" +
                        "        // TODO 3: Sortiere alphabetisch absteigend (Z-A)\n" +
                        "        System.out.println(\"\\nAlphabetisch absteigend:\");\n" +
                        "    }\n" +
                        "}"
        );
        ex11_1.setSolution(
                "import java.util.ArrayList;\n" +
                        "import java.util.Collections;\n" +
                        "import java.util.Comparator;\n\n" +
                        "public class LambdaBeispiel {\n\n" +
                        "    public static void main(String[] args) {\n" +
                        "        ArrayList<String> studenten = new ArrayList<>();\n" +
                        "        studenten.add(\"Maria\");\n" +
                        "        studenten.add(\"Alex\");\n" +
                        "        studenten.add(\"Tim\");\n" +
                        "        studenten.add(\"Anna\");\n\n" +
                        "        System.out.println(\"Alle Studenten:\");\n" +
                        "        studenten.forEach(s -> System.out.println(s));\n\n" +
                        "        Collections.sort(studenten, (a, b) -> a.length() - b.length());\n" +
                        "        System.out.println(\"\\nNach Namenslänge sortiert:\");\n" +
                        "        studenten.forEach(s -> System.out.println(s));\n\n" +
                        "        Collections.sort(studenten, (a, b) -> b.compareTo(a));\n" +
                        "        System.out.println(\"\\nAlphabetisch absteigend:\");\n" +
                        "        studenten.forEach(s -> System.out.println(s));\n" +
                        "    }\n" +
                        "}"
        );
        ex11_1.setDifficulty("EASY");
        ex11_1.setLesson(lesson11);
        exerciseSeedService.saveExerciseIfNotExists(ex11_1);

        // Aufgabe 11.2: Einfache Stream-Operationen
        Exercise ex11_2 = new Exercise();
        ex11_2.setTitle("DHSH-Notenauswertung: Einführung in Streams");
        ex11_2.setDescription(
                "Streams ermöglichen eine elegante Verarbeitung von Collections.\n\n" +
                        "Konzepte:\n" +
                        "• stream() erzeugen\n" +
                        "• filter() zum Filtern\n" +
                        "• map() zum Transformieren\n" +
                        "• collect() zum Sammeln\n\n" +
                        "Aufgaben:\n" +
                        "• Filtere Noten besser als 3\n" +
                        "• Berechne den Durchschnitt mit Streams\n" +
                        "• Zähle bestandene Prüfungen"
        );
        ex11_2.setStarterCode(
                "import java.util.ArrayList;\n" +
                        "import java.util.List;\n" +
                        "import java.util.stream.Collectors;\n\n" +
                        "public class StreamBeispiel {\n\n" +
                        "    public static void main(String[] args) {\n" +
                        "        ArrayList<Integer> noten = new ArrayList<>();\n" +
                        "        noten.add(1);\n" +
                        "        noten.add(3);\n" +
                        "        noten.add(2);\n" +
                        "        noten.add(5);\n" +
                        "        noten.add(4);\n" +
                        "        noten.add(2);\n" +
                        "        noten.add(1);\n\n" +
                        "        // TODO 1: Filtere alle guten Noten (1 oder 2)\n" +
                        "        // Syntax: liste.stream().filter(n -> ...).collect(...)\n" +
                        "        List<Integer> guteNoten = noten; // Ersetze durch Stream\n" +
                        "        System.out.println(\"Gute Noten: \" + guteNoten);\n\n" +
                        "        // TODO 2: Zähle bestandene Prüfungen (Note <= 4)\n" +
                        "        // Syntax: liste.stream().filter(...).count()\n" +
                        "        long bestanden = 0; // Ersetze durch Stream\n" +
                        "        System.out.println(\"Bestandene Prüfungen: \" + bestanden);\n\n" +
                        "        // TODO 3: Berechne den Notendurchschnitt\n" +
                        "        // Syntax: liste.stream().mapToInt(n -> n).average()\n" +
                        "        double durchschnitt = 0.0; // Ersetze durch Stream\n" +
                        "        System.out.println(\"Durchschnitt: \" + durchschnitt);\n" +
                        "    }\n" +
                        "}"
        );
        ex11_2.setSolution(
                "import java.util.ArrayList;\n" +
                        "import java.util.List;\n" +
                        "import java.util.stream.Collectors;\n\n" +
                        "public class StreamBeispiel {\n\n" +
                        "    public static void main(String[] args) {\n" +
                        "        ArrayList<Integer> noten = new ArrayList<>();\n" +
                        "        noten.add(1);\n" +
                        "        noten.add(3);\n" +
                        "        noten.add(2);\n" +
                        "        noten.add(5);\n" +
                        "        noten.add(4);\n" +
                        "        noten.add(2);\n" +
                        "        noten.add(1);\n\n" +
                        "        List<Integer> guteNoten = noten.stream()\n" +
                        "                .filter(n -> n <= 2)\n" +
                        "                .collect(Collectors.toList());\n" +
                        "        System.out.println(\"Gute Noten: \" + guteNoten);\n\n" +
                        "        long bestanden = noten.stream()\n" +
                        "                .filter(n -> n <= 4)\n" +
                        "                .count();\n" +
                        "        System.out.println(\"Bestandene Prüfungen: \" + bestanden);\n\n" +
                        "        double durchschnitt = noten.stream()\n" +
                        "                .mapToInt(n -> n)\n" +
                        "                .average()\n" +
                        "                .orElse(0.0);\n" +
                        "        System.out.println(\"Durchschnitt: \" + durchschnitt);\n" +
                        "    }\n" +
                        "}"
        );
        ex11_2.setDifficulty("MEDIUM");
        ex11_2.setLesson(lesson11);
        exerciseSeedService.saveExerciseIfNotExists(ex11_2);

        log.info("Lesson 11 initialized with 2 exercises");
    }
}
