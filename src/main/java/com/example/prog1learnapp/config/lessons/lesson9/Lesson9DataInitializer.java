package com.example.prog1learnapp.config.lessons.lesson9;

import com.example.prog1learnapp.config.lessons.LessonDataInitializer;
import com.example.prog1learnapp.config.seed.ExerciseSeedService;
import com.example.prog1learnapp.model.Exercise;
import com.example.prog1learnapp.model.Lesson;
import com.example.prog1learnapp.repository.LessonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Lesson9DataInitializer implements LessonDataInitializer {

    private static final Logger log =
            LoggerFactory.getLogger(Lesson9DataInitializer.class);

    private final LessonRepository lessonRepository;
    private final ExerciseSeedService exerciseSeedService;

    public Lesson9DataInitializer(LessonRepository lessonRepository,
                                  ExerciseSeedService exerciseSeedService) {
        this.lessonRepository = lessonRepository;
        this.exerciseSeedService = exerciseSeedService;
    }

    @Override
    public void init() {

        Lesson lesson9 = lessonRepository.findById(9L)
                .orElseGet(() -> lessonRepository.save(
                        new Lesson(
                                9L,
                                "Interfaces & Collections",
                                "interface, Comparable, Comparator, List, Set, Map",
                                "<h2>Interfaces in Java</h2>\n" +
                                        "<p>Ein Interface definiert einen Vertrag, den Klassen erfüllen müssen. " +
                                        "Es enthält nur Methodensignaturen (ohne Implementierung).</p>\n" +
                                        "\n" +
                                        "<h3>Interface definieren</h3>\n" +
                                        "<pre><code>public interface Vergleichbar {\n" +
                                        "    int vergleiche(Object o);\n" +
                                        "}</code></pre>\n" +
                                        "\n" +
                                        "<h3>Interface implementieren</h3>\n" +
                                        "<pre><code>public class Student implements Vergleichbar {\n" +
                                        "    @Override\n" +
                                        "    public int vergleiche(Object o) {\n" +
                                        "        // Implementierung\n" +
                                        "    }\n" +
                                        "}</code></pre>\n" +
                                        "<ul>\n" +
                                        "    <li>Eine Klasse kann <b>mehrere</b> Interfaces implementieren</li>\n" +
                                        "    <li>Alle Methoden eines Interfaces müssen implementiert werden</li>\n" +
                                        "    <li>Ab Java 8: Default-Methoden mit Implementierung möglich</li>\n" +
                                        "</ul>\n" +
                                        "\n" +
                                        "<h3>Comparable Interface</h3>\n" +
                                        "<p>Ermöglicht natürliche Sortierung von Objekten:</p>\n" +
                                        "<pre><code>public class Student implements Comparable&lt;Student&gt; {\n" +
                                        "    @Override\n" +
                                        "    public int compareTo(Student other) {\n" +
                                        "        return this.matrikelnummer - other.matrikelnummer;\n" +
                                        "    }\n" +
                                        "}</code></pre>\n" +
                                        "\n" +
                                        "<h3>Comparator Interface</h3>\n" +
                                        "<p>Ermöglicht alternative Sortierkriterien:</p>\n" +
                                        "<pre><code>Comparator&lt;Student&gt; nachName = (s1, s2) -&gt; \n" +
                                        "    s1.getName().compareTo(s2.getName());\n" +
                                        "Collections.sort(liste, nachName);</code></pre>\n" +
                                        "\n" +
                                        "<h2>Collections Framework</h2>\n" +
                                        "<h3>List (ArrayList, LinkedList)</h3>\n" +
                                        "<ul>\n" +
                                        "    <li>Geordnete Sammlung mit Index</li>\n" +
                                        "    <li>Duplikate erlaubt</li>\n" +
                                        "    <li><code>add(), get(), remove(), size()</code></li>\n" +
                                        "</ul>\n" +
                                        "\n" +
                                        "<h3>Set (HashSet, TreeSet)</h3>\n" +
                                        "<ul>\n" +
                                        "    <li>Keine Duplikate</li>\n" +
                                        "    <li>HashSet: keine Ordnung, TreeSet: sortiert</li>\n" +
                                        "    <li><code>add(), contains(), remove()</code></li>\n" +
                                        "</ul>\n" +
                                        "\n" +
                                        "<h3>Map (HashMap, TreeMap)</h3>\n" +
                                        "<ul>\n" +
                                        "    <li>Schlüssel-Wert-Paare</li>\n" +
                                        "    <li>Eindeutige Schlüssel</li>\n" +
                                        "    <li><code>put(), get(), containsKey(), keySet()</code></li>\n" +
                                        "</ul>"
                        )
                ));

        // Aufgabe 9.1: Studenten sortieren mit Comparable
        Exercise ex9_1 = new Exercise();
        ex9_1.setTitle("DHSH-Studentenliste: Sortieren mit Comparable");
        ex9_1.setDescription(
                "Die DHSH möchte Studenten nach Matrikelnummer sortieren können.\n\n" +
                        "Konzepte:\n" +
                        "• Comparable Interface implementieren\n" +
                        "• compareTo() Methode\n" +
                        "• Collections.sort() verwenden\n\n" +
                        "Aufgaben:\n" +
                        "• Implementiere Comparable<Student> in der Student-Klasse\n" +
                        "• Sortiere nach Matrikelnummer (aufsteigend)\n" +
                        "• Gib die sortierte Liste aus"
        );
        ex9_1.setStarterCode(
                "import java.util.ArrayList;\n" +
                        "import java.util.Collections;\n\n" +
                        "public class Student implements Comparable<Student> {\n\n" +
                        "    private String name;\n" +
                        "    private int matrikelnummer;\n\n" +
                        "    public Student(String name, int matrikelnummer) {\n" +
                        "        this.name = name;\n" +
                        "        this.matrikelnummer = matrikelnummer;\n" +
                        "    }\n\n" +
                        "    // TODO 1: Implementiere compareTo()\n" +
                        "    // Sortiere nach Matrikelnummer aufsteigend\n" +
                        "    @Override\n" +
                        "    public int compareTo(Student other) {\n" +
                        "        // Dein Code hier\n" +
                        "        return 0;\n" +
                        "    }\n\n" +
                        "    // TODO 2: Überschreibe toString()\n" +
                        "    @Override\n" +
                        "    public String toString() {\n" +
                        "        // Dein Code hier\n" +
                        "        return \"\";\n" +
                        "    }\n\n" +
                        "    public String getName() { return name; }\n" +
                        "    public int getMatrikelnummer() { return matrikelnummer; }\n\n" +
                        "    public static void main(String[] args) {\n" +
                        "        ArrayList<Student> studenten = new ArrayList<>();\n" +
                        "        studenten.add(new Student(\"Maria\", 54321));\n" +
                        "        studenten.add(new Student(\"Alex\", 12345));\n" +
                        "        studenten.add(new Student(\"Tim\", 33333));\n\n" +
                        "        System.out.println(\"Vor Sortierung:\");\n" +
                        "        for (Student s : studenten) {\n" +
                        "            System.out.println(s);\n" +
                        "        }\n\n" +
                        "        // TODO 3: Sortiere die Liste\n" +
                        "        // Collections.sort(...)\n\n" +
                        "        System.out.println(\"\\nNach Sortierung:\");\n" +
                        "        for (Student s : studenten) {\n" +
                        "            System.out.println(s);\n" +
                        "        }\n" +
                        "    }\n" +
                        "}"
        );
        ex9_1.setSolution(
                "import java.util.ArrayList;\n" +
                        "import java.util.Collections;\n\n" +
                        "public class Student implements Comparable<Student> {\n\n" +
                        "    private String name;\n" +
                        "    private int matrikelnummer;\n\n" +
                        "    public Student(String name, int matrikelnummer) {\n" +
                        "        this.name = name;\n" +
                        "        this.matrikelnummer = matrikelnummer;\n" +
                        "    }\n\n" +
                        "    @Override\n" +
                        "    public int compareTo(Student other) {\n" +
                        "        return this.matrikelnummer - other.matrikelnummer;\n" +
                        "    }\n\n" +
                        "    @Override\n" +
                        "    public String toString() {\n" +
                        "        return name + \" (\" + matrikelnummer + \")\";\n" +
                        "    }\n\n" +
                        "    public String getName() { return name; }\n" +
                        "    public int getMatrikelnummer() { return matrikelnummer; }\n\n" +
                        "    public static void main(String[] args) {\n" +
                        "        ArrayList<Student> studenten = new ArrayList<>();\n" +
                        "        studenten.add(new Student(\"Maria\", 54321));\n" +
                        "        studenten.add(new Student(\"Alex\", 12345));\n" +
                        "        studenten.add(new Student(\"Tim\", 33333));\n\n" +
                        "        System.out.println(\"Vor Sortierung:\");\n" +
                        "        for (Student s : studenten) {\n" +
                        "            System.out.println(s);\n" +
                        "        }\n\n" +
                        "        Collections.sort(studenten);\n\n" +
                        "        System.out.println(\"\\nNach Sortierung:\");\n" +
                        "        for (Student s : studenten) {\n" +
                        "            System.out.println(s);\n" +
                        "        }\n" +
                        "    }\n" +
                        "}"
        );
        ex9_1.setDifficulty("MEDIUM");
        ex9_1.setLesson(lesson9);
        exerciseSeedService.saveExerciseIfNotExists(ex9_1);

        // Aufgabe 9.2: Modulverwaltung mit Map
        Exercise ex9_2 = new Exercise();
        ex9_2.setTitle("DHSH-Modulkatalog: Collections mit Map");
        ex9_2.setDescription(
                "Die DHSH möchte einen Modulkatalog verwalten, in dem Module nach " +
                        "ihrer Modulnummer schnell gefunden werden können.\n\n" +
                        "Konzepte:\n" +
                        "• HashMap verwenden\n" +
                        "• put(), get(), containsKey()\n" +
                        "• Über Map iterieren\n\n" +
                        "Aufgaben:\n" +
                        "• Erstelle eine HashMap<Integer, String> für Modulnummer → Modulname\n" +
                        "• Füge Module hinzu\n" +
                        "• Suche nach einer Modulnummer\n" +
                        "• Gib alle Module aus"
        );
        ex9_2.setStarterCode(
                "import java.util.HashMap;\n" +
                        "import java.util.Map;\n\n" +
                        "public class Modulkatalog {\n\n" +
                        "    public static void main(String[] args) {\n" +
                        "        // TODO 1: Erstelle eine HashMap für Modulnummer -> Modulname\n" +
                        "        HashMap<Integer, String> module = new HashMap<>();\n\n" +
                        "        // TODO 2: Füge mindestens 4 Module hinzu\n" +
                        "        // Beispiel: 101 -> \"Programmieren 1\"\n\n" +
                        "        // TODO 3: Prüfe ob Modul 101 existiert und gib es aus\n" +
                        "        int gesuchteNummer = 101;\n\n" +
                        "        // TODO 4: Gib alle Module aus (Schleife über entrySet)\n" +
                        "        System.out.println(\"\\nAlle Module:\");\n" +
                        "    }\n" +
                        "}"
        );
        ex9_2.setSolution(
                "import java.util.HashMap;\n" +
                        "import java.util.Map;\n\n" +
                        "public class Modulkatalog {\n\n" +
                        "    public static void main(String[] args) {\n" +
                        "        HashMap<Integer, String> module = new HashMap<>();\n\n" +
                        "        module.put(101, \"Programmieren 1\");\n" +
                        "        module.put(102, \"Programmieren 2\");\n" +
                        "        module.put(201, \"Datenbanken\");\n" +
                        "        module.put(301, \"Software Engineering\");\n\n" +
                        "        int gesuchteNummer = 101;\n" +
                        "        if (module.containsKey(gesuchteNummer)) {\n" +
                        "            System.out.println(\"Modul \" + gesuchteNummer + \": \" + module.get(gesuchteNummer));\n" +
                        "        } else {\n" +
                        "            System.out.println(\"Modul nicht gefunden\");\n" +
                        "        }\n\n" +
                        "        System.out.println(\"\\nAlle Module:\");\n" +
                        "        for (Map.Entry<Integer, String> entry : module.entrySet()) {\n" +
                        "            System.out.println(entry.getKey() + \": \" + entry.getValue());\n" +
                        "        }\n" +
                        "    }\n" +
                        "}"
        );
        ex9_2.setDifficulty("MEDIUM");
        ex9_2.setLesson(lesson9);
        exerciseSeedService.saveExerciseIfNotExists(ex9_2);

        log.info("Lesson 9 initialized with 2 exercises");
    }
}
