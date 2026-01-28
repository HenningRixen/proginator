package com.example.prog1learnapp.config.lessons.lesson8;

import com.example.prog1learnapp.config.lessons.LessonDataInitializer;
import com.example.prog1learnapp.config.seed.ExerciseSeedService;
import com.example.prog1learnapp.model.Exercise;
import com.example.prog1learnapp.model.Lesson;
import com.example.prog1learnapp.repository.LessonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Lesson8DataInitializer implements LessonDataInitializer {

    private static final Logger log =
            LoggerFactory.getLogger(Lesson8DataInitializer.class);

    private final LessonRepository lessonRepository;
    private final ExerciseSeedService exerciseSeedService;

    public Lesson8DataInitializer(LessonRepository lessonRepository,
                                  ExerciseSeedService exerciseSeedService) {
        this.lessonRepository = lessonRepository;
        this.exerciseSeedService = exerciseSeedService;
    }

    @Override
    public void init() {

        Lesson lesson8 = lessonRepository.findById(8L)
                .orElseGet(() -> lessonRepository.save(
                        new Lesson(
                                8L,
                                "Vererbung & Polymorphismus",
                                "extends, super, Überschreiben, abstract, instanceof",
                                "<h2>Vererbung in Java</h2>\n" +
                                        "<p>Vererbung ermöglicht es, neue Klassen auf Basis bestehender Klassen zu erstellen. " +
                                        "Die neue Klasse (Unterklasse/Subklasse) erbt alle Eigenschaften und Methoden der " +
                                        "Oberklasse (Superklasse).</p>\n" +
                                        "\n" +
                                        "<h3>Das Schlüsselwort <code>extends</code></h3>\n" +
                                        "<pre><code>public class Student extends Person {\n" +
                                        "    // Student erbt von Person\n" +
                                        "}</code></pre>\n" +
                                        "<ul>\n" +
                                        "    <li>Eine Klasse kann nur von <b>einer</b> Klasse erben (Einfachvererbung)</li>\n" +
                                        "    <li>Alle nicht-privaten Attribute und Methoden werden geerbt</li>\n" +
                                        "    <li>Private Mitglieder sind nur über Getter/Setter zugänglich</li>\n" +
                                        "</ul>\n" +
                                        "\n" +
                                        "<h3>Das Schlüsselwort <code>super</code></h3>\n" +
                                        "<p>Mit <code>super</code> greifst du auf die Oberklasse zu:</p>\n" +
                                        "<ul>\n" +
                                        "    <li><code>super()</code> – ruft den Konstruktor der Oberklasse auf</li>\n" +
                                        "    <li><code>super.methodenName()</code> – ruft eine Methode der Oberklasse auf</li>\n" +
                                        "</ul>\n" +
                                        "\n" +
                                        "<h3>Methodenüberschreibung (Override)</h3>\n" +
                                        "<p>Eine Unterklasse kann Methoden der Oberklasse überschreiben:</p>\n" +
                                        "<pre><code>@Override\n" +
                                        "public void info() {\n" +
                                        "    System.out.println(\"Student: \" + getName());\n" +
                                        "}</code></pre>\n" +
                                        "\n" +
                                        "<h3>Abstrakte Klassen</h3>\n" +
                                        "<ul>\n" +
                                        "    <li>Mit <code>abstract</code> markierte Klassen können nicht instanziiert werden</li>\n" +
                                        "    <li>Abstrakte Methoden haben keinen Methodenkörper</li>\n" +
                                        "    <li>Unterklassen <b>müssen</b> abstrakte Methoden implementieren</li>\n" +
                                        "</ul>\n" +
                                        "<pre><code>public abstract class Figur {\n" +
                                        "    public abstract double berechneFlaeche();\n" +
                                        "}</code></pre>\n" +
                                        "\n" +
                                        "<h3>Polymorphismus</h3>\n" +
                                        "<p>Objekte einer Unterklasse können als Objekte der Oberklasse behandelt werden:</p>\n" +
                                        "<pre><code>Figur f = new Kreis(5.0);\n" +
                                        "System.out.println(f.berechneFlaeche());</code></pre>\n" +
                                        "\n" +
                                        "<h3>Der <code>instanceof</code>-Operator</h3>\n" +
                                        "<p>Prüft zur Laufzeit, ob ein Objekt von einem bestimmten Typ ist:</p>\n" +
                                        "<pre><code>if (f instanceof Kreis) {\n" +
                                        "    Kreis k = (Kreis) f;\n" +
                                        "}</code></pre>"
                        )
                ));

        // Aufgabe 8.1: Geometrische Figuren mit Vererbung
        Exercise ex8_1 = new Exercise();
        ex8_1.setTitle("Geometrische Figuren: Vererbung und abstrakte Klassen");
        ex8_1.setDescription(
                "An der DHSH soll ein geometrisches Zeichenprogramm entwickelt werden. " +
                        "Du implementierst eine Klassenhierarchie für geometrische Figuren.\n\n" +
                        "Konzepte:\n" +
                        "• Abstrakte Basisklasse\n" +
                        "• Vererbung mit extends\n" +
                        "• Methodenüberschreibung (@Override)\n" +
                        "• Polymorphismus\n\n" +
                        "Regeln:\n" +
                        "• GeomFigur ist abstrakt und enthält die abstrakte Methode berechneFlaeche()\n" +
                        "• Kreis und Rechteck erben von GeomFigur\n" +
                        "• Jede Klasse überschreibt die Flächenberechnung"
        );
        ex8_1.setStarterCode(
                "public abstract class GeomFigur {\n\n" +
                        "    // TODO 1: Attribut 'name' (protected)\n\n" +
                        "    // TODO 2: Konstruktor mit name-Parameter\n\n" +
                        "    // TODO 3: Abstrakte Methode berechneFlaeche()\n\n" +
                        "    // TODO 4: Methode info() die Name und Fläche ausgibt\n" +
                        "}\n\n" +
                        "class Kreis extends GeomFigur {\n\n" +
                        "    // TODO 5: Attribut 'radius' (private)\n\n" +
                        "    // TODO 6: Konstruktor der super() aufruft\n\n" +
                        "    // TODO 7: Überschreibe berechneFlaeche()\n" +
                        "    // Formel: π * r²\n" +
                        "}\n\n" +
                        "class Rechteck extends GeomFigur {\n\n" +
                        "    // TODO 8: Attribute 'breite' und 'hoehe' (private)\n\n" +
                        "    // TODO 9: Konstruktor der super() aufruft\n\n" +
                        "    // TODO 10: Überschreibe berechneFlaeche()\n" +
                        "    // Formel: breite * höhe\n" +
                        "}\n\n" +
                        "class Main {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        GeomFigur k = new Kreis(\"Mein Kreis\", 5.0);\n" +
                        "        GeomFigur r = new Rechteck(\"Mein Rechteck\", 4.0, 3.0);\n\n" +
                        "        k.info();\n" +
                        "        r.info();\n" +
                        "    }\n" +
                        "}"
        );
        ex8_1.setSolution(
                "public abstract class GeomFigur {\n\n" +
                        "    protected String name;\n\n" +
                        "    public GeomFigur(String name) {\n" +
                        "        this.name = name;\n" +
                        "    }\n\n" +
                        "    public abstract double berechneFlaeche();\n\n" +
                        "    public void info() {\n" +
                        "        System.out.println(name + \": Fläche = \" + berechneFlaeche());\n" +
                        "    }\n" +
                        "}\n\n" +
                        "class Kreis extends GeomFigur {\n\n" +
                        "    private double radius;\n\n" +
                        "    public Kreis(String name, double radius) {\n" +
                        "        super(name);\n" +
                        "        this.radius = radius;\n" +
                        "    }\n\n" +
                        "    @Override\n" +
                        "    public double berechneFlaeche() {\n" +
                        "        return Math.PI * radius * radius;\n" +
                        "    }\n" +
                        "}\n\n" +
                        "class Rechteck extends GeomFigur {\n\n" +
                        "    private double breite;\n" +
                        "    private double hoehe;\n\n" +
                        "    public Rechteck(String name, double breite, double hoehe) {\n" +
                        "        super(name);\n" +
                        "        this.breite = breite;\n" +
                        "        this.hoehe = hoehe;\n" +
                        "    }\n\n" +
                        "    @Override\n" +
                        "    public double berechneFlaeche() {\n" +
                        "        return breite * hoehe;\n" +
                        "    }\n" +
                        "}\n\n" +
                        "class Main {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        GeomFigur k = new Kreis(\"Mein Kreis\", 5.0);\n" +
                        "        GeomFigur r = new Rechteck(\"Mein Rechteck\", 4.0, 3.0);\n\n" +
                        "        k.info();\n" +
                        "        r.info();\n" +
                        "    }\n" +
                        "}"
        );
        ex8_1.setDifficulty("MEDIUM");
        ex8_1.setLesson(lesson8);
        exerciseSeedService.saveExerciseIfNotExists(ex8_1);

        // Aufgabe 8.2: DHSH Mitarbeiterverwaltung mit Polymorphismus
        Exercise ex8_2 = new Exercise();
        ex8_2.setTitle("DHSH-Mitarbeiterverwaltung: Polymorphismus und instanceof");
        ex8_2.setDescription(
                "Die DHSH möchte ihr Personal verwalten: Professoren und Assistenten.\n\n" +
                        "Konzepte:\n" +
                        "• Vererbungshierarchie mit gemeinsamer Basisklasse\n" +
                        "• Überschreiben von Methoden\n" +
                        "• Polymorphe Collections (Liste von Mitarbeitern)\n" +
                        "• instanceof zur Typprüfung\n\n" +
                        "Aufgaben:\n" +
                        "• Erstelle eine Basisklasse Mitarbeiter\n" +
                        "• Professor und Assistent erben von Mitarbeiter\n" +
                        "• Professor hat zusätzlich ein Fachgebiet\n" +
                        "• Assistent hat zusätzlich Arbeitsstunden pro Woche\n" +
                        "• Beide überschreiben die info()-Methode"
        );
        ex8_2.setStarterCode(
                "import java.util.ArrayList;\n\n" +
                        "public class Mitarbeiter {\n\n" +
                        "    // TODO 1: protected Attribute: name, personalnummer\n\n" +
                        "    // TODO 2: Konstruktor\n\n" +
                        "    // TODO 3: Methode info() zur Ausgabe\n\n" +
                        "    // TODO 4: Getter für personalnummer\n" +
                        "}\n\n" +
                        "class Professor extends Mitarbeiter {\n\n" +
                        "    // TODO 5: private Attribut: fachgebiet\n\n" +
                        "    // TODO 6: Konstruktor mit super()\n\n" +
                        "    // TODO 7: Überschreibe info()\n" +
                        "}\n\n" +
                        "class Assistent extends Mitarbeiter {\n\n" +
                        "    // TODO 8: private Attribut: stundenProWoche\n\n" +
                        "    // TODO 9: Konstruktor mit super()\n\n" +
                        "    // TODO 10: Überschreibe info()\n" +
                        "}\n\n" +
                        "class Personalverwaltung {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        ArrayList<Mitarbeiter> personal = new ArrayList<>();\n\n" +
                        "        personal.add(new Professor(\"Dr. Schmidt\", 1001, \"Informatik\"));\n" +
                        "        personal.add(new Assistent(\"Anna Müller\", 2001, 20));\n" +
                        "        personal.add(new Professor(\"Dr. Weber\", 1002, \"Mathematik\"));\n\n" +
                        "        // Alle Mitarbeiter ausgeben\n" +
                        "        for (Mitarbeiter m : personal) {\n" +
                        "            m.info();\n" +
                        "        }\n\n" +
                        "        // Nur Professoren zählen mit instanceof\n" +
                        "        int profCount = 0;\n" +
                        "        for (Mitarbeiter m : personal) {\n" +
                        "            if (m instanceof Professor) {\n" +
                        "                profCount++;\n" +
                        "            }\n" +
                        "        }\n" +
                        "        System.out.println(\"Anzahl Professoren: \" + profCount);\n" +
                        "    }\n" +
                        "}"
        );
        ex8_2.setSolution(
                "import java.util.ArrayList;\n\n" +
                        "public class Mitarbeiter {\n\n" +
                        "    protected String name;\n" +
                        "    protected int personalnummer;\n\n" +
                        "    public Mitarbeiter(String name, int personalnummer) {\n" +
                        "        this.name = name;\n" +
                        "        this.personalnummer = personalnummer;\n" +
                        "    }\n\n" +
                        "    public void info() {\n" +
                        "        System.out.println(\"Mitarbeiter: \" + name + \" (Nr. \" + personalnummer + \")\");\n" +
                        "    }\n\n" +
                        "    public int getPersonalnummer() {\n" +
                        "        return personalnummer;\n" +
                        "    }\n" +
                        "}\n\n" +
                        "class Professor extends Mitarbeiter {\n\n" +
                        "    private String fachgebiet;\n\n" +
                        "    public Professor(String name, int personalnummer, String fachgebiet) {\n" +
                        "        super(name, personalnummer);\n" +
                        "        this.fachgebiet = fachgebiet;\n" +
                        "    }\n\n" +
                        "    @Override\n" +
                        "    public void info() {\n" +
                        "        System.out.println(\"Professor: \" + name + \" (Nr. \" + personalnummer + \") - Fachgebiet: \" + fachgebiet);\n" +
                        "    }\n" +
                        "}\n\n" +
                        "class Assistent extends Mitarbeiter {\n\n" +
                        "    private int stundenProWoche;\n\n" +
                        "    public Assistent(String name, int personalnummer, int stundenProWoche) {\n" +
                        "        super(name, personalnummer);\n" +
                        "        this.stundenProWoche = stundenProWoche;\n" +
                        "    }\n\n" +
                        "    @Override\n" +
                        "    public void info() {\n" +
                        "        System.out.println(\"Assistent: \" + name + \" (Nr. \" + personalnummer + \") - \" + stundenProWoche + \" Std/Woche\");\n" +
                        "    }\n" +
                        "}\n\n" +
                        "class Personalverwaltung {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        ArrayList<Mitarbeiter> personal = new ArrayList<>();\n\n" +
                        "        personal.add(new Professor(\"Dr. Schmidt\", 1001, \"Informatik\"));\n" +
                        "        personal.add(new Assistent(\"Anna Müller\", 2001, 20));\n" +
                        "        personal.add(new Professor(\"Dr. Weber\", 1002, \"Mathematik\"));\n\n" +
                        "        for (Mitarbeiter m : personal) {\n" +
                        "            m.info();\n" +
                        "        }\n\n" +
                        "        int profCount = 0;\n" +
                        "        for (Mitarbeiter m : personal) {\n" +
                        "            if (m instanceof Professor) {\n" +
                        "                profCount++;\n" +
                        "            }\n" +
                        "        }\n" +
                        "        System.out.println(\"Anzahl Professoren: \" + profCount);\n" +
                        "    }\n" +
                        "}"
        );
        ex8_2.setDifficulty("MEDIUM");
        ex8_2.setLesson(lesson8);
        exerciseSeedService.saveExerciseIfNotExists(ex8_2);

        log.info("Lesson 8 initialized with 2 exercises");
    }
}
