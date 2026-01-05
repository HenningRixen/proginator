package config;

import model.Lesson;
import model.Exercise;
import model.User;
import repository.LessonRepository;
import repository.ExerciseRepository;
import repository.UserRepository;
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
        // Create test user
        if (!userRepository.existsByUsername("test")) {
            User user = new User();
            user.setUsername("test");
            user.setPassword(passwordEncoder.encode("test123"));
            user.setDisplayName("Test Student");
            userRepository.save(user);
        }

        // Create lessons if they don't exist
        if (lessonRepository.count() == 0) {
            // Lesson 1
            Lesson lesson1 = new Lesson(1L,
                    "Einstieg & Werkzeuge",
                    "Warum Java, IDE, JDK, Projektaufbau, Kompilieren / Ausführen",
                    "<h3>Warum Java?</h3><p>Java ist eine plattformunabhängige Programmiersprache...</p>");
            lessonRepository.save(lesson1);

            // Exercises for lesson 1
            Exercise ex1 = new Exercise();
            ex1.setTitle("Hello-World Programm");
            ex1.setDescription("Erstelle ein Java-Programm, das 'Hello World' auf der Konsole ausgibt.");
            ex1.setStarterCode("public class HelloWorld {\n    public static void main(String[] args) {\n        // Dein Code hier\n    }\n}");
            ex1.setSolution("public class HelloWorld {\n    public static void main(String[] args) {\n        System.out.println(\"Hello World!\");\n    }\n}");
            ex1.setDifficulty("EASY");
            ex1.setPoints(10);
            ex1.setLesson(lesson1);
            exerciseRepository.save(ex1);

            Exercise ex2 = new Exercise();
            ex2.setTitle("Kommandozeilenparameter");
            ex2.setDescription("Schreibe ein Programm, das alle Kommandozeilenparameter ausgibt.");
            ex2.setStarterCode("public class Arguments {\n    public static void main(String[] args) {\n        // args Array verwenden\n    }\n}");
            ex2.setSolution("public class Arguments {\n    public static void main(String[] args) {\n        for (int i = 0; i < args.length; i++) {\n            System.out.println(\"Argument \" + i + \": \" + args[i]);\n        }\n    }\n}");
            ex2.setDifficulty("MEDIUM");
            ex2.setPoints(15);
            ex2.setLesson(lesson1);
            exerciseRepository.save(ex2);

            // Lesson 2
            Lesson lesson2 = new Lesson(2L,
                    "Lexikalisches & Datentypen",
                    "Bezeichner, Kommentare, primitive Typen, Wertebereiche",
                    "<h3>Primitive Datentypen</h3><p>Java kennt 8 primitive Datentypen...</p>");
            lessonRepository.save(lesson2);

            // hier dann die weiteren hinzufügen

            System.out.println("Demo data initialized!");
        }
    }
}