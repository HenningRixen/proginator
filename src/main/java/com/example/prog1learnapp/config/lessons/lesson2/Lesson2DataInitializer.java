package com.example.prog1learnapp.config.lessons.lesson2;

import com.example.prog1learnapp.config.lessons.LessonDataInitializer;
import com.example.prog1learnapp.config.seed.ExerciseSeedService;
import com.example.prog1learnapp.model.Exercise;
import com.example.prog1learnapp.model.Lesson;
import com.example.prog1learnapp.repository.LessonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Lesson2DataInitializer implements LessonDataInitializer {

    private static final Logger log =
            LoggerFactory.getLogger(Lesson2DataInitializer.class);

    private final LessonRepository lessonRepository;
    private final ExerciseSeedService exerciseSeedService;

    public Lesson2DataInitializer(LessonRepository lessonRepository,
                                  ExerciseSeedService exerciseSeedService) {
        this.lessonRepository = lessonRepository;
        this.exerciseSeedService = exerciseSeedService;
    }
    public void init() {

        Lesson lesson2 = lessonRepository.findById(2L)
                .orElseGet(() -> lessonRepository.save(
                        new Lesson(2L,
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
                "</ul>")
                ));

        if (!lessonRepository.existsById(2L)) {
            lessonRepository.save(lesson2);
        }

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
        exerciseSeedService.saveExerciseIfNotExists(ex2);

    }

}
