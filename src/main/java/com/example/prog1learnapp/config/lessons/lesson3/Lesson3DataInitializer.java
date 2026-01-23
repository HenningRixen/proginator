package com.example.prog1learnapp.config.lessons.lesson3;

import com.example.prog1learnapp.config.lessons.LessonDataInitializer;
import com.example.prog1learnapp.config.seed.ExerciseSeedService;
import com.example.prog1learnapp.model.Exercise;
import com.example.prog1learnapp.model.Lesson;
import com.example.prog1learnapp.repository.LessonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Lesson3DataInitializer implements LessonDataInitializer {

    private static final Logger log =
            LoggerFactory.getLogger(Lesson3DataInitializer.class);

    private final LessonRepository lessonRepository;
    private final ExerciseSeedService exerciseSeedService;

    public Lesson3DataInitializer(LessonRepository lessonRepository,
                                  ExerciseSeedService exerciseSeedService) {
        this.lessonRepository = lessonRepository;
        this.exerciseSeedService = exerciseSeedService;
    }
    public void init() {

        Lesson lesson3 = lessonRepository.findById(3L)
                .orElseGet(() -> lessonRepository.save(
                        new Lesson(3L,
                                "Listen und Felder",
                                "Arrays, ArrayList, Collections, Eingabe/Verarbeitung",
                                "<h2>Listen und Felder in Java</h2>\n" +
                                        "<p>In Java gibt es zwei Hauptarten von Datenstrukturen für Sammlungen: <b>Arrays</b> (Felder) und <b>Listen</b> (meist ArrayList).</p>\n" +
                                        "\n" +
                                        "<h3>Arrays (Felder)</h3>\n" +
                                        "<ul>\n" +
                                        "    <li>Feste Größe (kann nachträglich nicht geändert werden)</li>\n" +
                                        "    <li>Schneller Zugriff über Index</li>\n" +
                                        "    <li>Deklaration: <code>int[] zahlen = new int[10];</code></li>\n" +
                                        "    <li>Initialisierung: <code>int[] zahlen = {1, 2, 3};</code></li>\n" +
                                        "</ul>\n" +
                                        "\n" +
                                        "<h3>Listen (ArrayList)</h3>\n" +
                                        "<ul>\n" +
                                        "    <li>Dynamische Größe (kann wachsen/schrumpfen)</li>\n" +
                                        "    <li>Viele Hilfsmethoden (add, remove, contains, etc.)</li>\n" +
                                        "    <li>Deklaration: <code>ArrayList<Integer> liste = new ArrayList<>();</code></li>\n" +
                                        "    <li>Benötigt Import: <code>import java.util.ArrayList;</code></li>\n" +
                                        "</ul>\n" +
                                        "\n" +
                                        "<h3>Vergleich == vs. .equals()</h3>\n" +
                                        "<p>Bei Arrays und Listen vergleicht <code>==</code> die <b>Referenzen</b>, nicht den Inhalt!</p>\n" +
                                        "<pre><code>int[] a = {1, 2, 3};\n" +
                                        "int[] b = {1, 2, 3};\n" +
                                        "System.out.println(a == b); // false (verschiedene Objekte)\n" +
                                        "System.out.println(Arrays.equals(a, b)); // true (gleicher Inhalt)</code></pre>\n" +
                                        "\n" +
                                        "<h3>Wichtige Methoden</h3>\n" +
                                        "<ul>\n" +
                                        "    <li><code>Collections.shuffle(liste)</code> - mischt eine Liste</li>\n" +
                                        "    <li><code>Collections.max(liste)</code> - findet Maximum</li>\n" +
                                        "    <li><code>liste.indexOf(wert)</code> - findet Index eines Elements</li>\n" +
                                        "</ul>")
                ));

        if (!lessonRepository.existsById(3L)) {
            lessonRepository.save(lesson3);
        }

// Aufgabe 1: Listenvergleich
        Exercise ex3_1 = new Exercise();
        ex3_1.setTitle("Listenvergleich: Inhaltliche Gleichheit prüfen");
        ex3_1.setDescription(
                "Schreiben Sie ein Programm, in dem zwei Listen, listeA und listeB, durch Eingabe des Benutzers mit " +
                        "Integer-Werten gefüllt werden. Prüfen Sie auf inhaltliche Gleichheit.\n\n" +
                        "Warum lässt sich dieses Problem nicht mit einem einfachen Vergleich listeA == listeB lösen? " +
                        "Welche Vorbedingung muss erfüllt sein?"
        );
        ex3_1.setStarterCode(
                "import java.util.ArrayList;\n" +
                        "import java.util.Scanner;\n" +
                        "\n" +
                        "public class ListenVergleich {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        Scanner scanner = new Scanner(System.in);\n" +
                        "        ArrayList<Integer> listeA = new ArrayList<>();\n" +
                        "        ArrayList<Integer> listeB = new ArrayList<>();\n" +
                        "        \n" +
                        "        System.out.println(\"Geben Sie Werte für Liste A ein (Ende mit 'ende'):\");\n" +
                        "        // TODO 1: Werte für listeA einlesen\n" +
                        "        \n" +
                        "        System.out.println(\"Geben Sie Werte für Liste B ein (Ende mit 'ende'):\");\n" +
                        "        // TODO 2: Werte für listeB einlesen\n" +
                        "        \n" +
                        "        // TODO 3: Prüfung auf inhaltliche Gleichheit\n" +
                        "        // Warum funktioniert listeA == listeB nicht?\n" +
                        "        \n" +
                        "        // TODO 4: Ausgabe des Ergebnisses\n" +
                        "    }\n" +
                        "}"
        );
        ex3_1.setSolution(
                "import java.util.ArrayList;\n" +
                        "import java.util.Scanner;\n" +
                        "\n" +
                        "public class ListenVergleich {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        Scanner scanner = new Scanner(System.in);\n" +
                        "        ArrayList<Integer> listeA = new ArrayList<>();\n" +
                        "        ArrayList<Integer> listeB = new ArrayList<>();\n" +
                        "        \n" +
                        "        // Eingabe für Liste A\n" +
                        "        System.out.println(\"Geben Sie Werte für Liste A ein (Ende mit 'ende'):\");\n" +
                        "        while (scanner.hasNextInt()) {\n" +
                        "            listeA.add(scanner.nextInt());\n" +
                        "        }\n" +
                        "        scanner.next(); // 'ende' konsumieren\n" +
                        "        \n" +
                        "        // Eingabe für Liste B\n" +
                        "        System.out.println(\"Geben Sie Werte für Liste B ein (Ende mit 'ende'):\");\n" +
                        "        while (scanner.hasNextInt()) {\n" +
                        "            listeB.add(scanner.nextInt());\n" +
                        "        }\n" +
                        "        scanner.next(); // 'ende' konsumieren\n" +
                        "        \n" +
                        "        // Inhaltliche Gleichheit prüfen\n" +
                        "        boolean gleich = listeA.equals(listeB);\n" +
                        "        \n" +
                        "        System.out.println(\"Die Listen sind inhaltlich \" + (gleich ? \"gleich\" : \"ungleich\"));\n" +
                        "        \n" +
                        "        // Erklärung:\n" +
                        "        // 'listeA == listeB' vergleicht die Referenzen (Speicheradressen),\n" +
                        "        // nicht den Inhalt. Zwei verschiedene Listenobjekte haben unterschiedliche Referenzen.\n" +
                        "        // Vorbedingung: Die Listen müssen in derselben Reihenfolge die gleichen Elemente enthalten.\n" +
                        "    }\n" +
                        "}"
        );
        ex3_1.setDifficulty("EASY");
        ex3_1.setLesson(lesson3);
        exerciseSeedService.saveExerciseIfNotExists(ex3_1);

// Aufgabe 2: Min/Max Position
        Exercise ex3_2 = new Exercise();
        ex3_2.setTitle("Minimum und Maximum in einer Liste finden");
        ex3_2.setDescription(
                "Lesen Sie eine vom Benutzer vorgegebene Anzahl von int-Werten ein, speichern Sie sie in einer Liste " +
                        "und geben Sie die Positionen des Maximums und Minimums aus."
        );
        ex3_2.setStarterCode(
                "import java.util.ArrayList;\n" +
                        "import java.util.Scanner;\n" +
                        "\n" +
                        "public class MinMaxPosition {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        Scanner scanner = new Scanner(System.in);\n" +
                        "        ArrayList<Integer> zahlen = new ArrayList<>();\n" +
                        "        \n" +
                        "        System.out.print(\"Wie viele Zahlen möchten Sie eingeben? \");\n" +
                        "        int anzahl = scanner.nextInt();\n" +
                        "        \n" +
                        "        // TODO 1: Zahlen einlesen und in Liste speichern\n" +
                        "        \n" +
                        "        // TODO 2: Minimum und Maximum finden (inkl. Positionen)\n" +
                        "        \n" +
                        "        // TODO 3: Ergebnisse ausgeben\n" +
                        "    }\n" +
                        "}"
        );
        ex3_2.setSolution(
                "import java.util.ArrayList;\n" +
                        "import java.util.Scanner;\n" +
                        "\n" +
                        "public class MinMaxPosition {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        Scanner scanner = new Scanner(System.in);\n" +
                        "        ArrayList<Integer> zahlen = new ArrayList<>();\n" +
                        "        \n" +
                        "        System.out.print(\"Wie viele Zahlen möchten Sie eingeben? \");\n" +
                        "        int anzahl = scanner.nextInt();\n" +
                        "        \n" +
                        "        // Eingabe\n" +
                        "        for (int i = 0; i < anzahl; i++) {\n" +
                        "            System.out.print(\"Zahl \" + (i + 1) + \": \");\n" +
                        "            zahlen.add(scanner.nextInt());\n" +
                        "        }\n" +
                        "        \n" +
                        "        // Initialisierung mit erstem Element\n" +
                        "        int maxWert = zahlen.get(0);\n" +
                        "        int minWert = zahlen.get(0);\n" +
                        "        int maxPos = 0;\n" +
                        "        int minPos = 0;\n" +
                        "        \n" +
                        "        // Durchlaufen (starten bei 1, da 0 schon initialisiert)\n" +
                        "        for (int i = 1; i < zahlen.size(); i++) {\n" +
                        "            int aktuell = zahlen.get(i);\n" +
                        "            if (aktuell > maxWert) {\n" +
                        "                maxWert = aktuell;\n" +
                        "                maxPos = i;\n" +
                        "            }\n" +
                        "            if (aktuell < minWert) {\n" +
                        "                minWert = aktuell;\n" +
                        "                minPos = i;\n" +
                        "            }\n" +
                        "        }\n" +
                        "        \n" +
                        "        // Ausgabe (Positionen um 1 erhöht für Benutzerfreundlichkeit)\n" +
                        "        System.out.println(\"Maximum: \" + maxWert + \" an Position \" + (maxPos + 1));\n" +
                        "        System.out.println(\"Minimum: \" + minWert + \" an Position \" + (minPos + 1));\n" +
                        "    }\n" +
                        "}"
        );
        ex3_2.setDifficulty("EASY");
        ex3_2.setLesson(lesson3);
        exerciseSeedService.saveExerciseIfNotExists(ex3_2);

// Aufgabe 3: Liste durchmischen
        Exercise ex3_3 = new Exercise();
        ex3_3.setTitle("Liste durchmischen und Elemente finden");
        ex3_3.setDescription(
                "Füllen Sie eine Liste mit den Werten 1 bis 50, mischen Sie sie mit Math.random() " +
                        "und geben Sie die Position aller Elemente > 20 aus.\n\n" +
                        "Warum lässt sich dieses Problem mit einer foreach-Schleife nur umständlich lösen?"
        );
        ex3_3.setStarterCode(
                "import java.util.ArrayList;\n" +
                        "import java.util.Collections;\n" +
                        "\n" +
                        "public class ListeDurchmischen {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        ArrayList<Integer> zahlen = new ArrayList<>();\n" +
                        "        \n" +
                        "        // TODO 1: Liste mit 1..50 füllen\n" +
                        "        \n" +
                        "        // TODO 2: Liste durchmischen (Collections.shuffle oder manuell mit Math.random())\n" +
                        "        \n" +
                        "        // TODO 3: Durchmischte Liste ausgeben\n" +
                        "        \n" +
                        "        // TODO 4: Positionen aller Elemente > 20 finden und ausgeben\n" +
                        "        // Warum ist foreach hier umständlich?\n" +
                        "    }\n" +
                        "}"
        );
        ex3_3.setSolution(
                "import java.util.ArrayList;\n" +
                        "import java.util.Collections;\n" +
                        "\n" +
                        "public class ListeDurchmischen {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        ArrayList<Integer> zahlen = new ArrayList<>();\n" +
                        "        \n" +
                        "        // Liste füllen\n" +
                        "        for (int i = 1; i <= 50; i++) {\n" +
                        "            zahlen.add(i);\n" +
                        "        }\n" +
                        "        \n" +
                        "        System.out.println(\"Original: \" + zahlen);\n" +
                        "        \n" +
                        "        // Durchmischen\n" +
                        "        Collections.shuffle(zahlen);\n" +
                        "        System.out.println(\"Durchmischt: \" + zahlen);\n" +
                        "        \n" +
                        "        // Positionen der Elemente > 20\n" +
                        "        System.out.println(\"\\nElemente > 20 an folgenden Positionen:\");\n" +
                        "        for (int i = 0; i < zahlen.size(); i++) {\n" +
                        "            if (zahlen.get(i) > 20) {\n" +
                        "                System.out.println(\"Wert: \" + zahlen.get(i) + \" an Position: \" + (i + 1));\n" +
                        "            }\n" +
                        "        }\n" +
                        "        \n" +
                        "        // Erklärung: Mit einer foreach-Schleife hat man keinen Zugriff auf den Index,\n" +
                        "        // man müsste zusätzlich einen Zähler mitführen. Daher ist eine normale for-Schleife besser.\n" +
                        "    }\n" +
                        "}"
        );
        ex3_3.setDifficulty("MEDIUM");
        ex3_3.setLesson(lesson3);
        exerciseSeedService.saveExerciseIfNotExists(ex3_3);

// Aufgabe 4: Sortieren durch Einfügen
        Exercise ex3_4 = new Exercise();
        ex3_4.setTitle("Sortieren durch Einfügen (Insertion Sort ähnlich)");
        ex3_4.setDescription(
                "Lesen Sie int-Werte ein, speichern Sie sie in einer Liste und sortieren Sie sie durch Einfügen " +
                        "in eine zweite Liste:\n" +
                        "1. Leere zweite Liste anlegen\n" +
                        "2. Erstes Element aus erster Liste entfernen und in zweite einfügen\n" +
                        "3. Weitere Elemente an richtiger Position in zweiter Liste einfügen"
        );
        ex3_4.setStarterCode(
                "import java.util.ArrayList;\n" +
                        "import java.util.Scanner;\n" +
                        "\n" +
                        "public class SortierenDurchEinfuegen {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        Scanner scanner = new Scanner(System.in);\n" +
                        "        ArrayList<Integer> liste1 = new ArrayList<>();\n" +
                        "        ArrayList<Integer> liste2 = new ArrayList<>();\n" +
                        "        \n" +
                        "        System.out.print(\"Wie viele Zahlen? \");\n" +
                        "        int anzahl = scanner.nextInt();\n" +
                        "        \n" +
                        "        // TODO 1: Zahlen in liste1 einlesen\n" +
                        "        \n" +
                        "        // TODO 2: Sortieren durch Einfügen\n" +
                        "        // Solange liste1 nicht leer:\n" +
                        "        // - Erstes Element entfernen\n" +
                        "        // - In liste2 an korrekter Position einfügen\n" +
                        "        \n" +
                        "        // TODO 3: Sortierte Liste ausgeben\n" +
                        "    }\n" +
                        "}"
        );
        ex3_4.setSolution(
                "import java.util.ArrayList;\n" +
                        "import java.util.Scanner;\n" +
                        "\n" +
                        "public class SortierenDurchEinfuegen {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        Scanner scanner = new Scanner(System.in);\n" +
                        "        ArrayList<Integer> liste1 = new ArrayList<>();\n" +
                        "        ArrayList<Integer> liste2 = new ArrayList<>();\n" +
                        "        \n" +
                        "        System.out.print(\"Wie viele Zahlen? \");\n" +
                        "        int anzahl = scanner.nextInt();\n" +
                        "        \n" +
                        "        // Eingabe\n" +
                        "        for (int i = 0; i < anzahl; i++) {\n" +
                        "            System.out.print(\"Zahl \" + (i + 1) + \": \");\n" +
                        "            liste1.add(scanner.nextInt());\n" +
                        "        }\n" +
                        "        \n" +
                        "        System.out.println(\"Unsortiert: \" + liste1);\n" +
                        "        \n" +
                        "        // Sortieren durch Einfügen\n" +
                        "        while (!liste1.isEmpty()) {\n" +
                        "            int aktuell = liste1.remove(0); // erstes Element entfernen\n" +
                        "            \n" +
                        "            // Einfügeposition in liste2 finden\n" +
                        "            int pos = 0;\n" +
                        "            while (pos < liste2.size() && liste2.get(pos) < aktuell) {\n" +
                        "                pos++;\n" +
                        "            }\n" +
                        "            liste2.add(pos, aktuell); // an korrekter Position einfügen\n" +
                        "        }\n" +
                        "        \n" +
                        "        System.out.println(\"Sortiert: \" + liste2);\n" +
                        "    }\n" +
                        "}"
        );
        ex3_4.setDifficulty("MEDIUM");
        ex3_4.setLesson(lesson3);
        exerciseSeedService.saveExerciseIfNotExists(ex3_4);

// Aufgabe 5: Duplikate entfernen
        Exercise ex3_5 = new Exercise();
        ex3_5.setTitle("Duplikate aus einer String-Liste entfernen");
        ex3_5.setDescription(
                "Lesen Sie Strings ein bis ein leerer String kommt. Speichern Sie sie in einer Liste, " +
                        "erstellen Sie eine zweite Liste ohne Duplikate und geben Sie diese aus.\n" +
                        "Beispiel: [eins, zwei, drei, eins] → [eins, zwei, drei]"
        );
        ex3_5.setStarterCode(
                "import java.util.ArrayList;\n" +
                        "import java.util.Scanner;\n" +
                        "\n" +
                        "public class DuplikateEntfernen {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        Scanner scanner = new Scanner(System.in);\n" +
                        "        ArrayList<String> liste1 = new ArrayList<>();\n" +
                        "        ArrayList<String> liste2 = new ArrayList<>();\n" +
                        "        \n" +
                        "        System.out.println(\"Geben Sie Strings ein (leere Zeile zum Beenden):\");\n" +
                        "        \n" +
                        "        // TODO 1: Strings einlesen bis leerer String\n" +
                        "        \n" +
                        "        // TODO 2: Duplikate entfernen (nur erste Vorkommen in liste2 speichern)\n" +
                        "        \n" +
                        "        // TODO 3: Liste ohne Duplikate ausgeben\n" +
                        "    }\n" +
                        "}"
        );
        ex3_5.setSolution(
                "import java.util.ArrayList;\n" +
                        "import java.util.Scanner;\n" +
                        "\n" +
                        "public class DuplikateEntfernen {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        Scanner scanner = new Scanner(System.in);\n" +
                        "        ArrayList<String> liste1 = new ArrayList<>();\n" +
                        "        ArrayList<String> liste2 = new ArrayList<>();\n" +
                        "        \n" +
                        "        System.out.println(\"Geben Sie Strings ein (leere Zeile zum Beenden):\");\n" +
                        "        \n" +
                        "        // Eingabe\n" +
                        "        while (true) {\n" +
                        "            String input = scanner.nextLine();\n" +
                        "            if (input.isEmpty()) {\n" +
                        "                break;\n" +
                        "            }\n" +
                        "            liste1.add(input);\n" +
                        "        }\n" +
                        "        \n" +
                        "        System.out.println(\"Eingabe: \" + liste1);\n" +
                        "        \n" +
                        "        // Duplikate entfernen\n" +
                        "        for (String s : liste1) {\n" +
                        "            if (!liste2.contains(s)) {\n" +
                        "                liste2.add(s);\n" +
                        "            }\n" +
                        "        }\n" +
                        "        \n" +
                        "        System.out.println(\"Ohne Duplikate: \" + liste2);\n" +
                        "    }\n" +
                        "}"
        );
        ex3_5.setDifficulty("EASY");
        ex3_5.setLesson(lesson3);
        exerciseSeedService.saveExerciseIfNotExists(ex3_5);

// Aufgabe 6: Array umdrehen
        Exercise ex3_6 = new Exercise();
        ex3_6.setTitle("Array umdrehen ohne zweites Array");
        ex3_6.setDescription(
                "Lesen Sie n int-Werte ein, speichern Sie sie in einem Array und drehen Sie die Reihenfolge um, " +
                        "ohne ein zweites Array zu verwenden."
        );
        ex3_6.setStarterCode(
                "import java.util.Scanner;\n" +
                        "\n" +
                        "public class ArrayUmdrehen {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        Scanner scanner = new Scanner(System.in);\n" +
                        "        \n" +
                        "        System.out.print(\"Anzahl n: \");\n" +
                        "        int n = scanner.nextInt();\n" +
                        "        \n" +
                        "        int[] feld = new int[n];\n" +
                        "        \n" +
                        "        // TODO 1: Werte in Array einlesen\n" +
                        "        \n" +
                        "        // TODO 2: Array umdrehen (ohne zweites Array!)\n" +
                        "        // Tipp: Vertauschen der Elemente von außen nach innen\n" +
                        "        \n" +
                        "        // TODO 3: Umgedrehtes Array ausgeben\n" +
                        "    }\n" +
                        "}"
        );
        ex3_6.setSolution(
                "import java.util.Scanner;\n" +
                        "\n" +
                        "public class ArrayUmdrehen {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        Scanner scanner = new Scanner(System.in);\n" +
                        "        \n" +
                        "        System.out.print(\"Anzahl n: \");\n" +
                        "        int n = scanner.nextInt();\n" +
                        "        \n" +
                        "        int[] feld = new int[n];\n" +
                        "        \n" +
                        "        // Eingabe\n" +
                        "        for (int i = 0; i < n; i++) {\n" +
                        "            System.out.print(\"Zahl \" + (i + 1) + \": \");\n" +
                        "            feld[i] = scanner.nextInt();\n" +
                        "        }\n" +
                        "        \n" +
                        "        System.out.println(\"Original: \");\n" +
                        "        for (int wert : feld) {\n" +
                        "            System.out.print(wert + \" \");\n" +
                        "        }\n" +
                        "        System.out.println();\n" +
                        "        \n" +
                        "        // Umdrehen (in-place)\n" +
                        "        for (int i = 0; i < n / 2; i++) {\n" +
                        "            int temp = feld[i];\n" +
                        "            feld[i] = feld[n - 1 - i];\n" +
                        "            feld[n - 1 - i] = temp;\n" +
                        "        }\n" +
                        "        \n" +
                        "        System.out.println(\"Umgedreht: \");\n" +
                        "        for (int wert : feld) {\n" +
                        "            System.out.print(wert + \" \");\n" +
                        "        }\n" +
                        "    }\n" +
                        "}"
        );
        ex3_6.setDifficulty("MEDIUM");
        ex3_6.setLesson(lesson3);
        exerciseSeedService.saveExerciseIfNotExists(ex3_6);

// Aufgabe 7: String zu long
        Exercise ex3_7 = new Exercise();
        ex3_7.setTitle("String in long umwandeln mit Validierung");
        ex3_7.setDescription(
                "Lesen Sie einen String ein, prüfen Sie ob es eine ganze Zahl ist und wandeln Sie ihn " +
                        "in einen long-Wert um."
        );
        ex3_7.setStarterCode(
                "import java.util.Scanner;\n" +
                        "\n" +
                        "public class StringZuLong {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        Scanner scanner = new Scanner(System.in);\n" +
                        "        \n" +
                        "        System.out.print(\"Geben Sie eine ganze Zahl ein: \");\n" +
                        "        String eingabe = scanner.nextLine();\n" +
                        "        \n" +
                        "        // TODO 1: Prüfen, ob String eine gültige ganze Zahl ist\n" +
                        "        // Tipp: Character.isDigit(), oder try-catch mit Long.parseLong\n" +
                        "        \n" +
                        "        // TODO 2: Bei gültiger Zahl: Umwandlung und Ausgabe\n" +
                        "        // Bei ungültig: Fehlermeldung\n" +
                        "    }\n" +
                        "}"
        );
        ex3_7.setSolution(
                "import java.util.Scanner;\n" +
                        "\n" +
                        "public class StringZuLong {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        Scanner scanner = new Scanner(System.in);\n" +
                        "        \n" +
                        "        System.out.print(\"Geben Sie eine ganze Zahl ein: \");\n" +
                        "        String eingabe = scanner.nextLine();\n" +
                        "        \n" +
                        "        try {\n" +
                        "            long zahl = Long.parseLong(eingabe);\n" +
                        "            System.out.println(\"Die Zahl als long: \" + zahl);\n" +
                        "        } catch (NumberFormatException e) {\n" +
                        "            System.out.println(\"Ungültige Eingabe: '\" + eingabe + \"' ist keine ganze Zahl.\");\n" +
                        "        }\n" +
                        "    }\n" +
                        "}"
        );
        ex3_7.setDifficulty("EASY");
        ex3_7.setLesson(lesson3);
        exerciseSeedService.saveExerciseIfNotExists(ex3_7);

    }

}
