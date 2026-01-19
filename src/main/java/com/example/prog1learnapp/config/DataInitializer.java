package com.example.prog1learnapp.config;

import com.example.prog1learnapp.model.Lesson;
import com.example.prog1learnapp.model.Exercise;
import com.example.prog1learnapp.repository.LessonRepository;
import com.example.prog1learnapp.repository.ExerciseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final LessonRepository lessonRepository;
    private final ExerciseRepository exerciseRepository;

    public DataInitializer(LessonRepository lessonRepository,
                           ExerciseRepository exerciseRepository) {
        this.lessonRepository = lessonRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    public void run(String... args) {
        Lesson lesson1 = new Lesson(1L,
                "Einstieg & Werkzeuge",
                "IDE, JDK, Projektaufbau, Kompilieren / Ausführen",
                "<h3>Einrichten von IntelliJ</h3><p>\n" +
                        "    Java ist eine weit verbreitete, plattformunabhängige Programmiersprache. Sie wird in der Industrie, Forschung und Lehre eingesetzt und ist besonders für Einsteiger geeignet.\n" +
                        "</p>\n" +
                        "\n" +
                        "<h3>1. Entwicklungsumgebung: IntelliJ IDEA (Student License)</h3>\n" +
                        "<ul>\n" +
                        "    <li>\n" +
                        "        <b>IntelliJ IDEA</b> ist eine moderne und leistungsfähige IDE für Java-Entwicklung.\n" +
                        "    </li>\n" +
                        "    <li>\n" +
                        "        Studierende erhalten eine kostenlose <b>non-commercial Edition</b> über die <a href=\"https://www.jetbrains.com/community/education/#students\" target=\"_blank\">JetBrains Student License</a>.\n" +
                        "    </li>\n" +
                        "    <li>\n" +
                        "        <b>So gehst du vor:</b>\n" +
                        "        <ol>\n" +
                        "            <li>Registriere dich mit deiner Uni-Mailadresse auf der JetBrains-Website.</li>\n" +
                        "            <li>Lade IntelliJ IDEA Ultimate herunter und installiere es.</li>\n" +
                        "            <li>Melde dich in der IDE mit deinem JetBrains-Account an, um die Lizenz zu aktivieren.</li>\n" +
                        "        </ol>\n" +
                        "    </li>\n" +
                        "</ul>\n" +
                        "\n" +
                        "<h3>2. JDK installieren</h3>\n" +
                        "<ul>\n" +
                        "    <li>Lade das <b>Java Development Kit (JDK)</b> herunter: </li>\n" +
                        "    <li>Starte IntelliJ IDEA und wähle <b>New Project</b>.</li>\n" +
                        "    <li>Wähle <b>Java</b> als Projekttyp.</li>\n" +
                        "    <li>Klicke bei <b>Project SDK</b> auf <b>Add SDK</b> oder <b>Download JDK</b>.</li>\n" +
                        "    <li>Wähle die gewünschte JDK-Version (z.B. Oracle 17) und klicke auf <b>Download</b>.</li>\n" +
                        "    <li>IntelliJ lädt das JDK herunter und richtet es automatisch ein.</li>\n" +
                        "</ol>" +
                        "</ul>\n" +
                        "\n" +
                        "<h3>3. Erstes Java-Projekt anlegen</h3>\n" +
                        "<ol>\n" +
                        "    <li>Starte IntelliJ IDEA und wähle <b>New Project</b>.</li>\n" +
                        "    <li>Wähle <b>Java</b> und das installierte JDK aus.</li>\n" +
                        "    <li>Gib dem Projekt einen Namen, z.B. <code>HelloWorld</code>.</li>\n" +
                        "    <li>Lege eine neue Java-Klasse an und schreibe dein erstes Programm.</li>\n" +
                        "</ol>\n" +
                        "\n" +
                        "<h3>4. Kompilieren & Ausführen</h3>\n" +
                        "<ul>\n" +
                        "    <li>Mit dem grünen Pfeil kannst du dein Programm direkt in IntelliJ starten.</li>\n" +
                        "    <li>Die Konsole zeigt dir die Ausgaben deines Programms an.</li>\n" +
                        "</ul>\n" +
                        "\n" +
                        "<h3>5. Weitere Tipps</h3>\n" +
                        "<ul>\n" +
                        "    <li>Nutze die <b>Auto-Vervollständigung</b> (Strg+Leertaste) und <b>Refactoring-Tools</b> von IntelliJ.</li>\n" +
                        "    <li>Speichere dein Projekt regelmäßig mit <b>Git</b> (Versionierung).</li>\n" +
                        "    <li>Weitere Ressourcen findest du im <a href=\"https://www.jetbrains.com/help/idea/discover-intellij-idea.html\" target=\"_blank\">IntelliJ IDEA Guide</a>.</li>\n" +
                        "</ul>");
        if (!lessonRepository.existsById(1L)) {
            lessonRepository.save(lesson1);
        }

        // Aufgaben für Lektion 1
        Exercise ex1 = new Exercise();
        ex1.setTitle("Hello-World Programm");
        ex1.setDescription("Erstelle ein Java-Programm, das 'Hello World' auf der Konsole ausgibt.");
        ex1.setStarterCode("public class HelloWorld {\n    public static void main(String[] args) {\n        // Dein Code hier\n    }\n}");
        ex1.setSolution("public class HelloWorld {\n    public static void main(String[] args) {\n        System.out.println(\"Hello World!\");\n    }\n}");
        ex1.setDifficulty("EASY");
        ex1.setLesson(lesson1);
        saveExerciseIfNotExists(ex1);

        // Lektion 2: Lexikalisches & Datentypen
        Lesson lesson2 = new Lesson(2L,
                "Lexikalisches & Datentypen",
                "Bezeichner, Kommentare, primitive Typen, Wertebereiche",
                "<h2>Lexikalische Grundlagen</h2>\n" +
                "<p>Bevor wir programmieren können, müssen wir die Grundbausteine der Sprache Java verstehen.</p>\n" +
                "\n" +
                "<h3>Bezeichner (Identifier)</h3>\n" +
                "<p>Ein Bezeichner ist ein Name für Variablen, Methoden, Klassen etc.</p>\n" +
                "<ul>\n" +
                "    <li>Muss mit einem <b>Buchstaben</b>, <code>_</code> oder <code>$</code> beginnen</li>\n" +
                "    <li>Darf <b>keine Leerzeichen</b> oder Sonderzeichen enthalten</li>\n" +
                "    <li>Darf <b>kein reserviertes Schlüsselwort</b> sein (z.B. <code>class</code>, <code>int</code>)</li>\n" +
                "    <li><b>Konvention:</b> camelCase für Variablen (<code>meinName</code>), PascalCase für Klassen (<code>MeineKlasse</code>)</li>\n" +
                "</ul>\n" +
                "\n" +
                "<h3>Kommentare</h3>\n" +
                "<p>Kommentare werden vom Compiler ignoriert und dienen der Dokumentation.</p>\n" +
                "<pre><code>// Einzeiliger Kommentar\n" +
                "\n" +
                "/* Mehrzeiliger\n" +
                "   Kommentar */\n" +
                "\n" +
                "/** Javadoc-Kommentar\n" +
                " *  für Dokumentation\n" +
                " */</code></pre>\n" +
                "\n" +
                "<h2>Primitive Datentypen</h2>\n" +
                "<p>Java kennt <b>8 primitive Datentypen</b>, die direkt Werte speichern (nicht Referenzen):</p>\n" +
                "\n" +
                "<h3>Ganzzahlige Typen</h3>\n" +
                "<ul>\n" +
                "    <li><code>byte</code> – 8 Bit, Wertebereich: -128 bis 127</li>\n" +
                "    <li><code>short</code> – 16 Bit, Wertebereich: -32.768 bis 32.767</li>\n" +
                "    <li><code>int</code> – 32 Bit, Wertebereich: ca. -2,1 Mrd. bis 2,1 Mrd. <b>(Standard)</b></li>\n" +
                "    <li><code>long</code> – 64 Bit, Wertebereich: sehr groß (mit <code>L</code> Suffix)</li>\n" +
                "</ul>\n" +
                "\n" +
                "<h3>Gleitkommazahlen</h3>\n" +
                "<ul>\n" +
                "    <li><code>float</code> – 32 Bit, mit <code>f</code> Suffix: <code>3.14f</code></li>\n" +
                "    <li><code>double</code> – 64 Bit <b>(Standard für Dezimalzahlen)</b></li>\n" +
                "</ul>\n" +
                "\n" +
                "<h3>Weitere Typen</h3>\n" +
                "<ul>\n" +
                "    <li><code>char</code> – 16 Bit, einzelnes Unicode-Zeichen: <code>'A'</code></li>\n" +
                "    <li><code>boolean</code> – <code>true</code> oder <code>false</code></li>\n" +
                "</ul>\n" +
                "\n" +
                "<h3>Beispiel</h3>\n" +
                "<pre><code>int alter = 21;\n" +
                "double preis = 19.99;\n" +
                "char note = 'A';\n" +
                "boolean besteht = true;</code></pre>\n" +
                "\n" +
                "<h2>Typumwandlung (Casting)</h2>\n" +
                "<p>Manchmal müssen Werte von einem Typ in einen anderen umgewandelt werden:</p>\n" +
                "<ul>\n" +
                "    <li><b>Implizit:</b> Kleiner → Größer (automatisch): <code>int → long</code></li>\n" +
                "    <li><b>Explizit:</b> Größer → Kleiner (manuell): <code>(int) 3.14</code> ergibt <code>3</code></li>\n" +
                "</ul>");
        if (!lessonRepository.existsById(2L)) {
            lessonRepository.save(lesson2);
        }

        // Aufgabe für Lektion 2
        Exercise ex2 = new Exercise();
        ex2.setTitle("Variablen deklarieren und ausgeben");
        ex2.setDescription("Erstelle Variablen verschiedener primitiver Datentypen und gib sie auf der Konsole aus.");
        ex2.setStarterCode(
                "public class DatenTypen {\n" +
                "    public static void main(String[] args) {\n" +
                "        // TODO 1: Deklariere eine int-Variable 'alter' mit Wert 21\n" +
                "        \n" +
                "        // TODO 2: Deklariere eine double-Variable 'preis' mit Wert 19.99\n" +
                "        \n" +
                "        // TODO 3: Deklariere eine boolean-Variable 'istStudent' mit Wert true\n" +
                "        \n" +
                "        // TODO 4: Gib alle Variablen mit System.out.println() aus\n" +
                "    }\n" +
                "}"
        );
        ex2.setSolution(
                "public class DatenTypen {\n" +
                "    public static void main(String[] args) {\n" +
                "        int alter = 21;\n" +
                "        double preis = 19.99;\n" +
                "        boolean istStudent = true;\n" +
                "        \n" +
                "        System.out.println(\"Alter: \" + alter);\n" +
                "        System.out.println(\"Preis: \" + preis);\n" +
                "        System.out.println(\"Ist Student: \" + istStudent);\n" +
                "    }\n" +
                "}"
        );
        ex2.setDifficulty("EASY");
        ex2.setLesson(lesson2);
        saveExerciseIfNotExists(ex2);

        // Lektion 6: Methoden
        Lesson lesson6 = new Lesson(6L,
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
        );
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
        saveExerciseIfNotExists(ex6);

        // =====================================================
        // Lektion 7: Objektorientierte Programmierung
        // =====================================================
        Lesson lesson7 = new Lesson(7L,
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
        );
        if (!lessonRepository.existsById(7L)) {
            lessonRepository.save(lesson7);
        }

        // Aufgabe 7.1: Student-Klasse (Einführung)
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
        saveExerciseIfNotExists(ex7_1);

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
        saveExerciseIfNotExists(ex7_2);

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
        saveExerciseIfNotExists(ex7_3);

        log.info("Demo data initialized successfully!");
    }

    /**
     * Speichert eine Übung nur, wenn sie noch nicht existiert.
     * Hinweis: @Transactional muss auf public/protected Methode, damit Spring AOP funktioniert.
     */
    protected void saveExerciseIfNotExists(Exercise exercise) {
        boolean exists = exerciseRepository.findByLessonId(exercise.getLesson().getId())
                .stream()
                .anyMatch(e -> e.getTitle().equals(exercise.getTitle()));
        if (!exists) {
            exerciseRepository.save(exercise);
            log.debug("Exercise '{}' saved for lesson {}", exercise.getTitle(), exercise.getLesson().getId());
        }
    }
}