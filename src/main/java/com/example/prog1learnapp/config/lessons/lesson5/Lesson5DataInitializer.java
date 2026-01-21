package com.example.prog1learnapp.config.lessons.lesson5;

import com.example.prog1learnapp.config.lessons.LessonDataInitializer;
import com.example.prog1learnapp.config.seed.ExerciseSeedService;
import com.example.prog1learnapp.model.Exercise;
import com.example.prog1learnapp.model.Lesson;
import com.example.prog1learnapp.repository.LessonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Lesson5DataInitializer implements LessonDataInitializer {

    private static final Logger log =
            LoggerFactory.getLogger(Lesson5DataInitializer.class);

    private final LessonRepository lessonRepository;
    private final ExerciseSeedService exerciseSeedService;

    public Lesson5DataInitializer(LessonRepository lessonRepository,
                                  ExerciseSeedService exerciseSeedService) {
        this.lessonRepository = lessonRepository;
        this.exerciseSeedService = exerciseSeedService;
    }
    public void init() {

        Lesson lesson5 = lessonRepository.findById(5L)
                .orElseGet(() -> lessonRepository.save(
                        new Lesson(
                                5L,
                                "Klassen und Objekte vertiefen",
                                "Vergleichsmethoden, Sortieralgorithmen, Klassenumwandlung, Rechteckklasse",
                                "<h2>Fortgeschrittene Konzepte von Klassen</h2>\n" +
                                        "<p>In dieser Lektion vertiefen wir unser Verständnis von Klassen und Objekten mit praktischen Übungen.</p>\n" +
                                        "\n" +
                                        "<h3>Objektvergleich und Gleichheit</h3>\n" +
                                        "<ul>\n" +
                                        "    <li><code>==</code> vergleicht Referenzen (Speicheradressen)</li>\n" +
                                        "    <li><code>.equals()</code> sollte inhaltliche Gleichheit prüfen</li>\n" +
                                        "    <li>Eigene Vergleichsmethoden für spezifische Anforderungen</li>\n" +
                                        "</ul>\n" +
                                        "\n" +
                                        "<h3>Sortieralgorithmen für Objekte</h3>\n" +
                                        "<p>Objekte können nach verschiedenen Kriterien sortiert werden:</p>\n" +
                                        "<ul>\n" +
                                        "    <li><b>BubbleSort</b> - Einfacher aber ineffizienter Algorithmus</li>\n" +
                                        "    <li><b>Vergleichskriterien</b> - Matrikelnummer, Name, etc.</li>\n" +
                                        "    <li><b>Comparator</b> - Interface für benutzerdefinierte Sortierung</li>\n" +
                                        "</ul>\n" +
                                        "\n" +
                                        "<h3>Klassenverbesserung und Refactoring</h3>\n" +
                                        "<ul>\n" +
                                        "    <li>Getter/Setter mit Validierung</li>\n" +
                                        "    <li>Hilfsmethoden für spezielle Operationen</li>\n" +
                                        "    <li>Vergleichsmethoden zwischen Objekten</li>\n" +
                                        "</ul>\n" +
                                        "\n" +
                                        "<h3>Geometrische Klassen</h3>\n" +
                                        "<p>Praktisches Beispiel mit Rechtecken:</p>\n" +
                                        "<ul>\n" +
                                        "    <li>Attribute mit Validierung</li>\n" +
                                        "    <li>Berechnungsmethoden (Fläche, Umfang)</li>\n" +
                                        "    <li>Kollisionserkennung zwischen Objekten</li>\n" +
                                        "</ul>"
                        )
                ));

        // Lektion 5: Klassen und Objekte vertiefen
        if (!lessonRepository.existsById(5L)) {
            lessonRepository.save(lesson5);
        }

// Aufgabe 1: Studenten vergleichen
        Exercise ex5_1 = new Exercise();
        ex5_1.setTitle("Studenten auf inhaltliche Gleichheit prüfen");
        ex5_1.setDescription(
                "Schreiben Sie eine Klassenmethode sindGleich, die zwei Student-Objekte auf inhaltliche Gleichheit prüft.\n\n" +
                        "Hinweis: Zwei Studenten sind gleich, wenn alle ihre Attribute (Name, Matrikelnummer, Semester) gleich sind."
        );
        ex5_1.setStarterCode(
                "public class StudentVergleich {\n" +
                        "    \n" +
                        "    // Annahme: Student-Klasse existiert bereits\n" +
                        "    static class Student {\n" +
                        "        private String name;\n" +
                        "        private int matrikelnummer;\n" +
                        "        private int semester;\n" +
                        "        \n" +
                        "        public Student(String name, int matrikelnummer, int semester) {\n" +
                        "            this.name = name;\n" +
                        "            this.matrikelnummer = matrikelnummer;\n" +
                        "            this.semester = semester;\n" +
                        "        }\n" +
                        "        \n" +
                        "        // Getter-Methoden\n" +
                        "        public String getName() { return name; }\n" +
                        "        public int getMatrikelnummer() { return matrikelnummer; }\n" +
                        "        public int getSemester() { return semester; }\n" +
                        "    }\n" +
                        "    \n" +
                        "    public static void main(String[] args) {\n" +
                        "        Student s1 = new Student(\"Max Mustermann\", 12345, 3);\n" +
                        "        Student s2 = new Student(\"Max Mustermann\", 12345, 3);\n" +
                        "        Student s3 = new Student(\"Maria Musterfrau\", 67890, 2);\n" +
                        "        \n" +
                        "        System.out.println(\"s1 und s2 sind gleich: \" + sindGleich(s1, s2)); // true\n" +
                        "        System.out.println(\"s1 und s3 sind gleich: \" + sindGleich(s1, s3)); // false\n" +
                        "    }\n" +
                        "    \n" +
                        "    // TODO: Implementieren Sie die Methode sindGleich\n" +
                        "    public static boolean sindGleich(Student student1, Student student2) {\n" +
                        "        // Vergleichen Sie alle Attribute der beiden Studenten\n" +
                        "        return false;\n" +
                        "    }\n" +
                        "}"
        );
        ex5_1.setSolution(
                "public class StudentVergleich {\n" +
                        "    \n" +
                        "    static class Student {\n" +
                        "        private String name;\n" +
                        "        private int matrikelnummer;\n" +
                        "        private int semester;\n" +
                        "        \n" +
                        "        public Student(String name, int matrikelnummer, int semester) {\n" +
                        "            this.name = name;\n" +
                        "            this.matrikelnummer = matrikelnummer;\n" +
                        "            this.semester = semester;\n" +
                        "        }\n" +
                        "        \n" +
                        "        public String getName() { return name; }\n" +
                        "        public int getMatrikelnummer() { return matrikelnummer; }\n" +
                        "        public int getSemester() { return semester; }\n" +
                        "    }\n" +
                        "    \n" +
                        "    public static void main(String[] args) {\n" +
                        "        Student s1 = new Student(\"Max Mustermann\", 12345, 3);\n" +
                        "        Student s2 = new Student(\"Max Mustermann\", 12345, 3);\n" +
                        "        Student s3 = new Student(\"Maria Musterfrau\", 67890, 2);\n" +
                        "        \n" +
                        "        System.out.println(\"s1 und s2 sind gleich: \" + sindGleich(s1, s2));\n" +
                        "        System.out.println(\"s1 und s3 sind gleich: \" + sindGleich(s1, s3));\n" +
                        "    }\n" +
                        "    \n" +
                        "    public static boolean sindGleich(Student student1, Student student2) {\n" +
                        "        // Prüfen, ob beide Referenzen auf dasselbe Objekt zeigen\n" +
                        "        if (student1 == student2) {\n" +
                        "            return true;\n" +
                        "        }\n" +
                        "        \n" +
                        "        // Prüfen, ob einer der Studenten null ist\n" +
                        "        if (student1 == null || student2 == null) {\n" +
                        "            return false;\n" +
                        "        }\n" +
                        "        \n" +
                        "        // Vergleichen aller Attribute\n" +
                        "        return student1.getName().equals(student2.getName()) &&\n" +
                        "               student1.getMatrikelnummer() == student2.getMatrikelnummer() &&\n" +
                        "               student1.getSemester() == student2.getSemester();\n" +
                        "    }\n" +
                        "}"
        );
        ex5_1.setDifficulty("EASY");
        ex5_1.setLesson(lesson5);
        exerciseSeedService.saveExerciseIfNotExists(ex5_1);

// Aufgabe 2: Studenten sortieren mit BubbleSort
        Exercise ex5_2 = new Exercise();
        ex5_2.setTitle("Studenten nach Matrikelnummer sortieren (BubbleSort)");
        ex5_2.setDescription(
                "Schreiben Sie eine Methode sortiere, die eine Liste von Studenten nach ihrer Matrikelnummer sortiert.\n\n" +
                        "Verwenden Sie den BubbleSort-Algorithmus:\n" +
                        "1. Vergleichen benachbarter Elemente\n" +
                        "2. Tauschen, wenn sie in falscher Reihenfolge sind\n" +
                        "3. Wiederholen, bis die Liste sortiert ist"
        );
        ex5_2.setStarterCode(
                "import java.util.ArrayList;\n" +
                        "import java.util.Arrays;\n" +
                        "\n" +
                        "public class SortiereStudenten {\n" +
                        "    \n" +
                        "    static class Student {\n" +
                        "        private String name;\n" +
                        "        private int matrikelnummer;\n" +
                        "        private int semester;\n" +
                        "        \n" +
                        "        public Student(String name, int matrikelnummer, int semester) {\n" +
                        "            this.name = name;\n" +
                        "            this.matrikelnummer = matrikelnummer;\n" +
                        "            this.semester = semester;\n" +
                        "        }\n" +
                        "        \n" +
                        "        public String getName() { return name; }\n" +
                        "        public int getMatrikelnummer() { return matrikelnummer; }\n" +
                        "        public int getSemester() { return semester; }\n" +
                        "        \n" +
                        "        @Override\n" +
                        "        public String toString() {\n" +
                        "            return name + \" (Matr.Nr.: \" + matrikelnummer + \", Sem.: \" + semester + \")\";\n" +
                        "        }\n" +
                        "    }\n" +
                        "    \n" +
                        "    public static void main(String[] args) {\n" +
                        "        ArrayList<Student> studenten = new ArrayList<>();\n" +
                        "        studenten.add(new Student(\"Zoe Schmidt\", 54321, 2));\n" +
                        "        studenten.add(new Student(\"Anna Müller\", 12345, 3));\n" +
                        "        studenten.add(new Student(\"Ben Weber\", 98765, 1));\n" +
                        "        studenten.add(new Student(\"Max Meyer\", 23456, 4));\n" +
                        "        \n" +
                        "        System.out.println(\"Unsortierte Liste:\");\n" +
                        "        for (Student s : studenten) {\n" +
                        "            System.out.println(\"  \" + s);\n" +
                        "        }\n" +
                        "        \n" +
                        "        sortiere(studenten);\n" +
                        "        \n" +
                        "        System.out.println(\"\\nSortierte Liste (nach Matrikelnummer):\");\n" +
                        "        for (Student s : studenten) {\n" +
                        "            System.out.println(\"  \" + s);\n" +
                        "        }\n" +
                        "    }\n" +
                        "    \n" +
                        "    // TODO: Implementieren Sie BubbleSort für Studenten\n" +
                        "    public static void sortiere(ArrayList<Student> liste) {\n" +
                        "        // BubbleSort Algorithmus:\n" +
                        "        // 1. Äußere Schleife für Durchgänge\n" +
                        "        // 2. Innere Schleife für Vergleiche\n" +
                        "        // 3. Tauschen bei falscher Reihenfolge\n" +
                        "    }\n" +
                        "}"
        );
        ex5_2.setSolution(
                "import java.util.ArrayList;\n" +
                        "\n" +
                        "public class SortiereStudenten {\n" +
                        "    \n" +
                        "    static class Student {\n" +
                        "        private String name;\n" +
                        "        private int matrikelnummer;\n" +
                        "        private int semester;\n" +
                        "        \n" +
                        "        public Student(String name, int matrikelnummer, int semester) {\n" +
                        "            this.name = name;\n" +
                        "            this.matrikelnummer = matrikelnummer;\n" +
                        "            this.semester = semester;\n" +
                        "        }\n" +
                        "        \n" +
                        "        public String getName() { return name; }\n" +
                        "        public int getMatrikelnummer() { return matrikelnummer; }\n" +
                        "        public int getSemester() { return semester; }\n" +
                        "        \n" +
                        "        @Override\n" +
                        "        public String toString() {\n" +
                        "            return name + \" (Matr.Nr.: \" + matrikelnummer + \", Sem.: \" + semester + \")\";\n" +
                        "        }\n" +
                        "    }\n" +
                        "    \n" +
                        "    public static void main(String[] args) {\n" +
                        "        ArrayList<Student> studenten = new ArrayList<>();\n" +
                        "        studenten.add(new Student(\"Zoe Schmidt\", 54321, 2));\n" +
                        "        studenten.add(new Student(\"Anna Müller\", 12345, 3));\n" +
                        "        studenten.add(new Student(\"Ben Weber\", 98765, 1));\n" +
                        "        studenten.add(new Student(\"Max Meyer\", 23456, 4));\n" +
                        "        \n" +
                        "        System.out.println(\"Unsortierte Liste:\");\n" +
                        "        for (Student s : studenten) {\n" +
                        "            System.out.println(\"  \" + s);\n" +
                        "        }\n" +
                        "        \n" +
                        "        sortiere(studenten);\n" +
                        "        \n" +
                        "        System.out.println(\"\\nSortierte Liste (nach Matrikelnummer):\");\n" +
                        "        for (Student s : studenten) {\n" +
                        "            System.out.println(\"  \" + s);\n" +
                        "        }\n" +
                        "    }\n" +
                        "    \n" +
                        "    public static void sortiere(ArrayList<Student> liste) {\n" +
                        "        int n = liste.size();\n" +
                        "        boolean getauscht;\n" +
                        "        \n" +
                        "        // BubbleSort Algorithmus\n" +
                        "        for (int i = 0; i < n - 1; i++) {\n" +
                        "            getauscht = false;\n" +
                        "            \n" +
                        "            // Letzte i Elemente sind bereits sortiert\n" +
                        "            for (int j = 0; j < n - 1 - i; j++) {\n" +
                        "                // Vergleiche benachbarte Studenten nach Matrikelnummer\n" +
                        "                if (liste.get(j).getMatrikelnummer() > liste.get(j + 1).getMatrikelnummer()) {\n" +
                        "                    // Tausche die Studenten\n" +
                        "                    Student temp = liste.get(j);\n" +
                        "                    liste.set(j, liste.get(j + 1));\n" +
                        "                    liste.set(j + 1, temp);\n" +
                        "                    getauscht = true;\n" +
                        "                }\n" +
                        "            }\n" +
                        "            \n" +
                        "            // Wenn in einem Durchgang nichts getauscht wurde, ist die Liste sortiert\n" +
                        "            if (!getauscht) {\n" +
                        "                break;\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "}"
        );
        ex5_2.setDifficulty("MEDIUM");
        ex5_2.setLesson(lesson5);
        exerciseSeedService.saveExerciseIfNotExists(ex5_2);

// Aufgabe 3: Uhr-Klasse verbessern
        Exercise ex5_3 = new Exercise();
        ex5_3.setTitle("Uhr-Klasse verbessern und erweitern");
        ex5_3.setDescription(
                "Verbessern Sie die Uhr-Klasse:\n\n" +
                        "a) tick()-Methode implementieren (erhöht Sekunden um 1)\n" +
                        "b) Getter/Setter mit Validierung für Stunden, Minuten, Sekunden\n" +
                        "c) addZeit()-Methode zum Hinzufügen/Subtrahieren von Sekunden\n" +
                        "d) compare()-Methode zum Vergleichen zweier Uhren"
        );
        ex5_3.setStarterCode(
                "public class Uhr2 {\n" +
                        "    // Attribute\n" +
                        "    private int stunden;\n" +
                        "    private int minuten;\n" +
                        "    private int sekunden;\n" +
                        "    \n" +
                        "    // Konstruktor\n" +
                        "    public Uhr2(int stunden, int minuten, int sekunden) {\n" +
                        "        // TODO: Setzen der Werte mit Validierung\n" +
                        "    }\n" +
                        "    \n" +
                        "    // TODO a): tick()-Methode\n" +
                        "    public void tick() {\n" +
                        "        // Erhöht die Sekunden um 1\n" +
                        "        // Bei 60 Sekunden: Minuten erhöhen, Sekunden auf 0\n" +
                        "        // Bei 60 Minuten: Stunden erhöhen, Minuten auf 0\n" +
                        "        // Bei 24 Stunden: Auf 0 zurücksetzen\n" +
                        "    }\n" +
                        "    \n" +
                        "    // TODO b): Getter und Setter mit Validierung\n" +
                        "    // Stunden: 0-23\n" +
                        "    // Minuten: 0-59\n" +
                        "    // Sekunden: 0-59\n" +
                        "    \n" +
                        "    // TODO c): addZeit()-Methode\n" +
                        "    public void addZeit(int sekunden) {\n" +
                        "        // Fügt Sekunden hinzu (können auch negativ sein)\n" +
                        "        // Passt Stunden, Minuten, Sekunden entsprechend an\n" +
                        "    }\n" +
                        "    \n" +
                        "    // TODO d): compare()-Methode\n" +
                        "    public int compare(Uhr2 andereUhr) {\n" +
                        "        // Vergleicht diese Uhr mit einer anderen\n" +
                        "        // Rückgabe: -1 wenn diese Uhr später ist (größere Zeit)\n" +
                        "        //           1 wenn diese Uhr früher ist (kleinere Zeit)\n" +
                        "        //           0 wenn beide gleich sind\n" +
                        "        return 0;\n" +
                        "    }\n" +
                        "    \n" +
                        "    // Hilfsmethode: Gesamtzeit in Sekunden\n" +
                        "    private int gesamtSekunden() {\n" +
                        "        return stunden * 3600 + minuten * 60 + sekunden;\n" +
                        "    }\n" +
                        "    \n" +
                        "    @Override\n" +
                        "    public String toString() {\n" +
                        "        return String.format(\"%02d:%02d:%02d\", stunden, minuten, sekunden);\n" +
                        "    }\n" +
                        "    \n" +
                        "    public static void main(String[] args) {\n" +
                        "        // Testen Sie hier Ihre Implementierung\n" +
                        "    }\n" +
                        "}"
        );
        ex5_3.setSolution(
                "public class Uhr2 {\n" +
                        "    private int stunden;\n" +
                        "    private int minuten;\n" +
                        "    private int sekunden;\n" +
                        "    \n" +
                        "    public Uhr2(int stunden, int minuten, int sekunden) {\n" +
                        "        setStunden(stunden);\n" +
                        "        setMinuten(minuten);\n" +
                        "        setSekunden(sekunden);\n" +
                        "    }\n" +
                        "    \n" +
                        "    // a) tick()-Methode\n" +
                        "    public void tick() {\n" +
                        "        sekunden++;\n" +
                        "        if (sekunden >= 60) {\n" +
                        "            sekunden = 0;\n" +
                        "            minuten++;\n" +
                        "            if (minuten >= 60) {\n" +
                        "                minuten = 0;\n" +
                        "                stunden++;\n" +
                        "                if (stunden >= 24) {\n" +
                        "                    stunden = 0;\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "    \n" +
                        "    // b) Getter und Setter mit Validierung\n" +
                        "    public int getStunden() {\n" +
                        "        return stunden;\n" +
                        "    }\n" +
                        "    \n" +
                        "    public void setStunden(int stunden) {\n" +
                        "        if (stunden >= 0 && stunden < 24) {\n" +
                        "            this.stunden = stunden;\n" +
                        "        } else {\n" +
                        "            throw new IllegalArgumentException(\"Stunden müssen zwischen 0 und 23 liegen\");\n" +
                        "        }\n" +
                        "    }\n" +
                        "    \n" +
                        "    public int getMinuten() {\n" +
                        "        return minuten;\n" +
                        "    }\n" +
                        "    \n" +
                        "    public void setMinuten(int minuten) {\n" +
                        "        if (minuten >= 0 && minuten < 60) {\n" +
                        "            this.minuten = minuten;\n" +
                        "        } else {\n" +
                        "            throw new IllegalArgumentException(\"Minuten müssen zwischen 0 und 59 liegen\");\n" +
                        "        }\n" +
                        "    }\n" +
                        "    \n" +
                        "    public int getSekunden() {\n" +
                        "        return sekunden;\n" +
                        "    }\n" +
                        "    \n" +
                        "    public void setSekunden(int sekunden) {\n" +
                        "        if (sekunden >= 0 && sekunden < 60) {\n" +
                        "            this.sekunden = sekunden;\n" +
                        "        } else {\n" +
                        "            throw new IllegalArgumentException(\"Sekunden müssen zwischen 0 und 59 liegen\");\n" +
                        "        }\n" +
                        "    }\n" +
                        "    \n" +
                        "    // c) addZeit()-Methode\n" +
                        "    public void addZeit(int sekunden) {\n" +
                        "        if (sekunden == 0) return;\n" +
                        "        \n" +
                        "        // Gesamtzeit in Sekunden berechnen\n" +
                        "        int gesamt = gesamtSekunden() + sekunden;\n" +
                        "        \n" +
                        "        // Sicherstellen, dass die Zeit nicht negativ wird\n" +
                        "        while (gesamt < 0) {\n" +
                        "            gesamt += 24 * 3600; // Einen Tag addieren\n" +
                        "        }\n" +
                        "        \n" +
                        "        // In Stunden, Minuten, Sekunden umrechnen\n" +
                        "        gesamt = gesamt % (24 * 3600); // Auf 24 Stunden begrenzen\n" +
                        "        this.stunden = gesamt / 3600;\n" +
                        "        gesamt %= 3600;\n" +
                        "        this.minuten = gesamt / 60;\n" +
                        "        this.sekunden = gesamt % 60;\n" +
                        "    }\n" +
                        "    \n" +
                        "    // d) compare()-Methode\n" +
                        "    public int compare(Uhr2 andereUhr) {\n" +
                        "        int dieseSekunden = this.gesamtSekunden();\n" +
                        "        int andereSekunden = andereUhr.gesamtSekunden();\n" +
                        "        \n" +
                        "        if (dieseSekunden > andereSekunden) {\n" +
                        "            return -1; // Diese Uhr zeigt später\n" +
                        "        } else if (dieseSekunden < andereSekunden) {\n" +
                        "            return 1; // Diese Uhr zeigt früher\n" +
                        "        } else {\n" +
                        "            return 0; // Beide zeigen gleiche Zeit\n" +
                        "        }\n" +
                        "    }\n" +
                        "    \n" +
                        "    // Hilfsmethode\n" +
                        "    private int gesamtSekunden() {\n" +
                        "        return stunden * 3600 + minuten * 60 + sekunden;\n" +
                        "    }\n" +
                        "    \n" +
                        "    @Override\n" +
                        "    public String toString() {\n" +
                        "        return String.format(\"%02d:%02d:%02d\", stunden, minuten, sekunden);\n" +
                        "    }\n" +
                        "    \n" +
                        "    public static void main(String[] args) {\n" +
                        "        Uhr2 u1 = new Uhr2(10, 30, 45);\n" +
                        "        Uhr2 u2 = new Uhr2(14, 15, 20);\n" +
                        "        \n" +
                        "        System.out.println(\"Uhr 1: \" + u1);\n" +
                        "        System.out.println(\"Uhr 2: \" + u2);\n" +
                        "        \n" +
                        "        u1.tick();\n" +
                        "        System.out.println(\"Uhr 1 nach tick(): \" + u1);\n" +
                        "        \n" +
                        "        u1.addZeit(3600); // 1 Stunde addieren\n" +
                        "        System.out.println(\"Uhr 1 nach +3600s: \" + u1);\n" +
                        "        \n" +
                        "        System.out.println(\"Vergleich (u1.compare(u2)): \" + u1.compare(u2));\n" +
                        "    }\n" +
                        "}"
        );
        ex5_3.setDifficulty("HARD");
        ex5_3.setLesson(lesson5);
        exerciseSeedService.saveExerciseIfNotExists(ex5_3);

// Aufgabe 4: Verwaltungsklasse anpassen
        Exercise ex5_4 = new Exercise();
        ex5_4.setTitle("Verwaltungsklasse für Studenten anpassen");
        ex5_4.setDescription(
                "Passen Sie die Verwaltungsklasse an, um mit der verbesserten Student-Klasse zu arbeiten.\n\n" +
                        "Welche Methoden werden nicht mehr benötigt? (Hinweis: Methoden, die jetzt in der Student-Klasse selbst sind)"
        );
        ex5_4.setStarterCode(
                "import java.util.ArrayList;\n" +
                        "\n" +
                        "public class Verwaltung {\n" +
                        "    \n" +
                        "    static class Student {\n" +
                        "        private String name;\n" +
                        "        private int matrikelnummer;\n" +
                        "        private int semester;\n" +
                        "        \n" +
                        "        public Student(String name, int matrikelnummer, int semester) {\n" +
                        "            this.name = name;\n" +
                        "            this.matrikelnummer = matrikelnummer;\n" +
                        "            this.semester = semester;\n" +
                        "        }\n" +
                        "        \n" +
                        "        // Getter-Methoden\n" +
                        "        public String getName() { return name; }\n" +
                        "        public int getMatrikelnummer() { return matrikelnummer; }\n" +
                        "        public int getSemester() { return semester; }\n" +
                        "        \n" +
                        "        // Setter-Methoden\n" +
                        "        public void setName(String name) { this.name = name; }\n" +
                        "        public void setMatrikelnummer(int matrikelnummer) { this.matrikelnummer = matrikelnummer; }\n" +
                        "        public void setSemester(int semester) { this.semester = semester; }\n" +
                        "        \n" +
                        "        @Override\n" +
                        "        public String toString() {\n" +
                        "            return name + \" (Matr.Nr.: \" + matrikelnummer + \", Sem.: \" + semester + \")\";\n" +
                        "        }\n" +
                        "    }\n" +
                        "    \n" +
                        "    private ArrayList<Student> studentenListe;\n" +
                        "    \n" +
                        "    public Verwaltung() {\n" +
                        "        studentenListe = new ArrayList<>();\n" +
                        "    }\n" +
                        "    \n" +
                        "    // TODO: Diese Methoden müssen angepasst/entfernt werden\n" +
                        "    \n" +
                        "    // Alte Methode, die jetzt in Student-Klasse ist:\n" +
                        "    public static String studentToString(Student s) {\n" +
                        "        return s.getName() + \", \" + s.getMatrikelnummer() + \", \" + s.getSemester();\n" +
                        "    }\n" +
                        "    \n" +
                        "    // Alte Methode, die jetzt in Student-Klasse ist:\n" +
                        "    public static boolean sindGleich(Student s1, Student s2) {\n" +
                        "        return s1.getMatrikelnummer() == s2.getMatrikelnummer();\n" +
                        "    }\n" +
                        "    \n" +
                        "    // Diese Methoden bleiben:\n" +
                        "    public void studentHinzufuegen(Student student) {\n" +
                        "        studentenListe.add(student);\n" +
                        "    }\n" +
                        "    \n" +
                        "    public void alleStudentenAusgeben() {\n" +
                        "        for (Student s : studentenListe) {\n" +
                        "            System.out.println(s); // Jetzt wird toString() von Student verwendet\n" +
                        "        }\n" +
                        "    }\n" +
                        "    \n" +
                        "    public Student findeStudent(int matrikelnummer) {\n" +
                        "        for (Student s : studentenListe) {\n" +
                        "            if (s.getMatrikelnummer() == matrikelnummer) {\n" +
                        "                return s;\n" +
                        "            }\n" +
                        "        }\n" +
                        "        return null;\n" +
                        "    }\n" +
                        "    \n" +
                        "    public static void main(String[] args) {\n" +
                        "        Verwaltung verwaltung = new Verwaltung();\n" +
                        "        \n" +
                        "        verwaltung.studentHinzufuegen(new Student(\"Max Mustermann\", 12345, 3));\n" +
                        "        verwaltung.studentHinzufuegen(new Student(\"Maria Musterfrau\", 67890, 2));\n" +
                        "        \n" +
                        "        System.out.println(\"Alle Studenten:\");\n" +
                        "        verwaltung.alleStudentenAusgeben();\n" +
                        "    }\n" +
                        "}"
        );
        ex5_4.setSolution(
                "import java.util.ArrayList;\n" +
                        "\n" +
                        "public class Verwaltung {\n" +
                        "    \n" +
                        "    static class Student {\n" +
                        "        private String name;\n" +
                        "        private int matrikelnummer;\n" +
                        "        private int semester;\n" +
                        "        \n" +
                        "        public Student(String name, int matrikelnummer, int semester) {\n" +
                        "            this.name = name;\n" +
                        "            this.matrikelnummer = matrikelnummer;\n" +
                        "            this.semester = semester;\n" +
                        "        }\n" +
                        "        \n" +
                        "        // Getter-Methoden\n" +
                        "        public String getName() { return name; }\n" +
                        "        public int getMatrikelnummer() { return matrikelnummer; }\n" +
                        "        public int getSemester() { return semester; }\n" +
                        "        \n" +
                        "        // Setter-Methoden\n" +
                        "        public void setName(String name) { this.name = name; }\n" +
                        "        public void setMatrikelnummer(int matrikelnummer) { this.matrikelnummer = matrikelnummer; }\n" +
                        "        public void setSemester(int semester) { this.semester = semester; }\n" +
                        "        \n" +
                        "        // toString-Methode (ersetzt studentToString)\n" +
                        "        @Override\n" +
                        "        public String toString() {\n" +
                        "            return name + \" (Matr.Nr.: \" + matrikelnummer + \", Sem.: \" + semester + \")\";\n" +
                        "        }\n" +
                        "        \n" +
                        "        // equals-Methode (ersetzt sindGleich)\n" +
                        "        @Override\n" +
                        "        public boolean equals(Object obj) {\n" +
                        "            if (this == obj) return true;\n" +
                        "            if (obj == null || getClass() != obj.getClass()) return false;\n" +
                        "            Student student = (Student) obj;\n" +
                        "            return matrikelnummer == student.matrikelnummer;\n" +
                        "        }\n" +
                        "    }\n" +
                        "    \n" +
                        "    private ArrayList<Student> studentenListe;\n" +
                        "    \n" +
                        "    public Verwaltung() {\n" +
                        "        studentenListe = new ArrayList<>();\n" +
                        "    }\n" +
                        "    \n" +
                        "    // Die folgenden Methoden werden NICHT mehr benötigt,\n" +
                        "    // da sie jetzt in der Student-Klasse implementiert sind:\n" +
                        "    // - studentToString(Student s) → wird durch toString() ersetzt\n" +
                        "    // - sindGleich(Student s1, Student s2) → wird durch equals() ersetzt\n" +
                        "    \n" +
                        "    // Diese Methoden bleiben in der Verwaltungsklasse:\n" +
                        "    public void studentHinzufuegen(Student student) {\n" +
                        "        studentenListe.add(student);\n" +
                        "    }\n" +
                        "    \n" +
                        "    public void alleStudentenAusgeben() {\n" +
                        "        for (Student s : studentenListe) {\n" +
                        "            System.out.println(s);\n" +
                        "        }\n" +
                        "    }\n" +
                        "    \n" +
                        "    public Student findeStudent(int matrikelnummer) {\n" +
                        "        for (Student s : studentenListe) {\n" +
                        "            if (s.getMatrikelnummer() == matrikelnummer) {\n" +
                        "                return s;\n" +
                        "            }\n" +
                        "        }\n" +
                        "        return null;\n" +
                        "    }\n" +
                        "    \n" +
                        "    public void studentEntfernen(int matrikelnummer) {\n" +
                        "        Student zuEntfernender = findeStudent(matrikelnummer);\n" +
                        "        if (zuEntfernender != null) {\n" +
                        "            studentenListe.remove(zuEntfernender);\n" +
                        "        }\n" +
                        "    }\n" +
                        "    \n" +
                        "    public static void main(String[] args) {\n" +
                        "        Verwaltung verwaltung = new Verwaltung();\n" +
                        "        \n" +
                        "        Student s1 = new Student(\"Max Mustermann\", 12345, 3);\n" +
                        "        Student s2 = new Student(\"Maria Musterfrau\", 67890, 2);\n" +
                        "        \n" +
                        "        verwaltung.studentHinzufuegen(s1);\n" +
                        "        verwaltung.studentHinzufuegen(s2);\n" +
                        "        \n" +
                        "        System.out.println(\"Alle Studenten:\");\n" +
                        "        verwaltung.alleStudentenAusgeben();\n" +
                        "        \n" +
                        "        // Demonstration der neuen Methoden in Student\n" +
                        "        System.out.println(\"\\nDemonstration der Student-Methoden:\");\n" +
                        "        System.out.println(\"s1.toString(): \" + s1);\n" +
                        "        System.out.println(\"s1.equals(s2): \" + s1.equals(s2));\n" +
                        "        \n" +
                        "        Student s3 = new Student(\"Anderer Name\", 12345, 5);\n" +
                        "        System.out.println(\"s1.equals(s3): \" + s1.equals(s3) + \" (gleiche Matrikelnummer!)\");\n" +
                        "    }\n" +
                        "}"
        );
        ex5_4.setDifficulty("MEDIUM");
        ex5_4.setLesson(lesson5);
        exerciseSeedService.saveExerciseIfNotExists(ex5_4);

// Aufgabe 5: Rechteck-Klasse
        Exercise ex5_5 = new Exercise();
        ex5_5.setTitle("Rechteck-Klasse mit Fläche und Umfang");
        ex5_5.setDescription(
                "Entwerfen Sie eine Klasse Rechteck mit:\n\n" +
                        "a) Private Attribute für Kantenlängen und Position\n" +
                        "b) Getter/Setter mit Validierung (Kantenlängen ≥ 0)\n" +
                        "c) Konstruktor mit allen Parametern\n" +
                        "d) Methoden für Umfang und Fläche\n" +
                        "e) Testklasse mit main-Methode"
        );
        ex5_5.setStarterCode(
                "public class Rechteck {\n" +
                        "    // TODO a): Private Attribute\n" +
                        "    // Kantenlängen a und b (double)\n" +
                        "    // Position der linken unteren Ecke: x, y (double)\n" +
                        "    \n" +
                        "    // TODO c): Konstruktor\n" +
                        "    public Rechteck(double a, double b, double x, double y) {\n" +
                        "        // Setzen der Attribute mit Validierung\n" +
                        "    }\n" +
                        "    \n" +
                        "    // TODO b): Getter und Setter\n" +
                        "    // Alle Getter\n" +
                        "    // Setter mit Validierung: a und b dürfen nicht negativ sein\n" +
                        "    \n" +
                        "    // TODO d): Methoden für Umfang und Fläche\n" +
                        "    public double getUmfang() {\n" +
                        "        return 0.0; // Platzhalter\n" +
                        "    }\n" +
                        "    \n" +
                        "    public double getFlaeche() {\n" +
                        "        return 0.0; // Platzhalter\n" +
                        "    }\n" +
                        "    \n" +
                        "    @Override\n" +
                        "    public String toString() {\n" +
                        "        return \"Rechteck mit Kanten \" + a + \" x \" + b + \" an Position (\" + x + \", \" + y + \")\";\n" +
                        "    }\n" +
                        "}\n" +
                        "\n" +
                        "// TODO e): Testklasse\n" +
                        "class TestRechteck {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        // Zwei Rechtecke erzeugen\n" +
                        "        // Methoden testen\n" +
                        "    }\n" +
                        "}"
        );
        ex5_5.setSolution(
                "public class Rechteck {\n" +
                        "    // a) Private Attribute\n" +
                        "    private double a; // Kantenlänge 1\n" +
                        "    private double b; // Kantenlänge 2\n" +
                        "    private double x; // x-Koordinate der linken unteren Ecke\n" +
                        "    private double y; // y-Koordinate der linken unteren Ecke\n" +
                        "    \n" +
                        "    // c) Konstruktor\n" +
                        "    public Rechteck(double a, double b, double x, double y) {\n" +
                        "        setA(a);\n" +
                        "        setB(b);\n" +
                        "        this.x = x;\n" +
                        "        this.y = y;\n" +
                        "    }\n" +
                        "    \n" +
                        "    // b) Getter und Setter\n" +
                        "    public double getA() {\n" +
                        "        return a;\n" +
                        "    }\n" +
                        "    \n" +
                        "    public void setA(double a) {\n" +
                        "        if (a >= 0) {\n" +
                        "            this.a = a;\n" +
                        "        } else {\n" +
                        "            throw new IllegalArgumentException(\"Kantenlänge a darf nicht negativ sein\");\n" +
                        "        }\n" +
                        "    }\n" +
                        "    \n" +
                        "    public double getB() {\n" +
                        "        return b;\n" +
                        "    }\n" +
                        "    \n" +
                        "    public void setB(double b) {\n" +
                        "        if (b >= 0) {\n" +
                        "            this.b = b;\n" +
                        "        } else {\n" +
                        "            throw new IllegalArgumentException(\"Kantenlänge b darf nicht negativ sein\");\n" +
                        "        }\n" +
                        "    }\n" +
                        "    \n" +
                        "    public double getX() {\n" +
                        "        return x;\n" +
                        "    }\n" +
                        "    \n" +
                        "    public void setX(double x) {\n" +
                        "        this.x = x;\n" +
                        "    }\n" +
                        "    \n" +
                        "    public double getY() {\n" +
                        "        return y;\n" +
                        "    }\n" +
                        "    \n" +
                        "    public void setY(double y) {\n" +
                        "        this.y = y;\n" +
                        "    }\n" +
                        "    \n" +
                        "    // d) Methoden für Umfang und Fläche\n" +
                        "    public double getUmfang() {\n" +
                        "        return 2 * (a + b);\n" +
                        "    }\n" +
                        "    \n" +
                        "    public double getFlaeche() {\n" +
                        "        return a * b;\n" +
                        "    }\n" +
                        "    \n" +
                        "    @Override\n" +
                        "    public String toString() {\n" +
                        "        return String.format(\"Rechteck(%.2f x %.2f) an Position (%.2f, %.2f) - Fläche: %.2f, Umfang: %.2f\", \n" +
                        "                a, b, x, y, getFlaeche(), getUmfang());\n" +
                        "    }\n" +
                        "}\n" +
                        "\n" +
                        "// e) Testklasse\n" +
                        "class TestRechteck {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        // Zwei Rechtecke erzeugen\n" +
                        "        Rechteck r1 = new Rechteck(5.0, 3.0, 0.0, 0.0);\n" +
                        "        Rechteck r2 = new Rechteck(4.0, 6.0, 2.0, 1.0);\n" +
                        "        \n" +
                        "        // Methoden testen\n" +
                        "        System.out.println(\"Rechteck 1:\");\n" +
                        "        System.out.println(\"  \" + r1);\n" +
                        "        System.out.println(\"  Fläche: \" + r1.getFlaeche());\n" +
                        "        System.out.println(\"  Umfang: \" + r1.getUmfang());\n" +
                        "        \n" +
                        "        System.out.println(\"\\nRechteck 2:\");\n" +
                        "        System.out.println(\"  \" + r2);\n" +
                        "        System.out.println(\"  Fläche: \" + r2.getFlaeche());\n" +
                        "        System.out.println(\"  Umfang: \" + r2.getUmfang());\n" +
                        "        \n" +
                        "        // Änderungen testen\n" +
                        "        r1.setA(6.0);\n" +
                        "        r1.setB(4.0);\n" +
                        "        System.out.println(\"\\nRechteck 1 nach Änderung:\");\n" +
                        "        System.out.println(\"  \" + r1);\n" +
                        "        \n" +
                        "        // Test ungültiger Wert\n" +
                        "        try {\n" +
                        "            r2.setA(-2.0);\n" +
                        "        } catch (IllegalArgumentException e) {\n" +
                        "            System.out.println(\"\\nFehler (erwartet): \" + e.getMessage());\n" +
                        "        }\n" +
                        "    }\n" +
                        "}"
        );
        ex5_5.setDifficulty("MEDIUM");
        ex5_5.setLesson(lesson5);
        exerciseSeedService.saveExerciseIfNotExists(ex5_5);

// Aufgabe 6: Rechteck-Überdeckung
        Exercise ex5_6 = new Exercise();
        ex5_6.setTitle("Überprüfung von Rechteck-Überdeckungen");
        ex5_6.setDescription(
                "Ergänzen Sie die Rechteck-Klasse um eine Methode ueberdeckt, die prüft, ob sich zwei Rechtecke überdecken.\n\n" +
                        "Testen Sie die Methode in der Testklasse."
        );
        ex5_6.setStarterCode(
                "// Fortsetzung der Rechteck-Klasse aus Aufgabe 5\n" +
                        "class Rechteck {\n" +
                        "    // ... bestehender Code aus Aufgabe 5 ...\n" +
                        "    \n" +
                        "    // TODO: ueberdeckt-Methode\n" +
                        "    public boolean ueberdeckt(Rechteck anderes) {\n" +
                        "        // Prüfen, ob sich dieses Rechteck mit dem anderen überdeckt\n" +
                        "        // Zwei Rechtecke überdecken sich, wenn:\n" +
                        "        // 1. Ihre x-Bereiche sich überlappen\n" +
                        "        // 2. Ihre y-Bereiche sich überlappen\n" +
                        "        return false;\n" +
                        "    }\n" +
                        "}\n" +
                        "\n" +
                        "class TestRechteck {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        // Testfälle für Überdeckung\n" +
                        "        Rechteck r1 = new Rechteck(4, 3, 0, 0);\n" +
                        "        Rechteck r2 = new Rechteck(3, 2, 2, 1);\n" +
                        "        Rechteck r3 = new Rechteck(2, 2, 5, 5); // Nicht überlappend\n" +
                        "        \n" +
                        "        System.out.println(\"r1: \" + r1);\n" +
                        "        System.out.println(\"r2: \" + r2);\n" +
                        "        System.out.println(\"r3: \" + r3);\n" +
                        "        \n" +
                        "        System.out.println(\"\\nr1.ueberdeckt(r2): \" + r1.ueberdeckt(r2)); // sollte true sein\n" +
                        "        System.out.println(\"r1.ueberdeckt(r3): \" + r1.ueberdeckt(r3)); // sollte false sein\n" +
                        "        System.out.println(\"r2.ueberdeckt(r3): \" + r2.ueberdeckt(r3)); // sollte false sein\n" +
                        "    }\n" +
                        "}"
        );
        ex5_6.setSolution(
                "class Rechteck {\n" +
                        "    private double a;\n" +
                        "    private double b;\n" +
                        "    private double x;\n" +
                        "    private double y;\n" +
                        "    \n" +
                        "    public Rechteck(double a, double b, double x, double y) {\n" +
                        "        setA(a);\n" +
                        "        setB(b);\n" +
                        "        this.x = x;\n" +
                        "        this.y = y;\n" +
                        "    }\n" +
                        "    \n" +
                        "    public double getA() { return a; }\n" +
                        "    public double getB() { return b; }\n" +
                        "    public double getX() { return x; }\n" +
                        "    public double getY() { return y; }\n" +
                        "    \n" +
                        "    public void setA(double a) {\n" +
                        "        if (a >= 0) this.a = a;\n" +
                        "        else throw new IllegalArgumentException(\"Kantenlänge a darf nicht negativ sein\");\n" +
                        "    }\n" +
                        "    \n" +
                        "    public void setB(double b) {\n" +
                        "        if (b >= 0) this.b = b;\n" +
                        "        else throw new IllegalArgumentException(\"Kantenlänge b darf nicht negativ sein\");\n" +
                        "    }\n" +
                        "    \n" +
                        "    public void setX(double x) { this.x = x; }\n" +
                        "    public void setY(double y) { this.y = y; }\n" +
                        "    \n" +
                        "    public double getUmfang() { return 2 * (a + b); }\n" +
                        "    public double getFlaeche() { return a * b; }\n" +
                        "    \n" +
                        "    // Neue Methode zur Überprüfung der Überdeckung\n" +
                        "    public boolean ueberdeckt(Rechteck anderes) {\n" +
                        "        // Berechne die rechte obere Ecke dieses Rechtecks\n" +
                        "        double diesesRechts = this.x + this.a;\n" +
                        "        double diesesOben = this.y + this.b;\n" +
                        "        \n" +
                        "        // Berechne die rechte obere Ecke des anderen Rechtecks\n" +
                        "        double anderesRechts = anderes.x + anderes.a;\n" +
                        "        double anderesOben = anderes.y + anderes.b;\n" +
                        "        \n" +
                        "        // Überprüfe Überlappung in x-Richtung\n" +
                        "        boolean xUeberlappung = this.x < anderesRechts && diesesRechts > anderes.x;\n" +
                        "        \n" +
                        "        // Überprüfe Überlappung in y-Richtung\n" +
                        "        boolean yUeberlappung = this.y < anderesOben && diesesOben > anderes.y;\n" +
                        "        \n" +
                        "        // Beide müssen überlappen für eine Überdeckung\n" +
                        "        return xUeberlappung && yUeberlappung;\n" +
                        "    }\n" +
                        "    \n" +
                        "    @Override\n" +
                        "    public String toString() {\n" +
                        "        return String.format(\"Rechteck(%.1f x %.1f) von (%.1f,%.1f) bis (%.1f,%.1f)\", \n" +
                        "                a, b, x, y, x + a, y + b);\n" +
                        "    }\n" +
                        "}\n" +
                        "\n" +
                        "class TestRechteck {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        // Testfälle\n" +
                        "        Rechteck r1 = new Rechteck(4, 3, 0, 0);\n" +
                        "        Rechteck r2 = new Rechteck(3, 2, 2, 1); // Überlappt mit r1\n" +
                        "        Rechteck r3 = new Rechteck(2, 2, 5, 5); // Nicht überlappend\n" +
                        "        Rechteck r4 = new Rechteck(6, 4, 1, 1); // Größeres Rechteck, das r1 umfasst\n" +
                        "        Rechteck r5 = new Rechteck(1, 1, 3.5, 2.5); // Kleines Rechteck innerhalb von r1\n" +
                        "        \n" +
                        "        System.out.println(\"Test-Rechtecke:\");\n" +
                        "        System.out.println(\"r1: \" + r1);\n" +
                        "        System.out.println(\"r2: \" + r2);\n" +
                        "        System.out.println(\"r3: \" + r3);\n" +
                        "        System.out.println(\"r4: \" + r4);\n" +
                        "        System.out.println(\"r5: \" + r5);\n" +
                        "        \n" +
                        "        System.out.println(\"\\nÜberdeckungstests:\");\n" +
                        "        System.out.println(\"r1.ueberdeckt(r2): \" + r1.ueberdeckt(r2) + \" (erwartet: true)\");\n" +
                        "        System.out.println(\"r1.ueberdeckt(r3): \" + r1.ueberdeckt(r3) + \" (erwartet: false)\");\n" +
                        "        System.out.println(\"r1.ueberdeckt(r4): \" + r1.ueberdeckt(r4) + \" (erwartet: true)\");\n" +
                        "        System.out.println(\"r1.ueberdeckt(r5): \" + r1.ueberdeckt(r5) + \" (erwartet: true)\");\n" +
                        "        System.out.println(\"r3.ueberdeckt(r4): \" + r3.ueberdeckt(r4) + \" (erwartet: false)\");\n" +
                        "        \n" +
                        "        // Test mit sich selbst\n" +
                        "        System.out.println(\"r1.ueberdeckt(r1): \" + r1.ueberdeckt(r1) + \" (erwartet: true)\");\n" +
                        "        \n" +
                        "        // Symmetrie-Test\n" +
                        "        System.out.println(\"\\nSymmetrie-Test:\");\n" +
                        "        System.out.println(\"r1.ueberdeckt(r2) == r2.ueberdeckt(r1): \" + \n" +
                        "                         (r1.ueberdeckt(r2) == r2.ueberdeckt(r1)) + \" (erwartet: true)\");\n" +
                        "    }\n" +
                        "}"
        );
        ex5_6.setDifficulty("HARD");
        ex5_6.setLesson(lesson5);
        exerciseSeedService.saveExerciseIfNotExists(ex5_6);



    }


}
