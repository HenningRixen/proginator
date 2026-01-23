package com.example.prog1learnapp.config.lessons.lesson7;

import com.example.prog1learnapp.config.lessons.LessonDataInitializer;
import com.example.prog1learnapp.config.seed.ExerciseSeedService;
import com.example.prog1learnapp.model.Exercise;
import com.example.prog1learnapp.model.Lesson;
import com.example.prog1learnapp.repository.LessonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Lesson7DataInitializer implements LessonDataInitializer {

    private static final Logger log =
            LoggerFactory.getLogger(Lesson7DataInitializer.class);

    private final LessonRepository lessonRepository;
    private final ExerciseSeedService exerciseSeedService;

    public Lesson7DataInitializer(LessonRepository lessonRepository,
                                  ExerciseSeedService exerciseSeedService) {
        this.lessonRepository = lessonRepository;
        this.exerciseSeedService = exerciseSeedService;
    }
    public void init() {

        Lesson lesson7 = lessonRepository.findById(7L)
                .orElseGet(() -> lessonRepository.save(
                        new Lesson(
                                7L,
                                "Objektorientierte Programmierung",
                                "Klassen, Objekte, Attribute, Konstruktoren, Methoden, Kapselung",
                                "<h3>Objektorientierte Programmierung (OOP)</h3>\n" +
                                        "<p>Die objektorientierte Programmierung ist ein Programmierparadigma, bei dem Programme aus Objekten aufgebaut werden.</p>\n" +
                                        "\n" +
                                        "<h3>Zentrale Konzepte</h3>\n" +
                                        "<ul>\n" +
                                        "    <li><b>Klassen:</b> Baupläne für Objekte, definieren Attribute und Methoden</li>\n" +
                                        "    <li><b>Objekte:</b> Instanzen von Klassen, existieren zur Laufzeit im Heap</li>\n" +
                                        "    <li><b>Attribute:</b> Eigenschaften eines Objekts (Instanzvariablen)</li>\n" +
                                        "    <li><b>Methoden:</b> Verhalten eines Objekts</li>\n" +
                                        "    <li><b>Konstruktoren:</b> Spezielle Methoden zur Initialisierung von Objekten</li>\n" +
                                        "</ul>\n" +
                                        "\n" +
                                        "<h3>Kapselung (Encapsulation)</h3>\n" +
                                        "<ul>\n" +
                                        "    <li>Attribute sollten <code>private</code> sein</li>\n" +
                                        "    <li>Zugriff erfolgt über <b>Getter</b> und <b>Setter</b></li>\n" +
                                        "    <li>Setter können Validierungen enthalten</li>\n" +
                                        "</ul>\n" +
                                        "\n" +
                                        "<h3>Das Schlüsselwort <code>this</code></h3>\n" +
                                        "<p>Mit <code>this</code> greifst du auf das aktuelle Objekt zu. Besonders nützlich, wenn Parameter und Attribute gleich heißen:</p>\n" +
                                        "<pre><code>public void setName(String name) {\n" +
                                        "    this.name = name;\n" +
                                        "}</code></pre>\n" +
                                        "\n" +
                                        "<h3>Stack vs. Heap</h3>\n" +
                                        "<ul>\n" +
                                        "    <li><b>Stack:</b> Speichert lokale Variablen und Methodenaufrufe</li>\n" +
                                        "    <li><b>Heap:</b> Speichert Objekte (mit <code>new</code> erzeugt)</li>\n" +
                                        "</ul>"
                        )
                ));

        if (!lessonRepository.existsById(7L)) {
            lessonRepository.save(lesson7);
        }
        Exercise ex7_1 = new Exercise();
        ex7_1.setTitle("Objektorientierte Programmierung: Verwaltung von Studenten an der DHSH");
        ex7_1.setDescription(
                "An der Hochschule DHSH sollen Studierende verwaltet werden. In dieser Aufgabe modellierst du einen Studenten als Objekt.\n\n" +
                        "Du arbeitest dabei mit zentralen Konzepten der Objektorientierung:\n" +
                        "• Klassen und Objekte\n" +
                        "• Attribute (Instanzvariablen)\n" +
                        "• Konstruktoren\n" +
                        "• Instanzmethoden\n" +
                        "• Zugriff auf Attribute über this\n" +
                        "• Trennung von Daten (Attribute) und Verhalten (Methoden)\n\n" +
                        "Hinweis: Jeder Student ist ein eigenes Objekt im Heap. Methoden arbeiten mit den Attributen ihres Objekts."
        );
        ex7_1.setStarterCode(
                "public class Student {\n\n" +
                        "    // TODO 1: Lege die Attribute name, matrikelnummer und semester an\n\n" +
                        "    // TODO 2: Erstelle einen Konstruktor,\n" +
                        "    // der alle Attribute initialisiert\n\n" +
                        "    // TODO 3: Erstelle eine Instanzmethode,\n" +
                        "    // die den Studenten ins nächste Semester versetzt\n\n" +
                        "    // TODO 4: Erstelle eine Instanzmethode,\n" +
                        "    // die eine kurze Info über den Studenten ausgibt\n" +
                        "    // Beispiel:\n" +
                        "    // \"Name: Alex | Matrikelnummer: 12345 | Semester: 2\"\n\n" +
                        "    public static void main(String[] args) {\n" +
                        "        Student s1 = new Student(\"Alex\", 12345, 1);\n" +
                        "        Student s2 = new Student(\"Maria\", 67890, 3);\n\n" +
                        "        s1.naechstesSemester();\n" +
                        "        s1.infoAusgeben();\n\n" +
                        "        s2.infoAusgeben();\n" +
                        "    }\n" +
                        "}"
        );
        ex7_1.setSolution(
                "public class Student {\n\n" +
                        "    // Attribute (Instanzvariablen)\n" +
                        "    private String name;\n" +
                        "    private int matrikelnummer;\n" +
                        "    private int semester;\n\n" +
                        "    // Konstruktor\n" +
                        "    public Student(String name, int matrikelnummer, int semester) {\n" +
                        "        this.name = name;\n" +
                        "        this.matrikelnummer = matrikelnummer;\n" +
                        "        this.semester = semester;\n" +
                        "    }\n\n" +
                        "    // Instanzmethode\n" +
                        "    public void naechstesSemester() {\n" +
                        "        semester++;\n" +
                        "    }\n\n" +
                        "    // Instanzmethode\n" +
                        "    public void infoAusgeben() {\n" +
                        "        System.out.println(\n" +
                        "            \"Name: \" + name +\n" +
                        "            \" | Matrikelnummer: \" + matrikelnummer +\n" +
                        "            \" | Semester: \" + semester\n" +
                        "        );\n" +
                        "    }\n\n" +
                        "    public static void main(String[] args) {\n" +
                        "        Student s1 = new Student(\"Alex\", 12345, 1);\n" +
                        "        Student s2 = new Student(\"Maria\", 67890, 3);\n\n" +
                        "        s1.naechstesSemester();\n" +
                        "        s1.infoAusgeben();\n\n" +
                        "        s2.infoAusgeben();\n" +
                        "    }\n" +
                        "}"
        );
        ex7_1.setDifficulty("EASY");
        ex7_1.setLesson(lesson7);
        exerciseSeedService.saveExerciseIfNotExists(ex7_1);

        // Aufgabe 7.2: Modul-Klasse (Kapselung, Getter/Setter)
        Exercise ex7_2 = new Exercise();
        ex7_2.setTitle("DHSH-Modulverwaltung: Klasse Modul");
        ex7_2.setDescription(
                "An der Hochschule DHSH werden Studienmodule verwaltet. In dieser Aufgabe modellierst du ein Modul als Klasse.\n\n" +
                        "Dabei sollst du zentrale Konzepte der Objektorientierung korrekt umsetzen:\n" +
                        "• sauberer Klassenaufbau\n" +
                        "• private Attribute (Kapselung)\n" +
                        "• Konstruktor zur Initialisierung\n" +
                        "• Getter- und Setter-Methoden\n" +
                        "• einfache Instanzmethoden, die mit Attributen arbeiten\n\n" +
                        "Regeln:\n" +
                        "• Alle Attribute müssen private sein\n" +
                        "• Zugriff auf Attribute erfolgt nur über Getter/Setter\n" +
                        "• Setter sollen einfache Plausibilitätsprüfungen enthalten"
        );
        ex7_2.setStarterCode(
                "public class Modul {\n\n" +
                        "    // TODO 1: Lege die privaten Attribute an:\n" +
                        "    // - modulName (String)\n" +
                        "    // - modulNummer (int)\n" +
                        "    // - semester (int)\n" +
                        "    // - credits (int)\n\n" +
                        "    // TODO 2: Erstelle einen Konstruktor,\n" +
                        "    // der alle Attribute initialisiert\n\n" +
                        "    // TODO 3: Erstelle Getter und Setter für alle Attribute\n" +
                        "    // Regeln:\n" +
                        "    // - semester darf nicht kleiner als 1 sein\n" +
                        "    // - credits dürfen nicht negativ sein\n\n" +
                        "    // TODO 4: Erstelle eine Methode,\n" +
                        "    // die eine Modulbeschreibung ausgibt\n" +
                        "    // Beispiel:\n" +
                        "    // \"Modul: Programmieren 1 (Nr. 101) | Semester: 1 | Credits: 5\"\n\n" +
                        "    public static void main(String[] args) {\n" +
                        "        Modul m1 = new Modul(\"Programmieren 1\", 101, 1, 5);\n\n" +
                        "        m1.setCredits(6);\n" +
                        "        m1.setSemester(2);\n\n" +
                        "        m1.modulInfoAusgeben();\n" +
                        "    }\n" +
                        "}"
        );
        ex7_2.setSolution(
                "public class Modul {\n\n" +
                        "    // Attribute (gekapselt)\n" +
                        "    private String modulName;\n" +
                        "    private int modulNummer;\n" +
                        "    private int semester;\n" +
                        "    private int credits;\n\n" +
                        "    // Konstruktor\n" +
                        "    public Modul(String modulName, int modulNummer, int semester, int credits) {\n" +
                        "        this.modulName = modulName;\n" +
                        "        this.modulNummer = modulNummer;\n" +
                        "        setSemester(semester);   // Nutzung des Setters\n" +
                        "        setCredits(credits);     // Nutzung des Setters\n" +
                        "    }\n\n" +
                        "    // Getter\n" +
                        "    public String getModulName() {\n" +
                        "        return modulName;\n" +
                        "    }\n\n" +
                        "    public int getModulNummer() {\n" +
                        "        return modulNummer;\n" +
                        "    }\n\n" +
                        "    public int getSemester() {\n" +
                        "        return semester;\n" +
                        "    }\n\n" +
                        "    public int getCredits() {\n" +
                        "        return credits;\n" +
                        "    }\n\n" +
                        "    // Setter mit Validierung\n" +
                        "    public void setModulName(String modulName) {\n" +
                        "        this.modulName = modulName;\n" +
                        "    }\n\n" +
                        "    public void setModulNummer(int modulNummer) {\n" +
                        "        this.modulNummer = modulNummer;\n" +
                        "    }\n\n" +
                        "    public void setSemester(int semester) {\n" +
                        "        if (semester >= 1) {\n" +
                        "            this.semester = semester;\n" +
                        "        }\n" +
                        "    }\n\n" +
                        "    public void setCredits(int credits) {\n" +
                        "        if (credits >= 0) {\n" +
                        "            this.credits = credits;\n" +
                        "        }\n" +
                        "    }\n\n" +
                        "    // Instanzmethode\n" +
                        "    public void modulInfoAusgeben() {\n" +
                        "        System.out.println(\n" +
                        "            \"Modul: \" + modulName +\n" +
                        "            \" (Nr. \" + modulNummer + \")\" +\n" +
                        "            \" | Semester: \" + semester +\n" +
                        "            \" | Credits: \" + credits\n" +
                        "        );\n" +
                        "    }\n\n" +
                        "    public static void main(String[] args) {\n" +
                        "        Modul m1 = new Modul(\"Programmieren 1\", 101, 1, 5);\n\n" +
                        "        m1.setCredits(6);\n" +
                        "        m1.setSemester(2);\n\n" +
                        "        m1.modulInfoAusgeben();\n" +
                        "    }\n" +
                        "}"
        );
        ex7_2.setDifficulty("MEDIUM");
        ex7_2.setLesson(lesson7);
        exerciseSeedService.saveExerciseIfNotExists(ex7_2);

        // Aufgabe 7.3: Studenten und Module (Fortgeschritten)
        Exercise ex7_3 = new Exercise();
        ex7_3.setTitle("DHSH-Studienverwaltung: Studenten und Module (Fortgeschritten)");
        ex7_3.setDescription(
                "An der Hochschule DHSH soll eine vereinfachte Studienverwaltung programmiert werden. " +
                        "Studierende können mehrere Module belegen.\n\n" +
                        "Ein Modul besitzt Credits und ein Semester, ein Student sammelt Credits durch belegte Module.\n\n" +
                        "In dieser Aufgabe modellierst du zwei Klassen, die zusammenarbeiten:\n" +
                        "• Modul\n" +
                        "• Student\n\n" +
                        "Du sollst dabei folgende Konzepte korrekt umsetzen:\n" +
                        "• Objektorientierter Klassenaufbau\n" +
                        "• private Attribute (Kapselung)\n" +
                        "• Getter & Setter mit Validierung\n" +
                        "• mehrere Konstruktoren\n" +
                        "• Methoden, die Objekte als Parameter verwenden\n" +
                        "• Nutzung von Listen zur Verwaltung mehrerer Objekte"
        );
        ex7_3.setStarterCode(
                "import java.util.ArrayList;\n\n" +
                        "public class Modul {\n\n" +
                        "    // TODO 1:\n" +
                        "    // private Attribute:\n" +
                        "    // - name (String)\n" +
                        "    // - nummer (int)\n" +
                        "    // - credits (int)\n" +
                        "    // - semester (int)\n\n" +
                        "    // TODO 2:\n" +
                        "    // Konstruktor mit allen Attributen\n\n" +
                        "    // TODO 3:\n" +
                        "    // Getter und Setter\n" +
                        "    // Regeln:\n" +
                        "    // - credits >= 0\n" +
                        "    // - semester >= 1\n\n" +
                        "    // TODO 4:\n" +
                        "    // Methode:\n" +
                        "    // String getModulInfo()\n" +
                        "    // Beispiel:\n" +
                        "    // \"Programmieren 1 (101) | Semester: 1 | Credits: 5\"\n" +
                        "}\n\n" +
                        "class Student {\n\n" +
                        "    // TODO 5:\n" +
                        "    // private Attribute:\n" +
                        "    // - name (String)\n" +
                        "    // - matrikelnummer (int)\n" +
                        "    // - semester (int)\n" +
                        "    // - belegteModule (ArrayList<Modul>)\n\n" +
                        "    // TODO 6:\n" +
                        "    // Konstruktor:\n" +
                        "    // name, matrikelnummer, semester\n" +
                        "    // belegteModule soll leer initialisiert werden\n\n" +
                        "    // TODO 7:\n" +
                        "    // Getter und Setter\n" +
                        "    // Regeln:\n" +
                        "    // - semester >= 1\n\n" +
                        "    // TODO 8:\n" +
                        "    // Methode:\n" +
                        "    // void modulHinzufuegen(Modul modul)\n" +
                        "    // → Modul zur Liste hinzufügen\n\n" +
                        "    // TODO 9:\n" +
                        "    // Methode:\n" +
                        "    // int berechneGesamtCredits()\n" +
                        "    // → Summe aller Credits der belegten Module\n\n" +
                        "    // TODO 10:\n" +
                        "    // Methode:\n" +
                        "    // void studentInfoAusgeben()\n" +
                        "    // → Name, Matrikelnummer, Semester, Gesamtcredits\n" +
                        "    // → Alle belegten Module ausgeben\n\n" +
                        "    public static void main(String[] args) {\n" +
                        "        Modul m1 = new Modul(\"Programmieren 1\", 101, 5, 1);\n" +
                        "        Modul m2 = new Modul(\"Datenbanken\", 202, 6, 2);\n\n" +
                        "        Student s = new Student(\"Alex\", 12345, 1);\n\n" +
                        "        s.modulHinzufuegen(m1);\n" +
                        "        s.modulHinzufuegen(m2);\n\n" +
                        "        s.studentInfoAusgeben();\n" +
                        "    }\n" +
                        "}"
        );
        ex7_3.setSolution(
                "import java.util.ArrayList;\n\n" +
                        "public class Modul {\n\n" +
                        "    private String name;\n" +
                        "    private int nummer;\n" +
                        "    private int credits;\n" +
                        "    private int semester;\n\n" +
                        "    public Modul(String name, int nummer, int credits, int semester) {\n" +
                        "        this.name = name;\n" +
                        "        this.nummer = nummer;\n" +
                        "        setCredits(credits);\n" +
                        "        setSemester(semester);\n" +
                        "    }\n\n" +
                        "    public String getName() {\n" +
                        "        return name;\n" +
                        "    }\n\n" +
                        "    public int getNummer() {\n" +
                        "        return nummer;\n" +
                        "    }\n\n" +
                        "    public int getCredits() {\n" +
                        "        return credits;\n" +
                        "    }\n\n" +
                        "    public int getSemester() {\n" +
                        "        return semester;\n" +
                        "    }\n\n" +
                        "    public void setName(String name) {\n" +
                        "        this.name = name;\n" +
                        "    }\n\n" +
                        "    public void setNummer(int nummer) {\n" +
                        "        this.nummer = nummer;\n" +
                        "    }\n\n" +
                        "    public void setCredits(int credits) {\n" +
                        "        if (credits >= 0) {\n" +
                        "            this.credits = credits;\n" +
                        "        }\n" +
                        "    }\n\n" +
                        "    public void setSemester(int semester) {\n" +
                        "        if (semester >= 1) {\n" +
                        "            this.semester = semester;\n" +
                        "        }\n" +
                        "    }\n\n" +
                        "    public String getModulInfo() {\n" +
                        "        return name + \" (\" + nummer + \")\" +\n" +
                        "               \" | Semester: \" + semester +\n" +
                        "               \" | Credits: \" + credits;\n" +
                        "    }\n" +
                        "}\n\n" +
                        "class Student {\n\n" +
                        "    private String name;\n" +
                        "    private int matrikelnummer;\n" +
                        "    private int semester;\n" +
                        "    private ArrayList<Modul> belegteModule;\n\n" +
                        "    public Student(String name, int matrikelnummer, int semester) {\n" +
                        "        this.name = name;\n" +
                        "        this.matrikelnummer = matrikelnummer;\n" +
                        "        setSemester(semester);\n" +
                        "        this.belegteModule = new ArrayList<>();\n" +
                        "    }\n\n" +
                        "    public String getName() {\n" +
                        "        return name;\n" +
                        "    }\n\n" +
                        "    public int getMatrikelnummer() {\n" +
                        "        return matrikelnummer;\n" +
                        "    }\n\n" +
                        "    public int getSemester() {\n" +
                        "        return semester;\n" +
                        "    }\n\n" +
                        "    public void setName(String name) {\n" +
                        "        this.name = name;\n" +
                        "    }\n\n" +
                        "    public void setMatrikelnummer(int matrikelnummer) {\n" +
                        "        this.matrikelnummer = matrikelnummer;\n" +
                        "    }\n\n" +
                        "    public void setSemester(int semester) {\n" +
                        "        if (semester >= 1) {\n" +
                        "            this.semester = semester;\n" +
                        "        }\n" +
                        "    }\n\n" +
                        "    public void modulHinzufuegen(Modul modul) {\n" +
                        "        belegteModule.add(modul);\n" +
                        "    }\n\n" +
                        "    public int berechneGesamtCredits() {\n" +
                        "        int summe = 0;\n" +
                        "        for (Modul m : belegteModule) {\n" +
                        "            summe += m.getCredits();\n" +
                        "        }\n" +
                        "        return summe;\n" +
                        "    }\n\n" +
                        "    public void studentInfoAusgeben() {\n" +
                        "        System.out.println(\"Student: \" + name);\n" +
                        "        System.out.println(\"Matrikelnummer: \" + matrikelnummer);\n" +
                        "        System.out.println(\"Semester: \" + semester);\n" +
                        "        System.out.println(\"Gesamtcredits: \" + berechneGesamtCredits());\n" +
                        "        System.out.println(\"Belegte Module:\");\n\n" +
                        "        for (Modul m : belegteModule) {\n" +
                        "            System.out.println(\" - \" + m.getModulInfo());\n" +
                        "        }\n" +
                        "    }\n\n" +
                        "    public static void main(String[] args) {\n" +
                        "        Modul m1 = new Modul(\"Programmieren 1\", 101, 5, 1);\n" +
                        "        Modul m2 = new Modul(\"Datenbanken\", 202, 6, 2);\n\n" +
                        "        Student s = new Student(\"Alex\", 12345, 1);\n\n" +
                        "        s.modulHinzufuegen(m1);\n" +
                        "        s.modulHinzufuegen(m2);\n\n" +
                        "        s.studentInfoAusgeben();\n" +
                        "    }\n" +
                        "}"
        );
        ex7_3.setDifficulty("HARD");
        ex7_3.setLesson(lesson7);
        exerciseSeedService.saveExerciseIfNotExists(ex7_3);
    }


}
