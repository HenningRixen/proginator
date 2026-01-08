package com.example.prog1learnapp.config;

import com.example.prog1learnapp.model.Lesson;
import com.example.prog1learnapp.model.Exercise;
import com.example.prog1learnapp.model.User;
import com.example.prog1learnapp.repository.LessonRepository;
import com.example.prog1learnapp.repository.ExerciseRepository;
import com.example.prog1learnapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final LessonRepository lessonRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(LessonRepository lessonRepository,
                           ExerciseRepository exerciseRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.lessonRepository = lessonRepository;
        this.exerciseRepository = exerciseRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
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

        // Lektion 2
        Lesson lesson2 = new Lesson(2L,
                "Lexikalisches & Datentypen",
                "Bezeichner, Kommentare, primitive Typen, Wertebereiche",
                "<h3>Primitive Datentypen</h3><p>Java kennt 8 primitive Datentypen...</p>");
        if (!lessonRepository.existsById(2L)) {
            lessonRepository.save(lesson2);
        }

        // Lektion 6
        Lesson lesson6 = new Lesson(6L,
                "Methoden verstehen: Parameter, Rückgabewerte & Rekursion",
                "In dieser Aufgabe arbeitest du mit verschiedenen Arten von Methoden.",
                " <h3>Du sollst:</h3>\n" +
                        "                        <ul>\n" +
                        "                            <li>eine <b>statische Methode</b> mit Parametern und Rückgabewert implementieren</li>\n" +
                        "                            <li>eine <b>überladene Methode</b> erstellen</li>\n" +
                        "                            <li>eine <b>rekursive Methode</b> schreiben</li>\n" +
                        "                            <li>den Unterschied zwischen <b>static-Methoden</b> und <b>Instanzmethoden</b> anwenden</li>\n" +
                        "                            <li>die <b>main-Methode</b> korrekt verwenden</li>\n" +
                        "                        </ul>\n" +
                        "                        \n" +
                        "                        <h3>Zusätzlich sollst du darauf achten, dass:</h3>\n" +
                        "                        <ul>\n" +
                        "                            <li>lokale Variablen nur innerhalb ihres <b>Scopes</b> gültig sind</li>\n" +
                        "                            <li>Methodenaufrufe über den <b>Stack</b> laufen, Objekte jedoch im <b>Heap</b> liegen</li>\n" +
                        "                        </ul>"
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

        System.out.println("Demo data initialized!");
    }

    private void saveExerciseIfNotExists(Exercise exercise) {
        boolean exists = exerciseRepository.findByLessonId(exercise.getLesson().getId())
                .stream()
                .anyMatch(e -> e.getTitle().equals(exercise.getTitle()));
        if (!exists) {
            exerciseRepository.save(exercise);
        }
    }
}