package com.example.prog1learnapp.config.lessons.lesson1;

import com.example.prog1learnapp.config.lessons.LessonDataInitializer;
import com.example.prog1learnapp.config.seed.ExerciseSeedService;
import com.example.prog1learnapp.model.Lesson;
import com.example.prog1learnapp.repository.LessonRepository;
import com.example.prog1learnapp.model.Exercise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Lesson1DataInitializer implements LessonDataInitializer {

    private static final Logger log =
            LoggerFactory.getLogger(Lesson1DataInitializer.class);

    private final LessonRepository lessonRepository;
    private final ExerciseSeedService exerciseSeedService;

    public Lesson1DataInitializer(LessonRepository lessonRepository,
                                  ExerciseSeedService exerciseSeedService) {
        this.lessonRepository = lessonRepository;
        this.exerciseSeedService = exerciseSeedService;
    }

    @Override
    public void init() {

        Lesson lesson1 = lessonRepository.findById(1L)
                .orElseGet(() -> lessonRepository.save(
                        new Lesson(
                                1L,
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
                        "</ul>"
                        )
                ));

        if (!lessonRepository.existsById(1L)) {
            lessonRepository.save(lesson1);
        }

        Exercise ex1 = new Exercise();
        ex1.setTitle("Hello-World Programm");
        ex1.setDescription(
                "Erstelle ein Java-Programm, das 'Hello World' auf der Konsole ausgibt."
        );
        ex1.setStarterCode(
                "public class HelloWorld {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        // Dein Code hier\n" +
                        "    }\n" +
                        "}"
        );
        ex1.setSolution(
                "public class HelloWorld {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        System.out.println(\"Hello World!\");\n" +
                        "    }\n" +
                        "}"
        );
        ex1.setDifficulty("EASY");
        ex1.setLesson(lesson1);

        exerciseSeedService.saveExerciseIfNotExists(ex1);
    }
}
